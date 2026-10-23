package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.CapacityExceededException;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.Shelter;
import com.disastermanagement.model.ShelterRegistration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for shelters and shelter_registrations tables.
 * Employs atomic transactions to strictly enforce shelter capacity limits.
 */
public class ShelterDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(Shelter shelter) throws DatabaseException {
        String sql = "INSERT INTO shelters (shelter_name, location, total_capacity, current_occupancy, contact_number, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shelter.getShelterName());
            ps.setString(2, shelter.getLocation());
            ps.setInt(3, shelter.getTotalCapacity());
            ps.setInt(4, shelter.getCurrentOccupancy());
            ps.setString(5, shelter.getContactNumber());
            ps.setString(6, shelter.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        shelter.setShelterId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add emergency shelter: " + e.getMessage(), e);
        }
    }

    public boolean update(Shelter shelter) throws DatabaseException {
        String sql = "UPDATE shelters SET shelter_name = ?, location = ?, total_capacity = ?, " +
                     "contact_number = ?, status = ? WHERE shelter_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelter.getShelterName());
            ps.setString(2, shelter.getLocation());
            ps.setInt(3, shelter.getTotalCapacity());
            ps.setString(4, shelter.getContactNumber());
            ps.setString(5, shelter.getStatus());
            ps.setInt(6, shelter.getShelterId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update shelter: " + e.getMessage(), e);
        }
    }

    public Optional<Shelter> findById(int shelterId) throws DatabaseException {
        String sql = "SELECT * FROM shelters WHERE shelter_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToShelter(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query shelter: " + e.getMessage(), e);
        }
    }

    public List<Shelter> findAll() throws DatabaseException {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT * FROM shelters ORDER BY status ASC, (total_capacity - current_occupancy) DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToShelter(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve shelters: " + e.getMessage(), e);
        }
    }

    /**
     * Atomically checks in a victim group into a shelter.
     * Enforces the capacity invariant: (current_occupancy + count) <= total_capacity.
     */
    public boolean checkInVictim(int shelterId, int victimId, int peopleCount) 
            throws DatabaseException, CapacityExceededException {
        
        Connection conn = null;
        try {
            conn = db.createNewConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Lock and check shelter occupancy and capacity
            String lockSql = "SELECT total_capacity, current_occupancy, status FROM shelters WHERE shelter_id = ? FOR UPDATE";
            int totalCap = 0;
            int currentOcc = 0;
            String status = "Open";

            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, shelterId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (rs.next()) {
                        totalCap = rs.getInt("total_capacity");
                        currentOcc = rs.getInt("current_occupancy");
                        status = rs.getString("status");
                    } else {
                        conn.rollback();
                        throw new DatabaseException("Shelter not found: " + shelterId);
                    }
                }
            }

            if ("Closed".equalsIgnoreCase(status)) {
                conn.rollback();
                throw new DatabaseException("Cannot check in evacuees: Shelter is currently Closed.");
            }

            // 2. Validate capacity limit
            if (currentOcc + peopleCount > totalCap) {
                conn.rollback();
                throw new CapacityExceededException(shelterId, totalCap, currentOcc, peopleCount);
            }

            // 3. Register shelter occupancy
            String regSql = "INSERT INTO shelter_registrations (shelter_id, victim_id, people_count, status) VALUES (?, ?, ?, 'Active')";
            try (PreparedStatement regStmt = conn.prepareStatement(regSql)) {
                regStmt.setInt(1, shelterId);
                regStmt.setInt(2, victimId);
                regStmt.setInt(3, peopleCount);
                regStmt.executeUpdate();
            }

            // 4. Update shelter current_occupancy and status
            int newOcc = currentOcc + peopleCount;
            String newStatus = (newOcc >= totalCap) ? "Full" : "Open";
            String updateSql = "UPDATE shelters SET current_occupancy = ?, status = ? WHERE shelter_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, newOcc);
                updateStmt.setString(2, newStatus);
                updateStmt.setInt(3, shelterId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new DatabaseException("Transaction failed during shelter check-in: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Atomically checks out evacuees and decrements shelter occupancy.
     */
    public boolean checkOutVictim(int registrationId) throws DatabaseException {
        Connection conn = null;
        try {
            conn = db.createNewConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int shelterId = -1;
            int peopleCount = 0;

            // 1. Fetch active registration
            String fetchSql = "SELECT shelter_id, people_count FROM shelter_registrations WHERE registration_id = ? AND status = 'Active'";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                fetchStmt.setInt(1, registrationId);
                try (ResultSet rs = fetchStmt.executeQuery()) {
                    if (rs.next()) {
                        shelterId = rs.getInt("shelter_id");
                        peopleCount = rs.getInt("people_count");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Mark registration Checked Out
            String regUpdate = "UPDATE shelter_registrations SET status = 'Checked Out', check_out_time = NOW() WHERE registration_id = ?";
            try (PreparedStatement regStmt = conn.prepareStatement(regUpdate)) {
                regStmt.setInt(1, registrationId);
                regStmt.executeUpdate();
            }

            // 3. Decrement occupancy and update shelter status
            String shelterUpdate = "UPDATE shelters SET current_occupancy = GREATEST(0, current_occupancy - ?), " +
                                  "status = CASE WHEN status = 'Full' THEN 'Open' ELSE status END WHERE shelter_id = ?";
            try (PreparedStatement sStmt = conn.prepareStatement(shelterUpdate)) {
                sStmt.setInt(1, peopleCount);
                sStmt.setInt(2, shelterId);
                sStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new DatabaseException("Transaction failed during shelter check-out: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<ShelterRegistration> findAllRegistrations() throws DatabaseException {
        List<ShelterRegistration> list = new ArrayList<>();
        String sql = "SELECT sr.*, s.shelter_name, v.full_name AS victim_name " +
                     "FROM shelter_registrations sr " +
                     "JOIN shelters s ON sr.shelter_id = s.shelter_id " +
                     "JOIN victims v ON sr.victim_id = v.victim_id " +
                     "ORDER BY sr.status ASC, sr.check_in_time DESC";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ShelterRegistration(
                        rs.getInt("registration_id"),
                        rs.getInt("shelter_id"),
                        rs.getString("shelter_name"),
                        rs.getInt("victim_id"),
                        rs.getString("victim_name"),
                        rs.getInt("people_count"),
                        rs.getTimestamp("check_in_time"),
                        rs.getTimestamp("check_out_time"),
                        rs.getString("status")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve shelter registrations: " + e.getMessage(), e);
        }
    }

    public int getTotalShelterCapacity() throws DatabaseException {
        String sql = "SELECT SUM(total_capacity) FROM shelters WHERE status != 'Closed'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate total capacity: " + e.getMessage(), e);
        }
    }

    public int getCurrentShelterOccupancy() throws DatabaseException {
        String sql = "SELECT SUM(current_occupancy) FROM shelters WHERE status != 'Closed'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate total occupancy: " + e.getMessage(), e);
        }
    }

    private Shelter mapRowToShelter(ResultSet rs) throws SQLException {
        return new Shelter(
                rs.getInt("shelter_id"),
                rs.getString("shelter_name"),
                rs.getString("location"),
                rs.getInt("total_capacity"),
                rs.getInt("current_occupancy"),
                rs.getString("contact_number"),
                rs.getString("status")
        );
    }
}
