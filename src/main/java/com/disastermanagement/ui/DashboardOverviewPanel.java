package com.disastermanagement.ui;

import com.disastermanagement.dao.*;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.User;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Overview / Dashboard panel featuring KPI summary cards, active incident monitor,
 * and emergency status indicators.
 */
public class DashboardOverviewPanel extends JPanel {

    private final User currentUser;
    private final DisasterDAO disasterDAO = new DisasterDAO();
    private final RescueTeamDAO teamDAO = new RescueTeamDAO();
    private final HelpRequestDAO helpDAO = new HelpRequestDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ShelterDAO shelterDAO = new ShelterDAO();
    private final VictimDAO victimDAO = new VictimDAO();

    private JPanel kpiGrid;
    private JTable recentIncidentsTable;
    private DefaultTableModel tableModel;
    private JLabel alertBannerLabel;

    public DashboardOverviewPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Welcome & Alert Banner
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(UITheme.BG_MAIN);

        JPanel welcomeRow = new JPanel(new BorderLayout());
        welcomeRow.setBackground(UITheme.BG_MAIN);

        JLabel welcomeLabel = new JLabel("Emergency Command & Coordination Center");
        welcomeLabel.setFont(UITheme.FONT_HEADER_LARGE);
        welcomeLabel.setForeground(UITheme.PRIMARY_NAVY);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh Metrics");
        refreshBtn.addActionListener(e -> refreshData());

        welcomeRow.add(welcomeLabel, BorderLayout.WEST);
        welcomeRow.add(refreshBtn, BorderLayout.EAST);
        topContainer.add(welcomeRow);
        topContainer.add(Box.createVerticalStrut(12));

        // Alert Banner
        JPanel alertBanner = new JPanel(new BorderLayout());
        alertBanner.setBackground(new Color(254, 242, 242));
        alertBanner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(248, 113, 113), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        alertBannerLabel = new JLabel("Status: System operational. Monitoring live disaster reports.");
        alertBannerLabel.setFont(UITheme.FONT_BODY_BOLD);
        alertBannerLabel.setForeground(UITheme.ACCENT_RED);
        alertBanner.add(alertBannerLabel, BorderLayout.CENTER);

        topContainer.add(alertBanner);
        topContainer.add(Box.createVerticalStrut(16));

        // KPI Summary Cards Grid (3x3 or 4x2 responsive layout)
        kpiGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        kpiGrid.setBackground(UITheme.BG_MAIN);
        topContainer.add(kpiGrid);

        add(topContainer, BorderLayout.NORTH);

        // Center: Active Critical/High Incidents Monitor Table
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(8, 8));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(Color.WHITE);
        JLabel tableTitle = new JLabel("Active Disaster Incidents (Priority Response Order)");
        tableTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        tableTitle.setForeground(UITheme.PRIMARY_NAVY);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Disaster Incident", "Type", "Severity", "Location", "People Affected", "Status", "Reported At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentIncidentsTable = new JTable(tableModel);
        UITheme.styleTable(recentIncidentsTable);
        JScrollPane scrollPane = new JScrollPane(recentIncidentsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            int totalIncidents = disasterDAO.countTotal();
            int activeIncidents = disasterDAO.countActive();
            int criticalIncidents = disasterDAO.countCritical();
            int availableTeams = teamDAO.countAvailable();
            int busyTeams = teamDAO.countBusy();
            int pendingRequests = helpDAO.countPending();
            int victimsCount = victimDAO.countTotal();
            int lowStock = resourceDAO.countLowStock();
            int totalShelterCap = shelterDAO.getTotalShelterCapacity();
            int curShelterOcc = shelterDAO.getCurrentShelterOccupancy();
            int availShelterSpace = Math.max(0, totalShelterCap - curShelterOcc);

            // Update Alert Banner
            if (criticalIncidents > 0) {
                alertBannerLabel.setText(String.format("CRITICAL ALERT: %d Critical disaster incidents require immediate rescue team mobilization!", criticalIncidents));
            } else if (activeIncidents > 0) {
                alertBannerLabel.setText(String.format("ATTENTION: %d Active emergency incidents undergoing response operations.", activeIncidents));
            } else {
                alertBannerLabel.setText("STATUS: All disaster incidents are currently resolved or stabilized.");
            }

            // Repopulate KPI Cards
            kpiGrid.removeAll();
            kpiGrid.add(createKPICard("Active Incidents", String.valueOf(activeIncidents), "Total: " + totalIncidents, UITheme.ACCENT_RED));
            kpiGrid.add(createKPICard("Critical Disasters", String.valueOf(criticalIncidents), "Highest Priority", UITheme.ACCENT_ORANGE));
            kpiGrid.add(createKPICard("Available Rescue Teams", String.valueOf(availableTeams), "Busy: " + busyTeams, UITheme.ACCENT_GREEN));
            kpiGrid.add(createKPICard("Pending Help Requests", String.valueOf(pendingRequests), "Distress Calls", UITheme.ACCENT_PURPLE));
            kpiGrid.add(createKPICard("Registered Victims", String.valueOf(victimsCount), "Across All Zones", UITheme.SECONDARY_NAVY));
            kpiGrid.add(createKPICard("Low-Stock Alerts", String.valueOf(lowStock), "Threshold Warning", lowStock > 0 ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN));
            kpiGrid.add(createKPICard("Shelter Occupancy", String.format("%d / %d", curShelterOcc, totalShelterCap), "Available: " + availShelterSpace, UITheme.ACCENT_BLUE));
            kpiGrid.add(createKPICard("System Role Access", currentUser.getRole(), currentUser.getUsername(), UITheme.PRIMARY_NAVY));

            kpiGrid.revalidate();
            kpiGrid.repaint();

            // Populate Table
            tableModel.setRowCount(0);
            List<Disaster> activeDisasters = disasterDAO.findActiveDisasters();
            for (Disaster d : activeDisasters) {
                tableModel.addRow(new Object[]{
                        d.getDisasterId(),
                        d.getTitle(),
                        d.getDisasterType(),
                        d.getSeverity(),
                        d.getLocation(),
                        d.getPeopleAffected(),
                        d.getStatus(),
                        d.getReportedAt()
                });
            }

        } catch (Exception e) {
            alertBannerLabel.setText("Database Connection Offline: " + e.getMessage());
        }
    }

    private JPanel createKPICard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Left accent indicator stripe
        JPanel stripe = new JPanel();
        stripe.setBackground(accentColor);
        stripe.setPreferredSize(new Dimension(5, 45));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(UITheme.FONT_KPI_VALUE);
        valLbl.setForeground(UITheme.TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(accentColor.darker());

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(2));
        content.add(valLbl);
        content.add(Box.createVerticalStrut(2));
        content.add(subLbl);

        card.add(stripe, BorderLayout.WEST);
        card.add(Box.createHorizontalStrut(8), BorderLayout.CENTER);
        card.add(content, BorderLayout.EAST);
        return card;
    }
}
