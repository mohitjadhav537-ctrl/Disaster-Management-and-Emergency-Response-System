package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.model.RescueTeam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for rescue_teams table.
 */
public class RescueTeamDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(RescueTeam team) throws DatabaseException {
        String sql = "INSERT INTO rescue_teams (team_name, team_leader, contact_number, team_size, skills, equipment, availability) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getTeamLeader());
            ps.setString(3, team.getContactNumber());
            ps.setInt(4, team.getTeamSize());
            ps.setString(5, team.getSkills());
            ps.setString(6, team.getEquipment());
            ps.setString(7, team.getAvailability());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        team.setTeamId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert rescue team: " + e.getMessage(), e);
        }
    }

    public boolean update(RescueTeam team) throws DatabaseException {
        String sql = "UPDATE rescue_teams SET team_name = ?, team_leader = ?, contact_number = ?, " +
                     "team_size = ?, skills = ?, equipment = ?, availability = ? WHERE team_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getTeamLeader());
            ps.setString(3, team.getContactNumber());
            ps.setInt(4, team.getTeamSize());
            ps.setString(5, team.getSkills());
            ps.setString(6, team.getEquipment());
            ps.setString(7, team.getAvailability());
            ps.setInt(8, team.getTeamId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update rescue team: " + e.getMessage(), e);
        }
    }

    public boolean updateAvailability(int teamId, String availability) throws DatabaseException {
        String sql = "UPDATE rescue_teams SET availability = ? WHERE team_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, availability);
            ps.setInt(2, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update team availability: " + e.getMessage(), e);
        }
    }

    public boolean delete(int teamId) throws DatabaseException {
        String sql = "DELETE FROM rescue_teams WHERE team_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete rescue team with active assignments or history: " + e.getMessage(), e);
        }
    }

    public Optional<RescueTeam> findById(int teamId) throws DatabaseException {
        String sql = "SELECT * FROM rescue_teams WHERE team_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRescueTeam(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query rescue team by ID: " + e.getMessage(), e);
        }
    }

    public List<RescueTeam> findAll() throws DatabaseException {
        List<RescueTeam> list = new ArrayList<>();
        String sql = "SELECT * FROM rescue_teams ORDER BY team_name ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToRescueTeam(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all rescue teams: " + e.getMessage(), e);
        }
    }

    public List<RescueTeam> findAvailableTeams() throws DatabaseException {
        List<RescueTeam> list = new ArrayList<>();
        String sql = "SELECT * FROM rescue_teams WHERE availability = 'Available' ORDER BY team_name ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToRescueTeam(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve available rescue teams: " + e.getMessage(), e);
        }
    }

    public int countAvailable() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM rescue_teams WHERE availability = 'Available'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count available rescue teams: " + e.getMessage(), e);
        }
    }

    public int countBusy() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM rescue_teams WHERE availability = 'Busy'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count busy rescue teams: " + e.getMessage(), e);
        }
    }

    private RescueTeam mapRowToRescueTeam(ResultSet rs) throws SQLException {
        return new RescueTeam(
                rs.getInt("team_id"),
                rs.getString("team_name"),
                rs.getString("team_leader"),
                rs.getString("contact_number"),
                rs.getInt("team_size"),
                rs.getString("skills"),
                rs.getString("equipment"),
                rs.getString("availability")
        );
    }
}
