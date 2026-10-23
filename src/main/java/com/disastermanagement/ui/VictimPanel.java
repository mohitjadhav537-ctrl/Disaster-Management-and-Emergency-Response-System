package com.disastermanagement.ui;

import com.disastermanagement.dao.DisasterDAO;
import com.disastermanagement.dao.RescueTeamDAO;
import com.disastermanagement.dao.VictimDAO;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.HelpRequest;
import com.disastermanagement.model.RescueTeam;
import com.disastermanagement.model.User;
import com.disastermanagement.model.Victim;
import com.disastermanagement.service.HelpRequestService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel managing victims registration, distress triage, and emergency help requests.
 */
public class VictimPanel extends JPanel {

    private final User currentUser;
    private final VictimDAO victimDAO = new VictimDAO();
    private final DisasterDAO disasterDAO = new DisasterDAO();
    private final RescueTeamDAO teamDAO = new RescueTeamDAO();
    private final HelpRequestService helpService = new HelpRequestService();

    private JTable victimsTable;
    private DefaultTableModel victimModel;
    private JTable requestsTable;
    private DefaultTableModel requestModel;

    public VictimPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadVictims();
        loadHelpRequests();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Victims & Emergency Help Request Management");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_MAIN);

        JButton addVictimBtn = UITheme.createPrimaryButton("+ Register Affected Victim / Group");
        addVictimBtn.addActionListener(e -> showVictimRegistrationDialog());
        btnPanel.add(addVictimBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> {
            loadVictims();
            loadHelpRequests();
        });
        btnPanel.add(refreshBtn);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Tabbed Pane for (1) Victims Registry and (2) Emergency Help Requests
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // Tab 1: Victims Table Card
        JPanel victimCard = UITheme.createCardPanel();
        victimCard.setLayout(new BorderLayout(8, 8));

