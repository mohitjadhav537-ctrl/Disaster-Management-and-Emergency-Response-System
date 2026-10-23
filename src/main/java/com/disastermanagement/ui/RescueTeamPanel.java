package com.disastermanagement.ui;

import com.disastermanagement.dao.RescueTeamDAO;
import com.disastermanagement.model.RescueTeam;
import com.disastermanagement.model.User;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel managing rescue teams, specialized gear, personnel capacities, and availability.
 */
public class RescueTeamPanel extends JPanel {

    private final User currentUser;
    private final RescueTeamDAO teamDAO = new RescueTeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public RescueTeamPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadTeams();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Rescue Teams & Taskforce Roster");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_MAIN);

        if (currentUser.hasAdminPrivileges()) {
            JButton addBtn = UITheme.createPrimaryButton("+ Register Rescue Team");
            addBtn.addActionListener(e -> showTeamDialog(null));
            btnPanel.add(addBtn);
        }

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> loadTeams());
        btnPanel.add(refreshBtn);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"ID", "Team Name", "Team Leader", "Contact", "Personnel Size", "Specialized Skills", "Equipment", "Availability"};
        tableModel = new DefaultTableModel(cols, 0) {
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

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottomBar.setBackground(Color.WHITE);

        if (currentUser.hasAdminPrivileges()) {
            JButton editBtn = UITheme.createSecondaryButton("Edit Team");
            editBtn.addActionListener(e -> editSelectedTeam());
            bottomBar.add(editBtn);

            JButton toggleAvailBtn = UITheme.createPrimaryButton("Toggle Availability Status");
            toggleAvailBtn.addActionListener(e -> toggleAvailability());
            bottomBar.add(toggleAvailBtn);
        }

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private void loadTeams() {
        try {
            tableModel.setRowCount(0);
            List<RescueTeam> teams = teamDAO.findAll();
            for (RescueTeam t : teams) {
                tableModel.addRow(new Object[]{
                        t.getTeamId(),
                        t.getTeamName(),
                        t.getTeamLeader(),
                        t.getContactNumber(),
                        t.getTeamSize(),
                        t.getSkills(),
                        t.getEquipment(),
                        t.getAvailability()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load teams: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RescueTeam getSelectedTeam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a rescue team from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int teamId = (int) tableModel.getValueAt(row, 0);
        try {
            return teamDAO.findById(teamId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void editSelectedTeam() {
        RescueTeam t = getSelectedTeam();
        if (t != null) {
            showTeamDialog(t);
        }
    }

    private void toggleAvailability() {
        RescueTeam t = getSelectedTeam();
        if (t == null) return;

        String[] statuses = {"Available", "Busy", "Off-Duty"};
        String current = t.getAvailability();
        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select new availability status for team: " + t.getTeamName(),
                "Update Availability", JOptionPane.PLAIN_MESSAGE, null, statuses, current);

        if (chosen != null && !chosen.equals(current)) {
            try {
                teamDAO.updateAvailability(t.getTeamId(), chosen);
                JOptionPane.showMessageDialog(this, "Status updated to " + chosen, "Updated", JOptionPane.INFORMATION_MESSAGE);
                loadTeams();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTeamDialog(RescueTeam existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Team #" + existing.getTeamId() : "Register New Rescue Team", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(isEdit ? existing.getTeamName() : "");
        JTextField leaderField = new JTextField(isEdit ? existing.getTeamLeader() : "");
        JTextField contactField = new JTextField(isEdit ? existing.getContactNumber() : "+1-555-");
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(isEdit ? existing.getTeamSize() : 10, 1, 100, 1));
        JTextField skillsField = new JTextField(isEdit ? existing.getSkills() : "Search & Rescue, First Aid");
        JTextField equipField = new JTextField(isEdit ? existing.getEquipment() : "Medical Kits, Radios, Ropes");
        JComboBox<String> availBox = new JComboBox<>(new String[]{"Available", "Busy", "Off-Duty"});
        if (isEdit) availBox.setSelectedItem(existing.getAvailability());

        form.add(new JLabel("Team Name:"));
        form.add(nameField);
        form.add(new JLabel("Team Leader:"));
        form.add(leaderField);
        form.add(new JLabel("Contact Number:"));
        form.add(contactField);
        form.add(new JLabel("Personnel Size:"));
        form.add(sizeSpinner);
        form.add(new JLabel("Skills:"));
        form.add(skillsField);
        form.add(new JLabel("Equipment:"));
        form.add(equipField);
        form.add(new JLabel("Availability:"));
        form.add(availBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Save Team");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String leader = leaderField.getText().trim();
                String contact = contactField.getText().trim();
                int size = (int) sizeSpinner.getValue();
                String skills = skillsField.getText().trim();
                String equip = equipField.getText().trim();
                String avail = (String) availBox.getSelectedItem();

                if (name.isEmpty() || leader.isEmpty() || contact.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please complete all mandatory fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (isEdit) {
                    existing.setTeamName(name);
                    existing.setTeamLeader(leader);
                    existing.setContactNumber(contact);
                    existing.setTeamSize(size);
                    existing.setSkills(skills);
                    existing.setEquipment(equip);
                    existing.setAvailability(avail);
                    teamDAO.update(existing);
                } else {
                    RescueTeam newTeam = new RescueTeam(name, leader, contact, size, skills, equip);
                    newTeam.setAvailability(avail);
                    teamDAO.insert(newTeam);
                }

                JOptionPane.showMessageDialog(dialog, "Rescue team saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTeams();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
