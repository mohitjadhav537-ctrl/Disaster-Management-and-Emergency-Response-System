package com.disastermanagement.ui;

import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.User;
import com.disastermanagement.service.DisasterService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel managing disaster incidents (CRUD, search, filtering, severity rankings).
 */
public class DisasterPanel extends JPanel {

    private final User currentUser;
    private final DisasterService disasterService = new DisasterService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<String> severityFilter;
    private JComboBox<String> statusFilter;

    public DisasterPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadDisasters();
    }

    private void initComponents() {
        // Header & Toolbar
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UITheme.BG_MAIN);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Disaster Incident Management");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnRow.setBackground(UITheme.BG_MAIN);

        if (currentUser.hasAdminPrivileges() || "Rescue Officer".equalsIgnoreCase(currentUser.getRole())) {
            JButton addBtn = UITheme.createPrimaryButton("+ Register New Incident");
            addBtn.addActionListener(e -> showDisasterDialog(null));
            actionBtnRow.add(addBtn);
        }

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadDisasters());
        actionBtnRow.add(refreshBtn);

        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(actionBtnRow, BorderLayout.EAST);
        topPanel.add(titleRow);
        topPanel.add(Box.createVerticalStrut(14));

        // Filter / Search Toolbar Card
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        searchField = new JTextField(15);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.setToolTipText("Search title, location or description");

        typeFilter = new JComboBox<>(new String[]{"All Types", "Flood", "Earthquake", "Fire", "Cyclone", "Landslide", "Other"});
        severityFilter = new JComboBox<>(new String[]{"All Severities", "Critical", "High", "Medium", "Low"});
        statusFilter = new JComboBox<>(new String[]{"All Statuses", "Reported", "Verified", "Response in Progress", "Resolved", "Closed"});

        JButton searchBtn = UITheme.createPrimaryButton("Search / Filter");
        searchBtn.addActionListener(e -> applyFilter());

        JButton clearBtn = UITheme.createSecondaryButton("Reset Filters");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            severityFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            loadDisasters();
        });

        filterCard.add(new JLabel("Keyword:"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Type:"));
        filterCard.add(typeFilter);
        filterCard.add(new JLabel("Severity:"));
        filterCard.add(severityFilter);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusFilter);
        filterCard.add(searchBtn);
        filterCard.add(clearBtn);

        topPanel.add(filterCard);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] columns = {"ID", "Title", "Type", "Severity", "Location", "People Affected", "Status", "Reported At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Operations Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(Color.WHITE);

        JButton viewBtn = UITheme.createSecondaryButton("View Details");
        viewBtn.addActionListener(e -> viewSelectedIncident());
        bottomBar.add(viewBtn);

        if (currentUser.hasAdminPrivileges() || "Rescue Officer".equalsIgnoreCase(currentUser.getRole())) {
            JButton editBtn = UITheme.createSecondaryButton("Edit Incident");
            editBtn.addActionListener(e -> editSelectedIncident());
            bottomBar.add(editBtn);

            JButton closeBtn = UITheme.createDangerButton("Close / Resolve Incident");
            closeBtn.addActionListener(e -> closeSelectedIncident());
            bottomBar.add(closeBtn);
        }

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private void loadDisasters() {
        try {
            List<Disaster> list = disasterService.getAllDisasters();
            populateTable(list);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load disasters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        try {
            String query = searchField.getText().trim();
            String type = (String) typeFilter.getSelectedItem();
            if ("All Types".equalsIgnoreCase(type)) type = "All";

            String sev = (String) severityFilter.getSelectedItem();
            if ("All Severities".equalsIgnoreCase(sev)) sev = "All";

            String st = (String) statusFilter.getSelectedItem();
            if ("All Statuses".equalsIgnoreCase(st)) st = "All";

            List<Disaster> results = disasterService.searchDisasters(query, type, sev, st);
            populateTable(results);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Disaster> list) {
        tableModel.setRowCount(0);
        for (Disaster d : list) {
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
    }

    private Disaster getSelectedDisaster() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an incident row from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int disasterId = (int) tableModel.getValueAt(row, 0);
        try {
            return disasterService.getDisaster(disasterId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void viewSelectedIncident() {
        Disaster d = getSelectedDisaster();
        if (d != null) {
            String info = String.format("""
                    Incident ID: #%d
                    Title: %s
                    Disaster Type: %s
                    Severity: %s
                    Status: %s
                    Location: %s
                    People Affected: %d
                    Reported At: %s
                    Last Updated: %s
                    
                    Description:
                    %s
                    """, d.getDisasterId(), d.getTitle(), d.getDisasterType(), d.getSeverity(),
                    d.getStatus(), d.getLocation(), d.getPeopleAffected(), d.getReportedAt(), d.getUpdatedAt(),
                    d.getDescription());

            JTextArea ta = new JTextArea(info, 12, 40);
            ta.setEditable(false);
            ta.setFont(UITheme.FONT_BODY);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Incident Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editSelectedIncident() {
        Disaster d = getSelectedDisaster();
        if (d != null) {
            showDisasterDialog(d);
        }
    }

    private void closeSelectedIncident() {
        Disaster d = getSelectedDisaster();
        if (d == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to resolve and close incident #" + d.getDisasterId() + " (" + d.getTitle() + ")?",
                "Confirm Incident Closure", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                disasterService.deleteOrClose(d.getDisasterId(), currentUser);
                JOptionPane.showMessageDialog(this, "Incident marked as Closed/Resolved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDisasters();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error closing incident: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDisasterDialog(Disaster existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Incident #" + existing.getDisasterId() : "Register New Disaster Incident", true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField(isEdit ? existing.getTitle() : "");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Flood", "Earthquake", "Fire", "Cyclone", "Landslide", "Other"});
        if (isEdit) typeBox.setSelectedItem(existing.getDisasterType());

        JTextField locField = new JTextField(isEdit ? existing.getLocation() : "");
        JComboBox<String> sevBox = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        if (isEdit) sevBox.setSelectedItem(existing.getSeverity());

        JSpinner affectedSpinner = new JSpinner(new SpinnerNumberModel(isEdit ? existing.getPeopleAffected() : 50, 0, 1000000, 10));
        JComboBox<String> stBox = new JComboBox<>(new String[]{"Reported", "Verified", "Response in Progress", "Resolved", "Closed"});
        if (isEdit) stBox.setSelectedItem(existing.getStatus());

        JTextArea descArea = new JTextArea(isEdit ? existing.getDescription() : "", 4, 20);

        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Disaster Type:"));
        form.add(typeBox);
        form.add(new JLabel("Location:"));
        form.add(locField);
        form.add(new JLabel("Severity:"));
        form.add(sevBox);
        form.add(new JLabel("People Affected:"));
        form.add(affectedSpinner);
        form.add(new JLabel("Status:"));
        form.add(stBox);
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descArea));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Save Incident");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String type = (String) typeBox.getSelectedItem();
                String loc = locField.getText().trim();
                String sev = (String) sevBox.getSelectedItem();
                int affected = (int) affectedSpinner.getValue();
                String status = (String) stBox.getSelectedItem();
                String desc = descArea.getText().trim();

                if (isEdit) {
                    existing.setTitle(title);
                    existing.setDisasterType(type);
                    existing.setLocation(loc);
                    existing.setSeverity(sev);
                    existing.setPeopleAffected(affected);
                    existing.setStatus(status);
                    existing.setDescription(desc);
                    disasterService.updateDisaster(existing, currentUser);
                } else {
                    Disaster newDisaster = new Disaster(title, type, desc, loc, sev, affected);
                    newDisaster.setStatus(status);
                    disasterService.registerDisaster(newDisaster, currentUser);
                }

                JOptionPane.showMessageDialog(dialog, "Disaster incident saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDisasters();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation / Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
