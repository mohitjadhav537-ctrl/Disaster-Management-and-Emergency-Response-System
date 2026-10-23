package com.disastermanagement.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UI Theme constants and styling helpers.
 * Implements a clean, professional disaster-response design using
 * navy blue, slate gray, clean white, and emergency accent highlights.
 */
public class UITheme {

    // Palette Colors
    public static final Color PRIMARY_NAVY = new Color(20, 35, 60);       // Dark Navy Blue
    public static final Color SECONDARY_NAVY = new Color(34, 58, 94);     // Slate Blue
    public static final Color ACCENT_BLUE = new Color(41, 128, 185);      // Interaction Blue
    public static final Color ACCENT_RED = new Color(214, 48, 49);        // Emergency Alert Red
    public static final Color ACCENT_GREEN = new Color(39, 174, 96);      // Available / Resolved
    public static final Color ACCENT_ORANGE = new Color(230, 126, 34);    // Warning / High Alert
    public static final Color ACCENT_YELLOW = new Color(241, 196, 15);    // Medium Alert
    public static final Color ACCENT_PURPLE = new Color(142, 68, 173);    // Special Operations
    
    public static final Color BG_MAIN = new Color(245, 247, 250);         // Canvas Background
    public static final Color BG_CARD = Color.WHITE;                      // Card Background
    public static final Color BG_SIDEBAR = new Color(15, 26, 44);         // Deep Sidebar Navy
    public static final Color TEXT_DARK = new Color(33, 37, 41);          // Standard Text
    public static final Color TEXT_MUTED = new Color(108, 117, 125);      // Muted Subtitle Text
    public static final Color TEXT_LIGHT = Color.WHITE;                   // White Contrast Text
    public static final Color BORDER_COLOR = new Color(222, 226, 230);    // Divider Border

    // Typography Fonts
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_KPI_VALUE = new Font("Segoe UI", Font.BOLD, 26);

    private UITheme() {}

    /**
     * Styles standard primary action buttons (Navy / Blue)
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_BLUE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_BLUE.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Styles alert/destructive buttons (Emergency Red)
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_RED);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_RED.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Styles secondary / reset buttons (Subtle Gray/Bordered)
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Styles success action buttons (Green)
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_GREEN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_GREEN.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates an elevated modern card panel
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    /**
     * Applies styling to standard JTable
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(238, 238, 238));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(241, 243, 245));
        header.setForeground(PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Center align numerical or status columns renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Custom renderer for badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(249, 250, 252));
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    /**
     * Helper to get severity color
     */
    public static Color getSeverityColor(String severity) {
        if (severity == null) return TEXT_DARK;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> ACCENT_RED;
            case "HIGH" -> ACCENT_ORANGE;
            case "MEDIUM" -> new Color(217, 130, 43);
            case "LOW" -> ACCENT_GREEN;
            default -> TEXT_DARK;
        };
    }
}
