package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.Victim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for victims table.
 */
public class VictimDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(Victim victim) throws DatabaseException {
        String sql = "INSERT INTO victims (disaster_id, full_name, age, contact_number, group_size, location, assistance_required, medical_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, victim.getDisasterId());
            ps.setString(2, victim.getFullName());
            ps.setInt(3, victim.getAge());
            ps.setString(4, victim.getContactNumber());
            ps.setInt(5, victim.getGroupSize());
            ps.setString(6, victim.getLocation());
            ps.setString(7, victim.getAssistanceRequired());
            ps.setString(8, victim.getMedicalStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        victim.setVictimId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register victim: " + e.getMessage(), e);
        }
    }

    public boolean update(Victim victim) throws DatabaseException {
        String sql = "UPDATE victims SET disaster_id = ?, full_name = ?, age = ?, contact_number = ?, " +
                     "group_size = ?, location = ?, assistance_required = ?, medical_status = ? WHERE victim_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victim.getDisasterId());
            ps.setString(2, victim.getFullName());
            ps.setInt(3, victim.getAge());
            ps.setString(4, victim.getContactNumber());
            ps.setInt(5, victim.getGroupSize());
            ps.setString(6, victim.getLocation());
            ps.setString(7, victim.getAssistanceRequired());
            ps.setString(8, victim.getMedicalStatus());
            ps.setInt(9, victim.getVictimId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update victim details: " + e.getMessage(), e);
        }
    }

    public boolean delete(int victimId) throws DatabaseException {
        String sql = "DELETE FROM victims WHERE victim_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete victim referenced in help requests or shelters: " + e.getMessage(), e);
        }
    }

    public Optional<Victim> findById(int victimId) throws DatabaseException {
        String sql = "SELECT v.*, d.title AS disaster_title FROM victims v " +
                     "JOIN disasters d ON v.disaster_id = d.disaster_id WHERE v.victim_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, victimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToVictim(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query victim: " + e.getMessage(), e);
        }
    }

    public List<Victim> findAll() throws DatabaseException {
        List<Victim> list = new ArrayList<>();
        String sql = "SELECT v.*, d.title AS disaster_title FROM victims v " +
                     "JOIN disasters d ON v.disaster_id = d.disaster_id ORDER BY v.registered_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToVictim(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve victims list: " + e.getMessage(), e);
        }
    }

    public List<Victim> findByDisasterId(int disasterId) throws DatabaseException {
        List<Victim> list = new ArrayList<>();
        String sql = "SELECT v.*, d.title AS disaster_title FROM victims v " +
                     "JOIN disasters d ON v.disaster_id = d.disaster_id WHERE v.disaster_id = ? " +
                     "ORDER BY v.registered_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disasterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToVictim(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve victims by disaster: " + e.getMessage(), e);
        }
    }

    public int countTotal() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM victims";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count victims: " + e.getMessage(), e);
        }
    }

    private Victim mapRowToVictim(ResultSet rs) throws SQLException {
        return new Victim(
                rs.getInt("victim_id"),
                rs.getInt("disaster_id"),
                rs.getString("disaster_title"),
                rs.getString("full_name"),
                rs.getInt("age"),
                rs.getString("contact_number"),
                rs.getInt("group_size"),
                rs.getString("location"),
                rs.getString("assistance_required"),
                rs.getString("medical_status"),
                rs.getTimestamp("registered_at")
        );
    }
}
