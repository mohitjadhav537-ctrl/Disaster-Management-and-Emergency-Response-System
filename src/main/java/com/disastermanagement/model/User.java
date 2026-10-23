package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Abstract base class representing a generic user in the Disaster Management System.
 * Demonstrates:
 * - Encapsulation (private fields, protected/public getters & setters)
 * - Abstraction (abstract methods defining mandatory role contracts)
 * - Constructors and constructor overloading
 */
public abstract class User {
    private int userId;
    private String fullName;
    private String username;
    private String passwordHash;
    private String phone;
    private String role;
    private String status;
    private Timestamp createdAt;

    // Default Constructor
    public User() {
        this.status = "Active";
    }

    // Overloaded Constructor for user registration / creation
    public User(String fullName, String username, String passwordHash, String phone, String role) {
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.status = "Active";
    }

    // Full Parameterized Constructor
    public User(int userId, String fullName, String username, String passwordHash, String phone, String role, String status, Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Abstract methods demonstrating Abstraction and Polymorphism
    public abstract boolean hasAdminPrivileges();
    public abstract String getRoleDescription();

    // Getters and Setters (Encapsulation)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", fullName, username, role);
    }
}
