package com.disastermanagement.ui;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.model.AuditLog;
import com.disastermanagement.model.User;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel displaying system activity and compliance audit logs.
 */
public class AuditTrailPanel extends JPanel {

    private final User currentUser;
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public AuditTrailPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadLogs();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("System Audit Trail & Compliance Log");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh Log");
        refreshBtn.addActionListener(e -> loadLogs());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"Log ID", "Timestamp", "User Account", "Action Performed", "Entity Type", "Entity ID", "Details / Payload"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadLogs() {
        try {
            tableModel.setRowCount(0);
            List<AuditLog> logs = auditDAO.findAll();
            for (AuditLog l : logs) {
                tableModel.addRow(new Object[]{
                        l.getLogId(),
                        l.getActionTimestamp(),
                        l.getUserName(),
                        l.getAction(),
                        l.getEntityType(),
                        l.getEntityId(),
                        l.getDetails()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load audit logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
