package com.disastermanagement.ui;

import com.disastermanagement.dao.DisasterDAO;
import com.disastermanagement.dao.RescueTeamDAO;
import com.disastermanagement.model.Assignment;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.RescueTeam;
import com.disastermanagement.model.User;
import com.disastermanagement.service.RescueAllocationService;
import com.disastermanagement.service.RescueAllocationService.TeamEvaluationResult;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Priority-Based Rescue Allocation Panel.
 * Demonstrates rule-based allocation simulation, eligibility assessment,
 * and mission management.
 */
public class AssignmentPanel extends JPanel {

    private final User currentUser;
    private final RescueAllocationService allocationService = new RescueAllocationService();
    private final DisasterDAO disasterDAO = new DisasterDAO();
    private final RescueTeamDAO teamDAO = new RescueTeamDAO();

    private JComboBox<Disaster> incidentDropdown;
    private JTable evaluationTable;
    private DefaultTableModel evalModel;
    private JTable activeAssignmentsTable;
    private DefaultTableModel assignModel;
    private JTextArea explanationArea;

    private List<TeamEvaluationResult> currentEvaluations;

    public AssignmentPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadIncidents();
        loadActiveAssignments();
    }

    private void initComponents() {
        // Top Title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Priority-Based Rescue Team Allocation Engine");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh All");
        refreshBtn.addActionListener(e -> {
            loadIncidents();
            loadActiveAssignments();
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center SplitPane: Allocation Wizard (Top/Left) & Active Assignments (Bottom/Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        // Top Section: Incident Selection & Rule Evaluation Card
        JPanel wizardCard = UITheme.createCardPanel();
        wizardCard.setLayout(new BorderLayout(10, 10));

        // Selector Bar
        JPanel selectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        selectBar.setBackground(Color.WHITE);

        JLabel selectLabel = new JLabel("Select Target Disaster Incident (Priority Ranked):");
        selectLabel.setFont(UITheme.FONT_BODY_BOLD);
        selectLabel.setForeground(UITheme.PRIMARY_NAVY);

        incidentDropdown = new JComboBox<>();
        incidentDropdown.setPreferredSize(new Dimension(460, 32));
        incidentDropdown.setFont(UITheme.FONT_BODY);
        incidentDropdown.addActionListener(e -> evaluateSelectedIncident());

        JButton evalBtn = UITheme.createPrimaryButton("⚡ Evaluate Teams for Incident");
        evalBtn.addActionListener(e -> evaluateSelectedIncident());

        selectBar.add(selectLabel);
        selectBar.add(incidentDropdown);
        selectBar.add(evalBtn);
        wizardCard.add(selectBar, BorderLayout.NORTH);

        // Team Evaluation Table
        String[] evalCols = {"Team ID", "Team Name", "Skills", "Availability", "Match Score", "Eligibility Status"};
        evalModel = new DefaultTableModel(evalCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        evaluationTable = new JTable(evalModel);
        UITheme.styleTable(evaluationTable);
        evaluationTable.getSelectionModel().addListSelectionListener(e -> showSelectedTeamExplanation());

        JScrollPane evalScroll = new JScrollPane(evaluationTable);
        evalScroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        // Bottom of Wizard: Explanation details and Assign Button
        JPanel wizardBottom = new JPanel(new BorderLayout(8, 8));
        wizardBottom.setBackground(Color.WHITE);

        explanationArea = new JTextArea("Select an incident and a rescue team to inspect eligibility rationale.", 3, 50);
        explanationArea.setFont(UITheme.FONT_BODY);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setEditable(false);
        explanationArea.setBackground(new Color(248, 249, 250));
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel assignActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        assignActionRow.setBackground(Color.WHITE);

        JButton assignBtn = UITheme.createSuccessButton("✓ Finalize & Dispatch Rescue Team");
        assignBtn.addActionListener(e -> finalizeAssignment());
        assignActionRow.add(assignBtn);

        wizardBottom.add(new JScrollPane(explanationArea), BorderLayout.CENTER);
        wizardBottom.add(assignActionRow, BorderLayout.SOUTH);

        wizardCard.add(evalScroll, BorderLayout.CENTER);
        wizardCard.add(wizardBottom, BorderLayout.SOUTH);

        splitPane.setTopComponent(wizardCard);

        // Bottom Section: Active Assignments Monitor
        JPanel activeCard = UITheme.createCardPanel();
        activeCard.setLayout(new BorderLayout(8, 8));

        JPanel activeHeader = new JPanel(new BorderLayout());
        activeHeader.setBackground(Color.WHITE);
        JLabel activeTitle = new JLabel("Currently Active Incident Deployments & Field Assignments");
        activeTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        activeTitle.setForeground(UITheme.PRIMARY_NAVY);
        activeHeader.add(activeTitle, BorderLayout.WEST);

        activeCard.add(activeHeader, BorderLayout.NORTH);

        String[] assignCols = {"ID", "Incident Title", "Severity", "Deployed Team", "Assigned By", "Assigned At", "Status"};
        assignModel = new DefaultTableModel(assignCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        activeAssignmentsTable = new JTable(assignModel);
        UITheme.styleTable(activeAssignmentsTable);
        JScrollPane assignScroll = new JScrollPane(activeAssignmentsTable);
        assignScroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        activeCard.add(assignScroll, BorderLayout.CENTER);

        JPanel activeBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        activeBottom.setBackground(Color.WHITE);

        JButton completeBtn = UITheme.createPrimaryButton("✓ Complete Mission (Release Team)");
        completeBtn.addActionListener(e -> completeSelectedMission());
        activeBottom.add(completeBtn);

        activeCard.add(activeBottom, BorderLayout.SOUTH);

        splitPane.setBottomComponent(activeCard);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadIncidents() {
        try {
            incidentDropdown.removeAllItems();
            List<Disaster> activeDisasters = disasterDAO.findActiveDisasters();
            for (Disaster d : activeDisasters) {
                incidentDropdown.addItem(d);
            }
            if (!activeDisasters.isEmpty()) {
                evaluateSelectedIncident();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load incidents: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void evaluateSelectedIncident() {
        Disaster selected = (Disaster) incidentDropdown.getSelectedItem();
        if (selected == null) {
            evalModel.setRowCount(0);
            explanationArea.setText("No active disaster incident selected.");
            return;
        }

        try {
            currentEvaluations = allocationService.getEvaluatedTeams(selected);
            evalModel.setRowCount(0);

            for (TeamEvaluationResult res : currentEvaluations) {
                RescueTeam t = res.getTeam();
                evalModel.addRow(new Object[]{
                        t.getTeamId(),
                        t.getTeamName(),
                        t.getSkills(),
                        t.getAvailability(),
                        res.getAssessment().getScore() + " pts",
                        res.getAssessment().isEligible() ? "ELIGIBLE" : "INELIGIBLE"
                });
            }

            if (!currentEvaluations.isEmpty()) {
                evaluationTable.setRowSelectionInterval(0, 0);
                showSelectedTeamExplanation();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Evaluation error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedTeamExplanation() {
        int row = evaluationTable.getSelectedRow();
        if (row >= 0 && currentEvaluations != null && row < currentEvaluations.size()) {
            TeamEvaluationResult res = currentEvaluations.get(row);
            explanationArea.setText(String.format("Evaluation for '%s':\n%s",
                    res.getTeam().getTeamName(), res.getAssessment().getExplanation()));
        }
    }

    private void finalizeAssignment() {
        Disaster selectedIncident = (Disaster) incidentDropdown.getSelectedItem();
        int row = evaluationTable.getSelectedRow();

        if (selectedIncident == null || row < 0 || currentEvaluations == null || row >= currentEvaluations.size()) {
            JOptionPane.showMessageDialog(this, "Please select both a disaster incident and a rescue team.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TeamEvaluationResult eval = currentEvaluations.get(row);
        RescueTeam team = eval.getTeam();

        if (!eval.getAssessment().isEligible()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot assign team: " + eval.getAssessment().getExplanation(),
                    "Ineligible Assignment Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation dialog with rule-based explanation
        String confirmMsg = String.format("""
                Confirm Rescue Team Mobilization:
                
                Disaster Incident: #%d - %s
                Severity: %s (Priority: %d)
                Rescue Team: %s
                Team Leader: %s
                
                Rule-Based Rationale:
                %s
                
                Do you want to finalize this assignment and dispatch the team?
                """, selectedIncident.getDisasterId(), selectedIncident.getTitle(), selectedIncident.getSeverity(),
                selectedIncident.getSeverityPriority(), team.getTeamName(), team.getTeamLeader(),
                eval.getAssessment().getExplanation());

        int choice = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirm Emergency Deployment", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean ok = allocationService.allocateTeam(selectedIncident.getDisasterId(), team.getTeamId(), currentUser);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Deployment Successful!\nTeam '" + team.getTeamName() + "' has been assigned to '" + selectedIncident.getTitle() + "'.",
                            "Assignment Completed", JOptionPane.INFORMATION_MESSAGE);
                    loadIncidents();
                    loadActiveAssignments();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Assignment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadActiveAssignments() {
        try {
            assignModel.setRowCount(0);
            List<Assignment> activeList = allocationService.getActiveAssignments();
            for (Assignment a : activeList) {
                assignModel.addRow(new Object[]{
                        a.getAssignmentId(),
                        a.getDisasterTitle(),
                        a.getDisasterSeverity(),
                        a.getTeamName(),
                        a.getAssignedByName(),
                        a.getAssignedAt(),
                        a.getAssignmentStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load active assignments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeSelectedMission() {
        int row = activeAssignmentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an active assignment row from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int assignmentId = (int) assignModel.getValueAt(row, 0);
        String teamName = (String) assignModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Complete mission for Assignment #" + assignmentId + "?\nTeam '" + teamName + "' will be released and marked Available.",
                "Complete Mission", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                allocationService.completeMission(assignmentId, currentUser);
                JOptionPane.showMessageDialog(this, "Mission completed. Team is now Available for new assignments.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadIncidents();
                loadActiveAssignments();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error completing mission: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
