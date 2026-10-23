package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for audit_logs table.
 */
public class AuditLogDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public void log(Integer userId, String action, String entityType, Integer entityId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null && userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId != null) {
                ps.setInt(4, entityId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[AUDIT ERROR] Failed to record audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> findAll() throws DatabaseException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.username, u.full_name AS user_full_name " +
                     "FROM audit_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.user_id " +
                     "ORDER BY al.action_timestamp DESC LIMIT 200";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("user_full_name");
                if (name == null) name = "System";
                list.add(new AuditLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        name,
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getInt("entity_id"),
                        rs.getTimestamp("action_timestamp"),
                        rs.getString("details")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve audit logs: " + e.getMessage(), e);
        }
    }
}
