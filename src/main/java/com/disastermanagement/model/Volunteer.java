package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Concrete model representing an emergency Volunteer.
 * Demonstrates Inheritance, Polymorphic method overriding, and specialized fields.
 */
public class Volunteer extends User {
    private int volunteerId;
    private String skills;
    private String availability;
    private String assignedTask;

    public Volunteer() {
        super();
        setRole("Volunteer");
        this.availability = "Available";
    }

    public Volunteer(String fullName, String username, String passwordHash, String phone, String skills) {
        super(fullName, username, passwordHash, phone, "Volunteer");
        this.skills = skills;
        this.availability = "Available";
    }

    public Volunteer(int userId, String fullName, String username, String passwordHash, String phone, 
                     String status, Timestamp createdAt, int volunteerId, String skills, 
                     String availability, String assignedTask) {
        super(userId, fullName, username, passwordHash, phone, "Volunteer", status, createdAt);
        this.volunteerId = volunteerId;
        this.skills = skills;
        this.availability = availability;
        this.assignedTask = assignedTask;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getAssignedTask() {
        return assignedTask;
    }

    public void setAssignedTask(String assignedTask) {
        this.assignedTask = assignedTask;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    @Override
    public String getRoleDescription() {
        return "Community Volunteer: Assisting in shelter management, food logistics, and support duties.";
    }
}
