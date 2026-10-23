package com.disastermanagement.ui;

import com.disastermanagement.dao.VictimDAO;
import com.disastermanagement.model.Shelter;
import com.disastermanagement.model.ShelterRegistration;
import com.disastermanagement.model.User;
import com.disastermanagement.model.Victim;
import com.disastermanagement.service.ShelterService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel managing evacuation shelters, occupancy thresholds, and evacuee registrations.
 */
public class ShelterPanel extends JPanel {

    private final User currentUser;
    private final ShelterService shelterService = new ShelterService();
    private final VictimDAO victimDAO = new VictimDAO();

    private JTable shelterTable;
    private DefaultTableModel shelterModel;
    private JTable regTable;
    private DefaultTableModel regModel;

    public ShelterPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadShelters();
        loadRegistrations();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Emergency Shelters & Evacuee Logistics");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_MAIN);

        JButton addShelterBtn = UITheme.createPrimaryButton("+ Register New Shelter");
        addShelterBtn.addActionListener(e -> showAddShelterDialog());
        btnPanel.add(addShelterBtn);

        JButton checkInBtn = UITheme.createSuccessButton("🏠 Check-In Evacuees to Shelter");
        checkInBtn.addActionListener(e -> showCheckInDialog());
        btnPanel.add(checkInBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> {
            loadShelters();
            loadRegistrations();
        });
        btnPanel.add(refreshBtn);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // Tab 1: Shelters Directory Card
        JPanel shelterCard = UITheme.createCardPanel();
        shelterCard.setLayout(new BorderLayout(8, 8));

