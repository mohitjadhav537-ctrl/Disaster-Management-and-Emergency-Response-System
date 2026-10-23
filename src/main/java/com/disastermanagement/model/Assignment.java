package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing an assignment of a rescue team to a disaster incident.
 * Implements Reportable.
 */
public class Assignment implements Reportable {
    private int assignmentId;
    private int disasterId;
    private String disasterTitle;
    private String disasterSeverity;
    private int teamId;
    private String teamName;
    private int assignedBy;
    private String assignedByName;
    private Timestamp assignedAt;
    private String assignmentStatus; // Active, Completed, Cancelled
    private Timestamp completedAt;

    public Assignment() {
        this.assignmentStatus = "Active";
    }

    public Assignment(int disasterId, int teamId, int assignedBy) {
        this.disasterId = disasterId;
        this.teamId = teamId;
        this.assignedBy = assignedBy;
        this.assignmentStatus = "Active";
    }

    public Assignment(int assignmentId, int disasterId, String disasterTitle, String disasterSeverity,
                      int teamId, String teamName, int assignedBy, String assignedByName,
                      Timestamp assignedAt, String assignmentStatus, Timestamp completedAt) {
        this.assignmentId = assignmentId;
        this.disasterId = disasterId;
        this.disasterTitle = disasterTitle;
        this.disasterSeverity = disasterSeverity;
        this.teamId = teamId;
        this.teamName = teamName;
        this.assignedBy = assignedBy;
        this.assignedByName = assignedByName;
        this.assignedAt = assignedAt;
        this.assignmentStatus = assignmentStatus;
        this.completedAt = completedAt;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(assignmentStatus);
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Assignment ID", "Incident", "Severity", "Rescue Team", "Assigned By", "Assigned At", "Status", "Completed At"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{assignmentId, disasterTitle, disasterSeverity, teamName, assignedByName, assignedAt, assignmentStatus, completedAt};
    }

    // Getters and Setters
    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(int disasterId) {
        this.disasterId = disasterId;
    }

    public String getDisasterTitle() {
        return disasterTitle;
    }

    public void setDisasterTitle(String disasterTitle) {
        this.disasterTitle = disasterTitle;
    }

    public String getDisasterSeverity() {
        return disasterSeverity;
    }

    public void setDisasterSeverity(String disasterSeverity) {
        this.disasterSeverity = disasterSeverity;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(int assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public Timestamp getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return String.format("Assignment #%d [%s -> %s (%s)]", assignmentId, teamName, disasterTitle, assignmentStatus);
    }
}
