package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.Disaster;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for disasters table.
 */
public class DisasterDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(Disaster disaster) throws DatabaseException {
        String sql = "INSERT INTO disasters (title, disaster_type, description, location, severity, people_affected, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, disaster.getTitle());
            ps.setString(2, disaster.getDisasterType());
            ps.setString(3, disaster.getDescription());
            ps.setString(4, disaster.getLocation());
            ps.setString(5, disaster.getSeverity());
            ps.setInt(6, disaster.getPeopleAffected());
            ps.setString(7, disaster.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        disaster.setDisasterId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert disaster incident: " + e.getMessage(), e);
        }
    }

    public boolean update(Disaster disaster) throws DatabaseException {
        String sql = "UPDATE disasters SET title = ?, disaster_type = ?, description = ?, location = ?, " +
                     "severity = ?, people_affected = ?, status = ? WHERE disaster_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, disaster.getTitle());
            ps.setString(2, disaster.getDisasterType());
            ps.setString(3, disaster.getDescription());
            ps.setString(4, disaster.getLocation());
            ps.setString(5, disaster.getSeverity());
            ps.setInt(6, disaster.getPeopleAffected());
            ps.setString(7, disaster.getStatus());
            ps.setInt(8, disaster.getDisasterId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update disaster incident: " + e.getMessage(), e);
        }
    }

    public boolean delete(int disasterId) throws DatabaseException {
        // Enforce referential integrity: Check if assignments, victims, or resources are attached
        if (hasReferences(disasterId)) {
            // Soft delete / close to preserve audit history
            return closeIncident(disasterId);
        }

        String sql = "DELETE FROM disasters WHERE disaster_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disasterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete disaster incident: " + e.getMessage(), e);
        }
    }

    public boolean closeIncident(int disasterId) throws DatabaseException {
        String sql = "UPDATE disasters SET status = 'Closed' WHERE disaster_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disasterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to close disaster incident: " + e.getMessage(), e);
        }
    }

    public boolean hasReferences(int disasterId) throws DatabaseException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM assignments WHERE disaster_id = ?) + " +
                "(SELECT COUNT(*) FROM victims WHERE disaster_id = ?) + " +
                "(SELECT COUNT(*) FROM resource_distributions WHERE disaster_id = ?) AS total_refs";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disasterId);
            ps.setInt(2, disasterId);
            ps.setInt(3, disasterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total_refs") > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to verify disaster references: " + e.getMessage(), e);
        }
    }

    public Optional<Disaster> findById(int disasterId) throws DatabaseException {
        String sql = "SELECT * FROM disasters WHERE disaster_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disasterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDisaster(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query disaster by ID: " + e.getMessage(), e);
        }
    }

    public List<Disaster> findAll() throws DatabaseException {
        List<Disaster> list = new ArrayList<>();
        String sql = "SELECT * FROM disasters ORDER BY " +
                "CASE severity WHEN 'Critical' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 WHEN 'Low' THEN 4 END, " +
                "reported_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToDisaster(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all disasters: " + e.getMessage(), e);
        }
    }

    public List<Disaster> findActiveDisasters() throws DatabaseException {
        List<Disaster> list = new ArrayList<>();
        String sql = "SELECT * FROM disasters WHERE status NOT IN ('Resolved', 'Closed') ORDER BY " +
                "CASE severity WHEN 'Critical' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 WHEN 'Low' THEN 4 END, " +
                "reported_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToDisaster(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve active disasters: " + e.getMessage(), e);
        }
    }

    public List<Disaster> search(String query, String typeFilter, String severityFilter, String statusFilter) throws DatabaseException {
        List<Disaster> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM disasters WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR location LIKE ? OR description LIKE ?) ");
            String wild = "%" + query.trim() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }
        if (typeFilter != null && !typeFilter.equalsIgnoreCase("All")) {
            sql.append("AND disaster_type = ? ");
            params.add(typeFilter);
        }
        if (severityFilter != null && !severityFilter.equalsIgnoreCase("All")) {
            sql.append("AND severity = ? ");
            params.add(severityFilter);
        }
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All")) {
            sql.append("AND status = ? ");
            params.add(statusFilter);
        }

        sql.append("ORDER BY CASE severity WHEN 'Critical' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 WHEN 'Low' THEN 4 END, reported_at DESC");

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDisaster(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search disaster incidents: " + e.getMessage(), e);
        }
    }

    public int countTotal() throws DatabaseException {
        return executeCount("SELECT COUNT(*) FROM disasters");
    }

    public int countActive() throws DatabaseException {
        return executeCount("SELECT COUNT(*) FROM disasters WHERE status NOT IN ('Resolved', 'Closed')");
    }

    public int countCritical() throws DatabaseException {
        return executeCount("SELECT COUNT(*) FROM disasters WHERE severity = 'Critical' AND status NOT IN ('Resolved', 'Closed')");
    }

    public Map<String, Integer> getSeveritySummary() throws DatabaseException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT severity, COUNT(*) as count FROM disasters GROUP BY severity";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("severity"), rs.getInt("count"));
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to summarize by severity: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getTypeSummary() throws DatabaseException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT disaster_type, COUNT(*) as count FROM disasters GROUP BY disaster_type";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("disaster_type"), rs.getInt("count"));
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to summarize by disaster type: " + e.getMessage(), e);
        }
    }

    private int executeCount(String sql) throws DatabaseException {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute count query: " + e.getMessage(), e);
        }
    }

    private Disaster mapRowToDisaster(ResultSet rs) throws SQLException {
        return new Disaster(
                rs.getInt("disaster_id"),
                rs.getString("title"),
                rs.getString("disaster_type"),
                rs.getString("description"),
                rs.getString("location"),
                rs.getString("severity"),
                rs.getInt("people_affected"),
                rs.getString("status"),
                rs.getTimestamp("reported_at"),
                rs.getTimestamp("updated_at")
        );
    }
}
