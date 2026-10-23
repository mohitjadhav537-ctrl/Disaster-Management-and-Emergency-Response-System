package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.InvalidAssignmentException;
import com.disastermanagement.model.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for assignments table.
 * Implements atomic ACID transactions when allocating or completing rescue assignments.
 */
public class AssignmentDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Atomically assigns a rescue team to an incident.
     * Transaction sequence:
     * 1. Check if team already has an active assignment (Lock/Prevent race condition).
     * 2. Insert record into assignments table.
     * 3. Update rescue_teams availability to 'Busy'.
     * 4. Update disasters status to 'Response in Progress' (if currently 'Reported' or 'Verified').
     * Commits on success; Rolls back on failure.
     */
    public boolean createAssignment(int disasterId, int teamId, int assignedBy) 
            throws DatabaseException, InvalidAssignmentException {
        
        Connection conn = null;
        try {
            conn = db.createNewConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 1. Check if team already has an active assignment
            String checkSql = "SELECT COUNT(*) FROM assignments WHERE team_id = ? AND assignment_status = 'Active'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, teamId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        throw new InvalidAssignmentException("Rescue team is already actively assigned to another incident!", disasterId, teamId);
                    }
                }
            }

            // 2. Insert assignment
            String insertSql = "INSERT INTO assignments (disaster_id, team_id, assigned_by, assignment_status) VALUES (?, ?, ?, 'Active')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, disasterId);
                insertStmt.setInt(2, teamId);
                insertStmt.setInt(3, assignedBy);
                insertStmt.executeUpdate();
            }

            // 3. Mark team as Busy
            String teamSql = "UPDATE rescue_teams SET availability = 'Busy' WHERE team_id = ?";
            try (PreparedStatement teamStmt = conn.prepareStatement(teamSql)) {
                teamStmt.setInt(1, teamId);
                teamStmt.executeUpdate();
            }

            // 4. Update incident status to Response in Progress if not resolved
            String disasterSql = "UPDATE disasters SET status = 'Response in Progress' WHERE disaster_id = ? AND status IN ('Reported', 'Verified')";
            try (PreparedStatement disasterStmt = conn.prepareStatement(disasterSql)) {
                disasterStmt.setInt(1, disasterId);
                disasterStmt.executeUpdate();
            }

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Safe rollback
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new DatabaseException("Transaction failed during team assignment: " + e.getMessage(), e);
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
     * Atomically completes an assignment and frees up the rescue team.
     */
    public boolean completeAssignment(int assignmentId) throws DatabaseException {
        Connection conn = null;
        try {
            conn = db.createNewConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            int teamId = -1;
            // 1. Fetch team ID from assignment
            String fetchSql = "SELECT team_id FROM assignments WHERE assignment_id = ? AND assignment_status = 'Active'";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                fetchStmt.setInt(1, assignmentId);
                try (ResultSet rs = fetchStmt.executeQuery()) {
                    if (rs.next()) {
                        teamId = rs.getInt("team_id");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Mark assignment Completed
            String updateSql = "UPDATE assignments SET assignment_status = 'Completed', completed_at = NOW() WHERE assignment_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, assignmentId);
                updateStmt.executeUpdate();
            }

            // 3. Reset team availability to Available
            if (teamId > 0) {
                String teamSql = "UPDATE rescue_teams SET availability = 'Available' WHERE team_id = ?";
                try (PreparedStatement teamStmt = conn.prepareStatement(teamSql)) {
                    teamStmt.setInt(1, teamId);
                    teamStmt.executeUpdate();
                }
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
            throw new DatabaseException("Transaction failed while completing assignment: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<Assignment> findAllWithDetails() throws DatabaseException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.disaster_id, d.title AS disaster_title, d.severity AS disaster_severity, " +
                     "a.team_id, t.team_name, a.assigned_by, u.full_name AS assigned_by_name, " +
                     "a.assigned_at, a.assignment_status, a.completed_at " +
                     "FROM assignments a " +
                     "JOIN disasters d ON a.disaster_id = d.disaster_id " +
                     "JOIN rescue_teams t ON a.team_id = t.team_id " +
                     "JOIN users u ON a.assigned_by = u.user_id " +
                     "ORDER BY a.assignment_status ASC, a.assigned_at DESC";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToAssignment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query assignments with details: " + e.getMessage(), e);
        }
    }

    public List<Assignment> findActiveAssignments() throws DatabaseException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.disaster_id, d.title AS disaster_title, d.severity AS disaster_severity, " +
                     "a.team_id, t.team_name, a.assigned_by, u.full_name AS assigned_by_name, " +
                     "a.assigned_at, a.assignment_status, a.completed_at " +
                     "FROM assignments a " +
                     "JOIN disasters d ON a.disaster_id = d.disaster_id " +
                     "JOIN rescue_teams t ON a.team_id = t.team_id " +
                     "JOIN users u ON a.assigned_by = u.user_id " +
                     "WHERE a.assignment_status = 'Active' " +
                     "ORDER BY a.assigned_at DESC";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToAssignment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query active assignments: " + e.getMessage(), e);
        }
    }

    public boolean hasActiveAssignment(int teamId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM assignments WHERE team_id = ? AND assignment_status = 'Active'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check active assignment: " + e.getMessage(), e);
        }
    }

    private Assignment mapRowToAssignment(ResultSet rs) throws SQLException {
        return new Assignment(
                rs.getInt("assignment_id"),
                rs.getInt("disaster_id"),
                rs.getString("disaster_title"),
                rs.getString("disaster_severity"),
                rs.getInt("team_id"),
                rs.getString("team_name"),
                rs.getInt("assigned_by"),
                rs.getString("assigned_by_name"),
                rs.getTimestamp("assigned_at"),
                rs.getString("assignment_status"),
                rs.getTimestamp("completed_at")
        );
    }
}
