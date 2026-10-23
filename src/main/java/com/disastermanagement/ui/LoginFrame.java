package com.disastermanagement.ui;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.exception.AuthenticationException;
import com.disastermanagement.model.User;
import com.disastermanagement.service.AuthenticationService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Swing Login Frame with credential validation, role feedback,
 * and quick-fill demonstration helpers.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Disaster Management & Emergency Response System - Login");
        setSize(520, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_MAIN);

        // Header Panel (Navy with emergency accent line)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_NAVY);
        headerPanel.setBorder(new EmptyBorder(28, 24, 28, 24));

        JLabel titleLabel = new JLabel("EMERGENCY COMMAND PORTAL", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Disaster Management & Priority Response System", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(new Color(189, 195, 199));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Accent line
        JPanel accentBar = new JPanel();
        accentBar.setBackground(UITheme.ACCENT_RED);
        accentBar.setPreferredSize(new Dimension(520, 4));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(accentBar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Form Center Card Panel
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setBackground(UITheme.BG_MAIN);
        cardWrapper.setBorder(new EmptyBorder(20, 36, 20, 36));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel loginTitle = new JLabel("Secure User Authentication");
        loginTitle.setFont(UITheme.FONT_HEADER_MEDIUM);
        loginTitle.setForeground(UITheme.PRIMARY_NAVY);
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(loginTitle);
        formCard.add(Box.createVerticalStrut(20));

        // Username Field
        JLabel userLabel = new JLabel("Username / ID:");
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(userLabel);
        formCard.add(Box.createVerticalStrut(6));

        usernameField = new JTextField("admin");
        usernameField.setFont(UITheme.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(360, 38));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(16));

        // Password Field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UITheme.FONT_BODY_BOLD);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(6));

        passwordField = new JPasswordField("admin123");
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(360, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(16));

        // Status Label for error feedback
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.ACCENT_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(12));

        // Action Buttons
        loginButton = UITheme.createPrimaryButton("Log In to Dashboard");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.addActionListener(e -> performLogin());

        // Enter key trigger
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(18));

        // Quick Demo Fill Buttons
        JLabel demoLabel = new JLabel("Quick Demo Sign-In Credentials:");
        demoLabel.setFont(UITheme.FONT_SMALL);
        demoLabel.setForeground(UITheme.TEXT_MUTED);
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(demoLabel);
        formCard.add(Box.createVerticalStrut(8));

        JPanel demoButtonsPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        demoButtonsPanel.setBackground(Color.WHITE);
        demoButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JButton btnAdmin = createQuickFillBtn("Admin", "admin", "admin123");
        JButton btnOfficer = createQuickFillBtn("Officer", "officer_sarah", "rescue123");
        JButton btnVol = createQuickFillBtn("Volunteer", "volunteer_emily", "volunteer123");

        demoButtonsPanel.add(btnAdmin);
        demoButtonsPanel.add(btnOfficer);
        demoButtonsPanel.add(btnVol);
        formCard.add(demoButtonsPanel);

        cardWrapper.add(formCard);
        mainPanel.add(cardWrapper, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UITheme.BG_MAIN);
        JLabel footerLabel = new JLabel("Java OOP & MySQL Relational Project © Second-Year Engineering");
        footerLabel.setFont(UITheme.FONT_SMALL);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footer.add(footerLabel);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createQuickFillBtn(String role, String user, String pass) {
        JButton btn = new JButton(role);
        btn.setFont(UITheme.FONT_SMALL);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            statusLabel.setText("Filled credentials for " + role);
            statusLabel.setForeground(UITheme.ACCENT_BLUE);
        });
        return btn;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            statusLabel.setForeground(UITheme.ACCENT_RED);
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating credentials...");
        statusLabel.setForeground(UITheme.ACCENT_BLUE);

        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().login(username, password);
                statusLabel.setText("Login successful! Loading workspace...");
                statusLabel.setForeground(UITheme.ACCENT_GREEN);

                // Open Main Dashboard
                DashboardFrame dashboard = new DashboardFrame(user);
                dashboard.setVisible(true);
                dispose();

            } catch (AuthenticationException ex) {
                statusLabel.setText(ex.getMessage());
                statusLabel.setForeground(UITheme.ACCENT_RED);
                loginButton.setEnabled(true);
            } catch (Exception ex) {
                statusLabel.setText("Database Connection Error. Check config/db.properties.");
                statusLabel.setForeground(UITheme.ACCENT_RED);
                loginButton.setEnabled(true);
                JOptionPane.showMessageDialog(this,
                        "Could not connect to MySQL database.\nError: " + ex.getMessage() +
                                "\n\nPlease verify:\n1. MySQL server is running.\n2. disaster_management_db is imported.\n3. config/db.properties matches MySQL user & password.",
                        "Database Connectivity Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