        String[] sCols = {"ID", "Shelter Facility Name", "Location", "Total Capacity", "Current Occupancy", "Available Slots", "Occupancy %", "Facility Status"};
        shelterModel = new DefaultTableModel(sCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        shelterTable = new JTable(shelterModel);
        UITheme.styleTable(shelterTable);

        // Highlighting for Full / Near capacity shelters
        shelterTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String val = (value == null) ? "" : value.toString();
                if ("Full".equalsIgnoreCase(val) || "Closed".equalsIgnoreCase(val)) {
                    c.setForeground(UITheme.ACCENT_RED);
                    c.setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    c.setForeground(UITheme.ACCENT_GREEN.darker());
                    c.setFont(UITheme.FONT_BODY);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        shelterCard.add(new JScrollPane(shelterTable), BorderLayout.CENTER);
        tabbedPane.addTab("Emergency Shelters Overview", shelterCard);

        // Tab 2: Shelter Evacuee Registrations Card
        JPanel regCard = UITheme.createCardPanel();
        regCard.setLayout(new BorderLayout(8, 8));

        String[] rCols = {"Registration ID", "Shelter Facility", "Evacuee / Group Name", "People Count", "Check-In Time", "Check-Out Time", "Status"};
        regModel = new DefaultTableModel(rCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        regTable = new JTable(regModel);
        UITheme.styleTable(regTable);
        regCard.add(new JScrollPane(regTable), BorderLayout.CENTER);

        JPanel regBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        regBottom.setBackground(Color.WHITE);

        JButton checkOutBtn = UITheme.createPrimaryButton("Check-Out Evacuees (Free Up Capacity)");
        checkOutBtn.addActionListener(e -> checkOutSelected());
        regBottom.add(checkOutBtn);

        regCard.add(regBottom, BorderLayout.SOUTH);
        tabbedPane.addTab("Shelter Evacuee Check-In / Check-Out Log", regCard);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadShelters() {
        try {
            shelterModel.setRowCount(0);
            List<Shelter> list = shelterService.getAllShelters();
            for (Shelter s : list) {
                shelterModel.addRow(new Object[]{
                        s.getShelterId(),
                        s.getShelterName(),
                        s.getLocation(),
                        s.getTotalCapacity(),
                        s.getCurrentOccupancy(),
                        s.getAvailableCapacity(),
                        String.format("%.1f%%", s.getOccupancyPercentage()),
                        s.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load shelters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRegistrations() {
        try {
            regModel.setRowCount(0);
            List<ShelterRegistration> list = shelterService.getAllRegistrations();
            for (ShelterRegistration sr : list) {
                regModel.addRow(new Object[]{
                        sr.getRegistrationId(),
                        sr.getShelterName(),
                        sr.getVictimName(),
                        sr.getPeopleCount(),
                        sr.getCheckInTime(),
                        sr.getCheckOutTime() != null ? sr.getCheckOutTime() : "Still Occupying",
                        sr.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load registrations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddShelterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Emergency Shelter", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField locField = new JTextField();
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(250, 10, 10000, 25));
        JTextField phoneField = new JTextField("+1-555-");

        form.add(new JLabel("Shelter Facility Name:"));
        form.add(nameField);
        form.add(new JLabel("Location / Address:"));
        form.add(locField);
        form.add(new JLabel("Total Evacuee Capacity:"));
        form.add(capSpinner);
        form.add(new JLabel("Contact Hotline:"));
        form.add(phoneField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Register Shelter");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String loc = locField.getText().trim();
                int cap = (int) capSpinner.getValue();
                String phone = phoneField.getText().trim();

                Shelter s = new Shelter(name, loc, cap, phone);
                shelterService.registerShelter(s, currentUser);

                JOptionPane.showMessageDialog(dialog, "Shelter registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadShelters();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showCheckInDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Check-In Evacuees to Shelter", true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<Shelter> shelterBox = new JComboBox<>();
        try {
            List<Shelter> shelters = shelterService.getAllShelters();
            for (Shelter s : shelters) {
                if (!"Closed".equalsIgnoreCase(s.getStatus())) {
                    shelterBox.addItem(s);
                }
            }
        } catch (Exception ignored) {}

        JComboBox<Victim> victimBox = new JComboBox<>();
        try {
            List<Victim> victims = victimDAO.findAll();
            for (Victim v : victims) victimBox.addItem(v);
        } catch (Exception ignored) {}

        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JLabel availLabel = new JLabel("Select shelter to view available space");
        availLabel.setForeground(UITheme.TEXT_MUTED);

        shelterBox.addActionListener(e -> {
            Shelter s = (Shelter) shelterBox.getSelectedItem();
            if (s != null) {
                availLabel.setText(String.format("Available: %d slots (Occupied: %d / %d)",
                        s.getAvailableCapacity(), s.getCurrentOccupancy(), s.getTotalCapacity()));
                availLabel.setForeground(s.getAvailableCapacity() > 0 ? UITheme.ACCENT_GREEN.darker() : UITheme.ACCENT_RED);
            }
        });

        victimBox.addActionListener(e -> {
            Victim v = (Victim) victimBox.getSelectedItem();
            if (v != null) {
                countSpinner.setValue(Math.max(1, v.getGroupSize()));
            }
        });

        form.add(new JLabel("Target Shelter:"));
        form.add(shelterBox);
        form.add(new JLabel("Shelter Space:"));
        form.add(availLabel);
        form.add(new JLabel("Evacuee / Family:"));
        form.add(victimBox);
        form.add(new JLabel("Number of People:"));
        form.add(countSpinner);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createSuccessButton("Confirm Check-In");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                Shelter selectedShelter = (Shelter) shelterBox.getSelectedItem();
                Victim selectedVictim = (Victim) victimBox.getSelectedItem();
                int count = (int) countSpinner.getValue();

                if (selectedShelter == null || selectedVictim == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select both a shelter and an evacuee.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Execute transactional check-in
                shelterService.checkInEvacuees(selectedShelter.getShelterId(), selectedVictim.getVictimId(), count, currentUser);

                JOptionPane.showMessageDialog(dialog,
                        String.format("Successfully checked in %d people into %s.", count, selectedShelter.getShelterName()),
                        "Check-In Completed", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadShelters();
                loadRegistrations();
            } catch (Exception ex) {
                // Catches CapacityExceededException or DatabaseException
                JOptionPane.showMessageDialog(dialog,
                        "Check-in Blocked / Rejected:\n" + ex.getMessage(),
                        "Capacity Exceeded / Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void checkOutSelected() {
        int row = regTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an active registration row to check out.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int regId = (int) regModel.getValueAt(row, 0);
        String name = (String) regModel.getValueAt(row, 2);
        String status = (String) regModel.getValueAt(row, 6);

        if ("Checked Out".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This evacuee group has already checked out.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Check out evacuee group '" + name + "' (Registration #" + regId + ")?\nThis will decrement occupancy and free shelter slots.",
                "Confirm Check-Out", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                shelterService.checkOutEvacuees(regId, currentUser);
                JOptionPane.showMessageDialog(this, "Check-out completed and shelter capacity restored.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadShelters();
                loadRegistrations();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error during check-out: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