        String[] vCols = {"Victim ID", "Incident", "Full Name", "Age", "Contact", "Group Size", "Location", "Assistance Required", "Medical Status"};
        victimModel = new DefaultTableModel(vCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        victimsTable = new JTable(victimModel);
        UITheme.styleTable(victimsTable);
        victimCard.add(new JScrollPane(victimsTable), BorderLayout.CENTER);

        JPanel victimBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        victimBottom.setBackground(Color.WHITE);
        JButton createReqBtn = UITheme.createPrimaryButton("+ Create Help Request for Selected Victim");
        createReqBtn.addActionListener(e -> createHelpRequestForSelectedVictim());
        victimBottom.add(createReqBtn);
        victimCard.add(victimBottom, BorderLayout.SOUTH);

        tabbedPane.addTab("Registered Victims Directory", victimCard);

        // Tab 2: Help Requests Table Card
        JPanel reqCard = UITheme.createCardPanel();
        reqCard.setLayout(new BorderLayout(8, 8));

        String[] rCols = {"Request ID", "Victim", "Incident", "Type", "Priority", "Status", "Assigned Team", "Created At"};
        requestModel = new DefaultTableModel(rCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        requestsTable = new JTable(requestModel);
        UITheme.styleTable(requestsTable);
        reqCard.add(new JScrollPane(requestsTable), BorderLayout.CENTER);

        JPanel reqBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        reqBottom.setBackground(Color.WHITE);

        JButton updateStatusBtn = UITheme.createPrimaryButton("Update Request Status / Dispatch Team");
        updateStatusBtn.addActionListener(e -> updateHelpRequestStatus());
        reqBottom.add(updateStatusBtn);

        tabbedPane.addTab("Emergency Help Requests & Distress Calls", reqCard);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadVictims() {
        try {
            victimModel.setRowCount(0);
            List<Victim> list = victimDAO.findAll();
            for (Victim v : list) {
                victimModel.addRow(new Object[]{
                        v.getVictimId(),
                        v.getDisasterTitle(),
                        v.getFullName(),
                        v.getAge(),
                        v.getContactNumber(),
                        v.getGroupSize(),
                        v.getLocation(),
                        v.getAssistanceRequired(),
                        v.getMedicalStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load victims: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHelpRequests() {
        try {
            requestModel.setRowCount(0);
            List<HelpRequest> requests = helpService.getAllRequests();
            for (HelpRequest hr : requests) {
                requestModel.addRow(new Object[]{
                        hr.getRequestId(),
                        hr.getVictimName(),
                        hr.getDisasterTitle(),
                        hr.getRequestType(),
                        hr.getPriority(),
                        hr.getStatus(),
                        hr.getAssignedTeamName() != null ? hr.getAssignedTeamName() : "Unassigned",
                        hr.getCreatedAt()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load help requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showVictimRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Victim or Evacuee Group", true);
        dialog.setSize(480, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<Disaster> incidentBox = new JComboBox<>();
        try {
            List<Disaster> disasters = disasterDAO.findAll();
            for (Disaster d : disasters) incidentBox.addItem(d);
        } catch (Exception ignored) {}

        JTextField nameField = new JTextField();
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 120, 1));
        JTextField phoneField = new JTextField("+1-555-");
        JSpinner groupSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JTextField locField = new JTextField();
        JComboBox<String> assistBox = new JComboBox<>(new String[]{
                "Medical assistance", "Evacuation", "Food and water", "Shelter", "Rescue assistance"
        });
        JTextField medField = new JTextField("Normal");

        form.add(new JLabel("Disaster Incident:"));
        form.add(incidentBox);
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Age:"));
        form.add(ageSpinner);
        form.add(new JLabel("Contact Number:"));
        form.add(phoneField);
        form.add(new JLabel("Group Size:"));
        form.add(groupSpinner);
        form.add(new JLabel("Exact Location:"));
        form.add(locField);
        form.add(new JLabel("Assistance Required:"));
        form.add(assistBox);
        form.add(new JLabel("Medical Status:"));
        form.add(medField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Register Victim");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                Disaster selectedDisaster = (Disaster) incidentBox.getSelectedItem();
                if (selectedDisaster == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select an incident.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String name = nameField.getText().trim();
                int age = (int) ageSpinner.getValue();
                String phone = phoneField.getText().trim();
                int group = (int) groupSpinner.getValue();
                String loc = locField.getText().trim();
                String assist = (String) assistBox.getSelectedItem();
                String med = medField.getText().trim();

                if (name.isEmpty() || loc.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Full name and location are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Victim v = new Victim(selectedDisaster.getDisasterId(), name, age, phone, group, loc, assist, med);
                victimDAO.insert(v);

                // Option to automatically create a Help Request
                int autoReq = JOptionPane.showConfirmDialog(dialog,
                        "Victim registered successfully!\nDo you also want to generate an immediate Emergency Help Request?",
                        "Create Help Request?", JOptionPane.YES_NO_OPTION);

                if (autoReq == JOptionPane.YES_OPTION) {
                    HelpRequest req = new HelpRequest(v.getVictimId(), selectedDisaster.getDisasterId(), assist, "High",
                            "Emergency call from " + name + " (" + group + " people) at " + loc);
                    helpService.submitRequest(req, currentUser);
                }

                dialog.dispose();
                loadVictims();
                loadHelpRequests();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Registration error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void createHelpRequestForSelectedVictim() {
        int row = victimsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a victim from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int victimId = (int) victimModel.getValueAt(row, 0);
        String victimName = (String) victimModel.getValueAt(row, 2);

        try {
            Victim v = victimDAO.findById(victimId).orElse(null);
            if (v == null) return;

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Help Request for " + victimName, true);
            dialog.setSize(440, 360);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
            form.setBorder(new EmptyBorder(20, 20, 20, 20));

            JComboBox<String> typeBox = new JComboBox<>(new String[]{
                    "Medical assistance", "Evacuation", "Food and water", "Shelter", "Rescue assistance"
            });
            typeBox.setSelectedItem(v.getAssistanceRequired());

            JComboBox<String> prioBox = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
            JTextField descField = new JTextField("Request for " + v.getGroupSize() + " people at " + v.getLocation());

            form.add(new JLabel("Request Type:"));
            form.add(typeBox);
            form.add(new JLabel("Priority:"));
            form.add(prioBox);
            form.add(new JLabel("Description / Triage:"));
            form.add(descField);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveBtn = UITheme.createPrimaryButton("Submit Distress Request");
            JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
            cancelBtn.addActionListener(e -> dialog.dispose());

            saveBtn.addActionListener(e -> {
                try {
                    HelpRequest req = new HelpRequest(v.getVictimId(), v.getDisasterId(),
                            (String) typeBox.getSelectedItem(), (String) prioBox.getSelectedItem(), descField.getText().trim());
                    helpService.submitRequest(req, currentUser);
                    JOptionPane.showMessageDialog(dialog, "Help request logged successfully!", "Logged", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadHelpRequests();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnPanel.add(cancelBtn);
            btnPanel.add(saveBtn);
            dialog.add(form, BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateHelpRequestStatus() {
        int row = requestsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a help request from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int reqId = (int) requestModel.getValueAt(row, 0);
        String curStatus = (String) requestModel.getValueAt(row, 5);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Request #" + reqId, true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending", "Assigned", "In Progress", "Resolved", "Cancelled"});
        statusBox.setSelectedItem(curStatus);

        JComboBox<RescueTeam> teamBox = new JComboBox<>();
        teamBox.addItem(null); // Option for no team
        try {
            List<RescueTeam> teams = teamDAO.findAll();
            for (RescueTeam t : teams) teamBox.addItem(t);
        } catch (Exception ignored) {}

        form.add(new JLabel("New Status:"));
        form.add(statusBox);
        form.add(new JLabel("Assign Rescue Team:"));
        form.add(teamBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Update Status");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String st = (String) statusBox.getSelectedItem();
                RescueTeam selectedTeam = (RescueTeam) teamBox.getSelectedItem();
                Integer teamId = (selectedTeam != null) ? selectedTeam.getTeamId() : null;

                helpService.updateStatus(reqId, st, teamId, currentUser);
                JOptionPane.showMessageDialog(dialog, "Request status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadHelpRequests();
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
}
