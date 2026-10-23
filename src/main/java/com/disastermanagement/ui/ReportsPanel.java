package com.disastermanagement.ui;

import com.disastermanagement.model.User;
import com.disastermanagement.service.ReportService;
import com.disastermanagement.util.CSVExporter;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Analytical Reports & Export Panel.
 * Generates relational aggregations, multi-table joins, and CSV downloads.
 */
public class ReportsPanel extends JPanel {

    private final User currentUser;
    private final ReportService reportService = new ReportService();

    private JComboBox<String> reportTypeBox;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel recordCountLabel;

    public ReportsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadSelectedReport();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UITheme.BG_MAIN);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Emergency Management Reports & Relational Analytics");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JButton exportCsvBtn = UITheme.createSuccessButton("📥 Export Report to CSV");
        exportCsvBtn.addActionListener(e -> exportToCSV());

        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(exportCsvBtn, BorderLayout.EAST);
        topPanel.add(titleRow);
        topPanel.add(Box.createVerticalStrut(14));

        // Filter / Selector Card
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 6));

        JLabel selectLbl = new JLabel("Select Analytical Report Query:");
        selectLbl.setFont(UITheme.FONT_BODY_BOLD);
        selectLbl.setForeground(UITheme.PRIMARY_NAVY);

        String[] reportOptions = {
                "1. Incidents by Disaster Type & Severity (GROUP BY & Aggregates)",
                "2. Active Rescue Team Deployments (Multi-Table Relational JOIN)",
                "3. Relief Supplies Distributed per Incident (SUM & GROUP BY)",
                "4. Shelter Capacity & Occupancy Utilization (Calculated Metrics)",
                "5. Emergency Help Requests by Category (Status Breakdown)",
                "6. Low-Stock Inventory Alert Report (Threshold Filter)"
        };

        reportTypeBox = new JComboBox<>(reportOptions);
        reportTypeBox.setFont(UITheme.FONT_BODY);
        reportTypeBox.setPreferredSize(new Dimension(520, 32));
        reportTypeBox.addActionListener(e -> loadSelectedReport());

        JButton runQueryBtn = UITheme.createPrimaryButton("Run Analytical Query");
        runQueryBtn.addActionListener(e -> loadSelectedReport());

        filterCard.add(selectLbl);
        filterCard.add(reportTypeBox);
        filterCard.add(runQueryBtn);

        topPanel.add(filterCard);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        UITheme.styleTable(reportTable);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom status summary
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Color.WHITE);

        recordCountLabel = new JLabel("Records Loaded: 0");
        recordCountLabel.setFont(UITheme.FONT_BODY_BOLD);
        recordCountLabel.setForeground(UITheme.TEXT_MUTED);
        bottomRow.add(recordCountLabel, BorderLayout.WEST);

        tableCard.add(bottomRow, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private void loadSelectedReport() {
        int index = reportTypeBox.getSelectedIndex();
        try {
            DefaultTableModel model = switch (index) {
                case 0 -> reportService.getIncidentsBySeverityAndTypeReport();
                case 1 -> reportService.getRescueTeamAssignmentsReport();
                case 2 -> reportService.getResourceDistributionSummaryReport();
                case 3 -> reportService.getShelterOccupancyReport();
                case 4 -> reportService.getHelpRequestsSummaryReport();
                case 5 -> reportService.getLowStockInventoryReport();
                default -> new DefaultTableModel();
            };

            reportTable.setModel(model);
            UITheme.styleTable(reportTable);
            recordCountLabel.setText("Records Loaded: " + model.getRowCount() + " rows");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Query execution error: " + e.getMessage(), "Report Query Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCSV() {
        if (reportTable.getModel().getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report rows to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String defaultName = "Disaster_Report_" + System.currentTimeMillis() + ".csv";
        CSVExporter.exportTableToCSV(this, reportTable, defaultName);
    }
}
