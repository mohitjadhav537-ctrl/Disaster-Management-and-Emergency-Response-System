package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing an emergency help request.
 * Implements Reportable.
 */
public class HelpRequest implements Reportable {
    private int requestId;
    private int victimId;
    private String victimName;
    private int disasterId;
    private String disasterTitle;
    private String requestType; // Medical assistance, Evacuation, Food and water, Shelter, Rescue assistance
    private String priority;    // Critical, High, Medium, Low
    private String description;
    private String status;      // Pending, Assigned, In Progress, Resolved, Cancelled
    private Integer assignedTeamId;
    private String assignedTeamName;
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    public HelpRequest() {
        this.status = "Pending";
        this.priority = "High";
    }

    public HelpRequest(int victimId, int disasterId, String requestType, String priority, String description) {
        this.victimId = victimId;
        this.disasterId = disasterId;
        this.requestType = requestType;
        this.priority = priority;
        this.description = description;
        this.status = "Pending";
    }

    public HelpRequest(int requestId, int victimId, String victimName, int disasterId, String disasterTitle,
                       String requestType, String priority, String description, String status,
                       Integer assignedTeamId, String assignedTeamName, Timestamp createdAt, Timestamp resolvedAt) {
        this.requestId = requestId;
        this.victimId = victimId;
        this.victimName = victimName;
        this.disasterId = disasterId;
        this.disasterTitle = disasterTitle;
        this.requestType = requestType;
        this.priority = priority;
        this.description = description;
        this.status = status;
        this.assignedTeamId = assignedTeamId;
        this.assignedTeamName = assignedTeamName;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public int getPriorityRank() {
        if (priority == null) return 0;
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Request ID", "Victim", "Incident", "Type", "Priority", "Status", "Assigned Team", "Created At"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{requestId, victimName, disasterTitle, requestType, priority, status, assignedTeamName, createdAt};
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getVictimId() {
        return victimId;
    }

    public void setVictimId(int victimId) {
        this.victimId = victimId;
    }

    public String getVictimName() {
        return victimName;
    }

    public void setVictimName(String victimName) {
        this.victimName = victimName;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(Integer assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
    }

    public String getAssignedTeamName() {
        return assignedTeamName;
    }

    public void setAssignedTeamName(String assignedTeamName) {
        this.assignedTeamName = assignedTeamName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public String toString() {
        return String.format("#%d [%s | %s] %s", requestId, priority, requestType, description);
    }
}
