package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Concrete model representing a field Rescue Officer.
 * Demonstrates Inheritance, Polymorphic method overriding, and specialized fields.
 */
public class RescueOfficer extends User {
    private Integer assignedTeamId;
    private String teamName;

    public RescueOfficer() {
        super();
        setRole("Rescue Officer");
    }

    public RescueOfficer(String fullName, String username, String passwordHash, String phone) {
        super(fullName, username, passwordHash, phone, "Rescue Officer");
    }

    public RescueOfficer(int userId, String fullName, String username, String passwordHash, String phone, String status, Timestamp createdAt) {
        super(userId, fullName, username, passwordHash, phone, "Rescue Officer", status, createdAt);
    }

    public Integer getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(Integer assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    @Override
    public String getRoleDescription() {
        return "Rescue Officer: Operational field responder managing assigned incident operations and team progress.";
    }
}
