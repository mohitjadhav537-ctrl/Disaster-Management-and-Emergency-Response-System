package com.disastermanagement.ui;

import com.disastermanagement.model.User;
import com.disastermanagement.service.AuthenticationService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application dashboard containing responsive sidebar navigation,
 * role-based access enforcement, and dynamic module card switching.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final List<JButton> navButtons = new ArrayList<>();

    // Sub-panels
    private DashboardOverviewPanel overviewPanel;
    private DisasterPanel disasterPanel;
    private RescueTeamPanel rescueTeamPanel;
    private AssignmentPanel assignmentPanel;
    private VictimPanel victimPanel;
    private ResourcePanel resourcePanel;
    private ShelterPanel shelterPanel;
    private ReportsPanel reportsPanel;
    private AuditTrailPanel auditPanel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("Disaster Management and Emergency Response System - Command Console");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // 1. Top Navigation & User Info Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY_NAVY);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel appTitle = new JLabel("🚨 EMERGENCY DISASTER MANAGEMENT SYSTEM");
        appTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        appTitle.setForeground(Color.WHITE);

        JPanel rightUserPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightUserPanel.setBackground(UITheme.PRIMARY_NAVY);

        JLabel userBadge = new JLabel(String.format("Logged in: %s (%s)", currentUser.getFullName(), currentUser.getRole()));
        userBadge.setFont(UITheme.FONT_BODY_BOLD);
        userBadge.setForeground(new Color(236, 240, 241));

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.setPreferredSize(new Dimension(95, 32));
        logoutBtn.addActionListener(e -> performLogout());

        rightUserPanel.add(userBadge);
        rightUserPanel.add(logoutBtn);

        topBar.add(appTitle, BorderLayout.WEST);
        topBar.add(rightUserPanel, BorderLayout.EAST);

        // Accent strip under topbar
        JPanel redStripe = new JPanel();
        redStripe.setBackground(UITheme.ACCENT_RED);
        redStripe.setPreferredSize(new Dimension(1280, 3));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(topBar, BorderLayout.CENTER);
        headerWrapper.add(redStripe, BorderLayout.SOUTH);
        root.add(headerWrapper, BorderLayout.NORTH);

        // 2. Sidebar Navigation Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 720));
        sidebar.setBorder(new EmptyBorder(18, 12, 18, 12));

        JLabel navHeading = new JLabel("OPERATIONS MENU");
        navHeading.setFont(UITheme.FONT_SMALL);
        navHeading.setForeground(new Color(149, 165, 166));
        navHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navHeading);
        sidebar.add(Box.createVerticalStrut(12));

        // Create Navigation Items based on User Role
        addNavButton(sidebar, "📊 Dashboard Overview", "OVERVIEW");
        addNavButton(sidebar, "⚠️ Disaster Incidents", "DISASTERS");

        if (currentUser.hasAdminPrivileges() || "Rescue Officer".equalsIgnoreCase(currentUser.getRole())) {
            addNavButton(sidebar, "🚒 Rescue Teams", "TEAMS");
            addNavButton(sidebar, "🎯 Priority Allocation", "ALLOCATION");
        }

        addNavButton(sidebar, "👥 Victims & Help Requests", "VICTIMS");
        addNavButton(sidebar, "📦 Relief Inventory", "RESOURCES");
        addNavButton(sidebar, "🏠 Emergency Shelters", "SHELTERS");

        if (currentUser.hasAdminPrivileges() || "Rescue Officer".equalsIgnoreCase(currentUser.getRole())) {
            addNavButton(sidebar, "📈 Analytical Reports", "REPORTS");
        }

        if (currentUser.hasAdminPrivileges()) {
            addNavButton(sidebar, "🔒 Audit Trail Log", "AUDIT");
        }

        sidebar.add(Box.createVerticalGlue());

        // Role details at bottom of sidebar
        JPanel roleFooter = new JPanel(new BorderLayout());
        roleFooter.setBackground(new Color(25, 42, 68));
        roleFooter.setBorder(new EmptyBorder(10, 10, 10, 10));
        roleFooter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel roleLabel = new JLabel("Role: " + currentUser.getRole());
        roleLabel.setFont(UITheme.FONT_BODY_BOLD);
        roleLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("Status: Active Session");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_GREEN);

        roleFooter.add(roleLabel, BorderLayout.NORTH);
        roleFooter.add(statusLabel, BorderLayout.SOUTH);
        sidebar.add(roleFooter);

        root.add(sidebar, BorderLayout.WEST);

        // 3. Central Content Panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        // Instantiate Modules
        overviewPanel = new DashboardOverviewPanel(currentUser);
        disasterPanel = new DisasterPanel(currentUser);
        victimPanel = new VictimPanel(currentUser);
        resourcePanel = new ResourcePanel(currentUser);
        shelterPanel = new ShelterPanel(currentUser);

        contentPanel.add(overviewPanel, "OVERVIEW");
        contentPanel.add(disasterPanel, "DISASTERS");
        contentPanel.add(victimPanel, "VICTIMS");
        contentPanel.add(resourcePanel, "RESOURCES");
        contentPanel.add(shelterPanel, "SHELTERS");

        if (currentUser.hasAdminPrivileges() || "Rescue Officer".equalsIgnoreCase(currentUser.getRole())) {
            rescueTeamPanel = new RescueTeamPanel(currentUser);
            assignmentPanel = new AssignmentPanel(currentUser);
            reportsPanel = new ReportsPanel(currentUser);

            contentPanel.add(rescueTeamPanel, "TEAMS");
            contentPanel.add(assignmentPanel, "ALLOCATION");
            contentPanel.add(reportsPanel, "REPORTS");
        }

        if (currentUser.hasAdminPrivileges()) {
            auditPanel = new AuditTrailPanel(currentUser);
            contentPanel.add(auditPanel, "AUDIT");
        }

        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);

        // Select first button by default
        if (!navButtons.isEmpty()) {
            navButtons.get(0).doClick();
        }
    }

    private void addNavButton(JPanel container, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(200, 214, 229));
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(216, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(30, 48, 77), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            // Highlight active button
            for (JButton b : navButtons) {
                b.setBackground(UITheme.BG_SIDEBAR);
                b.setForeground(new Color(200, 214, 229));
            }
            btn.setBackground(UITheme.SECONDARY_NAVY);
            btn.setForeground(Color.WHITE);

            // Trigger refresh on overview when navigated
            if ("OVERVIEW".equals(cardName) && overviewPanel != null) {
                overviewPanel.refreshData();
            }
        });

        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to end your emergency command session?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthenticationService.getInstance().logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
}
