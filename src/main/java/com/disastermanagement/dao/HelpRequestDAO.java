package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.HelpRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for help_requests table.
 */
public class HelpRequestDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(HelpRequest request) throws DatabaseException {
        String sql = "INSERT INTO help_requests (victim_id, disaster_id, request_type, priority, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getVictimId());
            ps.setInt(2, request.getDisasterId());
            ps.setString(3, request.getRequestType());
            ps.setString(4, request.getPriority());
            ps.setString(5, request.getDescription());
            ps.setString(6, request.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        request.setRequestId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to submit emergency help request: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int requestId, String status, Integer teamId) throws DatabaseException {
        String sql = "UPDATE help_requests SET status = ?, assigned_team_id = ?, " +
                     "resolved_at = CASE WHEN ? = 'Resolved' THEN NOW() ELSE resolved_at END WHERE request_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (teamId != null && teamId > 0) {
                ps.setInt(2, teamId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, status);
            ps.setInt(4, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update help request status: " + e.getMessage(), e);
        }
    }

    public List<HelpRequest> findAllWithDetails() throws DatabaseException {
        List<HelpRequest> list = new ArrayList<>();
        String sql = "SELECT hr.*, v.full_name AS victim_name, d.title AS disaster_title, t.team_name AS assigned_team_name " +
                     "FROM help_requests hr " +
                     "JOIN victims v ON hr.victim_id = v.victim_id " +
                     "JOIN disasters d ON hr.disaster_id = d.disaster_id " +
                     "LEFT JOIN rescue_teams t ON hr.assigned_team_id = t.team_id " +
                     "ORDER BY CASE hr.priority WHEN 'Critical' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 WHEN 'Low' THEN 4 END, " +
                     "hr.created_at DESC";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToHelpRequest(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve help requests: " + e.getMessage(), e);
        }
    }

    public int countPending() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM help_requests WHERE status IN ('Pending', 'Assigned')";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count pending help requests: " + e.getMessage(), e);
        }
    }

    private HelpRequest mapRowToHelpRequest(ResultSet rs) throws SQLException {
        Integer teamId = rs.getInt("assigned_team_id");
        if (rs.wasNull()) teamId = null;

        return new HelpRequest(
                rs.getInt("request_id"),
                rs.getInt("victim_id"),
                rs.getString("victim_name"),
                rs.getInt("disaster_id"),
                rs.getString("disaster_title"),
                rs.getString("request_type"),
                rs.getString("priority"),
                rs.getString("description"),
                rs.getString("status"),
                teamId,
                rs.getString("assigned_team_name"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("resolved_at")
        );
    }
}
