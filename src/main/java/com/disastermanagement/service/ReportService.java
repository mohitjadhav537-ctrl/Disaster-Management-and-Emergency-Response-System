package com.disastermanagement.service;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.DatabaseException;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;

/**
 * Service generating analytical reports and summary tables across relational joins,
 * aggregates, and subqueries.
 */
public class ReportService {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Executes a SQL query and transforms it directly into a Swing DefaultTableModel
     * suitable for display in JTable or export via CSVExporter.
     */
    public DefaultTableModel executeReportQuery(String sql) throws DatabaseException {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                colNames.add(meta.getColumnLabel(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            return new DefaultTableModel(data, colNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

        } catch (Exception e) {
            throw new DatabaseException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    public DefaultTableModel getIncidentsBySeverityAndTypeReport() throws DatabaseException {
        String sql = "SELECT disaster_type AS 'Disaster Type', " +
                "COUNT(*) AS 'Total Incidents', " +
                "SUM(CASE WHEN severity = 'Critical' THEN 1 ELSE 0 END) AS 'Critical', " +
                "SUM(CASE WHEN severity = 'High' THEN 1 ELSE 0 END) AS 'High', " +
                "SUM(CASE WHEN severity = 'Medium' THEN 1 ELSE 0 END) AS 'Medium', " +
                "SUM(CASE WHEN severity = 'Low' THEN 1 ELSE 0 END) AS 'Low', " +
                "SUM(people_affected) AS 'Total People Affected' " +
                "FROM disasters GROUP BY disaster_type ORDER BY 'Total Incidents' DESC";
        return executeReportQuery(sql);
    }

    public DefaultTableModel getRescueTeamAssignmentsReport() throws DatabaseException {
        String sql = "SELECT a.assignment_id AS 'Assignment ID', " +
                "d.title AS 'Disaster Incident', " +
                "d.severity AS 'Severity', " +
                "t.team_name AS 'Rescue Team', " +
                "t.team_leader AS 'Leader', " +
                "t.contact_number AS 'Contact', " +
                "u.full_name AS 'Assigned By', " +
                "a.assigned_at AS 'Assigned At', " +
                "a.assignment_status AS 'Status' " +
                "FROM assignments a " +
                "JOIN disasters d ON a.disaster_id = d.disaster_id " +
                "JOIN rescue_teams t ON a.team_id = t.team_id " +
                "JOIN users u ON a.assigned_by = u.user_id " +
                "ORDER BY a.assigned_at DESC";
        return executeReportQuery(sql);
    }

    public DefaultTableModel getResourceDistributionSummaryReport() throws DatabaseException {
        String sql = "SELECT d.title AS 'Disaster Incident', " +
                "r.resource_name AS 'Resource Distributed', " +
                "r.category AS 'Category', " +
                "SUM(rd.quantity_distributed) AS 'Total Distributed', " +
                "r.unit AS 'Unit', " +
                "COUNT(rd.distribution_id) AS 'Shipment Count', " +
                "MAX(rd.distribution_date) AS 'Latest Distribution' " +
                "FROM resource_distributions rd " +
                "JOIN disasters d ON rd.disaster_id = d.disaster_id " +
                "JOIN resources r ON rd.resource_id = r.resource_id " +
                "GROUP BY d.title, r.resource_name, r.category, r.unit " +
                "ORDER BY d.title ASC, 'Total Distributed' DESC";
        return executeReportQuery(sql);
    }

    public DefaultTableModel getShelterOccupancyReport() throws DatabaseException {
        String sql = "SELECT s.shelter_name AS 'Shelter Name', " +
                "s.location AS 'Location', " +
                "s.total_capacity AS 'Total Capacity', " +
                "s.current_occupancy AS 'Current Occupancy', " +
                "(s.total_capacity - s.current_occupancy) AS 'Available Space', " +
                "CONCAT(ROUND((s.current_occupancy / s.total_capacity) * 100, 1), '%') AS 'Occupancy Rate', " +
                "s.status AS 'Shelter Status' " +
                "FROM shelters s ORDER BY (s.current_occupancy / s.total_capacity) DESC";
        return executeReportQuery(sql);
    }

    public DefaultTableModel getHelpRequestsSummaryReport() throws DatabaseException {
        String sql = "SELECT hr.request_type AS 'Request Type', " +
                "COUNT(*) AS 'Total Requests', " +
                "SUM(CASE WHEN hr.status = 'Pending' THEN 1 ELSE 0 END) AS 'Pending', " +
                "SUM(CASE WHEN hr.status = 'In Progress' THEN 1 ELSE 0 END) AS 'In Progress', " +
                "SUM(CASE WHEN hr.status = 'Resolved' THEN 1 ELSE 0 END) AS 'Resolved', " +
                "SUM(CASE WHEN hr.priority = 'Critical' THEN 1 ELSE 0 END) AS 'Critical Requests' " +
                "FROM help_requests hr GROUP BY hr.request_type";
        return executeReportQuery(sql);
    }

    public DefaultTableModel getLowStockInventoryReport() throws DatabaseException {
        String sql = "SELECT r.resource_name AS 'Resource Name', " +
                "r.category AS 'Category', " +
                "r.quantity AS 'Current Stock', " +
                "r.minimum_stock AS 'Threshold Minimum', " +
                "r.unit AS 'Unit', " +
                "r.storage_location AS 'Depot Location', " +
                "CASE WHEN r.quantity = 0 THEN 'OUT OF STOCK' ELSE 'LOW STOCK WARNING' END AS 'Urgency Status' " +
                "FROM resources r WHERE r.quantity <= r.minimum_stock " +
                "ORDER BY r.quantity ASC";
        return executeReportQuery(sql);
    }
}
