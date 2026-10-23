package com.disastermanagement.dao;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.InsufficientResourceException;
import com.disastermanagement.model.Resource;
import com.disastermanagement.model.ResourceDistribution;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for resources and resource_distributions tables.
 * Employs ACID database transactions to prevent negative inventory and ensure distribution consistency.
 */
public class ResourceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean insert(Resource res) throws DatabaseException {
        String sql = "INSERT INTO resources (resource_name, category, quantity, unit, storage_location, minimum_stock) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, res.getResourceName());
            ps.setString(2, res.getCategory());
            ps.setInt(3, res.getQuantity());
            ps.setString(4, res.getUnit());
            ps.setString(5, res.getStorageLocation());
            ps.setInt(6, res.getMinimumStock());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        res.setResourceId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add resource item: " + e.getMessage(), e);
        }
    }

    public boolean update(Resource res) throws DatabaseException {
        String sql = "UPDATE resources SET resource_name = ?, category = ?, quantity = ?, " +
                     "unit = ?, storage_location = ?, minimum_stock = ? WHERE resource_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, res.getResourceName());
            ps.setString(2, res.getCategory());
            ps.setInt(3, res.getQuantity());
            ps.setString(4, res.getUnit());
            ps.setString(5, res.getStorageLocation());
            ps.setInt(6, res.getMinimumStock());
            ps.setInt(7, res.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update resource item: " + e.getMessage(), e);
        }
    }

    public boolean delete(int resourceId) throws DatabaseException {
        String sql = "DELETE FROM resources WHERE resource_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete resource with distribution history: " + e.getMessage(), e);
        }
    }

    public Optional<Resource> findById(int resourceId) throws DatabaseException {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToResource(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query resource: " + e.getMessage(), e);
        }
    }

    public List<Resource> findAll() throws DatabaseException {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY category ASC, resource_name ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToResource(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve resources: " + e.getMessage(), e);
        }
    }

    public List<Resource> findLowStock() throws DatabaseException {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE quantity <= minimum_stock ORDER BY quantity ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToResource(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve low-stock resources: " + e.getMessage(), e);
        }
    }

    /**
     * Atomically distributes relief supplies to an incident.
     * Guarantees ACID transactional compliance:
     * 1. Locks resource row (FOR UPDATE)
     * 2. Checks available stock vs requested quantity
     * 3. Prevents negative inventory (throws InsufficientResourceException)
     * 4. Deducts stock
     * 5. Inserts distribution audit record
     * 6. Commits atomically
     */
    public boolean distributeResource(int resourceId, int disasterId, int quantity, int distributedBy, String notes)
            throws DatabaseException, InsufficientResourceException {
        
        Connection conn = null;
        try {
            conn = db.createNewConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Lock and inspect current quantity
            String checkSql = "SELECT resource_name, quantity FROM resources WHERE resource_id = ? FOR UPDATE";
            int currentStock = 0;
            String resourceName = "Unknown";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, resourceId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        resourceName = rs.getString("resource_name");
                        currentStock = rs.getInt("quantity");
                    } else {
                        conn.rollback();
                        throw new DatabaseException("Resource not found with ID: " + resourceId);
                    }
                }
            }

            // 2. Validate quantity invariant
            if (currentStock < quantity) {
                conn.rollback();
                throw new InsufficientResourceException(resourceName, quantity, currentStock);
            }

            // 3. Deduct quantity
            String deductSql = "UPDATE resources SET quantity = quantity - ? WHERE resource_id = ?";
            try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                deductStmt.setInt(1, quantity);
                deductStmt.setInt(2, resourceId);
                deductStmt.executeUpdate();
            }

            // 4. Record distribution
            String distSql = "INSERT INTO resource_distributions (resource_id, disaster_id, quantity_distributed, distributed_by, notes) " +
                             "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement distStmt = conn.prepareStatement(distSql)) {
                distStmt.setInt(1, resourceId);
                distStmt.setInt(2, disasterId);
                distStmt.setInt(3, quantity);
                distStmt.setInt(4, distributedBy);
                distStmt.setString(5, notes);
                distStmt.executeUpdate();
            }

            // 5. Commit atomic transaction
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
            throw new DatabaseException("Transaction failed during resource distribution: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<ResourceDistribution> findAllDistributions() throws DatabaseException {
        List<ResourceDistribution> list = new ArrayList<>();
        String sql = "SELECT rd.*, r.resource_name, r.unit, d.title AS disaster_title, u.full_name AS distributed_by_name " +
                     "FROM resource_distributions rd " +
                     "JOIN resources r ON rd.resource_id = r.resource_id " +
                     "JOIN disasters d ON rd.disaster_id = d.disaster_id " +
                     "JOIN users u ON rd.distributed_by = u.user_id " +
                     "ORDER BY rd.distribution_date DESC";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ResourceDistribution(
                        rs.getInt("distribution_id"),
                        rs.getInt("resource_id"),
                        rs.getString("resource_name"),
                        rs.getInt("disaster_id"),
                        rs.getString("disaster_title"),
                        rs.getInt("quantity_distributed"),
                        rs.getString("unit"),
                        rs.getInt("distributed_by"),
                        rs.getString("distributed_by_name"),
                        rs.getTimestamp("distribution_date"),
                        rs.getString("notes")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve resource distributions: " + e.getMessage(), e);
        }
    }

    public int countTotalAvailableUnits() throws DatabaseException {
        String sql = "SELECT SUM(quantity) FROM resources";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count total resources: " + e.getMessage(), e);
        }
    }

    public int countLowStock() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM resources WHERE quantity <= minimum_stock";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count low-stock items: " + e.getMessage(), e);
        }
    }

    private Resource mapRowToResource(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getInt("resource_id"),
                rs.getString("resource_name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                rs.getString("storage_location"),
                rs.getInt("minimum_stock")
        );
    }
}
