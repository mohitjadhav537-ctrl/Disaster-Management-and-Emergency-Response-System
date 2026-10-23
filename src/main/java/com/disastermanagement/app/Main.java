package com.disastermanagement.app;

import com.disastermanagement.database.DatabaseConnection;
import com.disastermanagement.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Main application entry point for the Disaster Management and Emergency Response System.
 */
public class Main {

    public static void main(String[] args) {
       
        System.out.println(" Disaster Management & Emergency Response System (Version 1.0.0)");
        System.out.println(" Second-Year Engineering Project: Java OOP & MySQL Relational DBMS");
      

        // 1. Initialize Modern FlatLaf GUI Look & Feel
        try {
            FlatLightLaf.setup();
            System.out.println("[UI] FlatLaf modern Light theme initialized successfully.");
        } catch (Exception e) {
            System.err.println("[UI] FlatLaf setup failed. Falling back to system look and feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // 2. Pre-flight Database Diagnostic check
        try {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            if (connected) {
                System.out.println("[DB] Successfully connected to MySQL database: " +
                        DatabaseConnection.getInstance().getDbUrl());
            } else {
                System.out.println("[DB] Notice: MySQL database connection not yet established.");
                System.out.println("[DB] Please verify MySQL service is running and config/db.properties is configured.");
            }
        } catch (Exception e) {
            System.out.println("[DB] Notice: " + e.getMessage());
        }

        // 3. Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
