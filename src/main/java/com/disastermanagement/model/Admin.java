package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Concrete model representing an Administrator.
 * Demonstrates Inheritance and Polymorphic method overriding.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole("Admin");
    }

    public Admin(String fullName, String username, String passwordHash, String phone) {
        super(fullName, username, passwordHash, phone, "Admin");
    }

    public Admin(int userId, String fullName, String username, String passwordHash, String phone, String status, Timestamp createdAt) {
        super(userId, fullName, username, passwordHash, phone, "Admin", status, createdAt);
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator: Full access to incident response, team allocation, relief logistics, shelters, and reports.";
    }
}
