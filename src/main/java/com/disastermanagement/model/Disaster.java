package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing a disaster incident.
 * Implements Reportable for standardized reporting.
 */
public class Disaster implements Reportable {
    private int disasterId;
    private String title;
    private String disasterType; // Flood, Earthquake, Fire, Cyclone, Landslide, Other
    private String description;
    private String location;
    private String severity;     // Critical, High, Medium, Low
    private int peopleAffected;
    private String status;       // Reported, Verified, Response in Progress, Resolved, Closed
    private Timestamp reportedAt;
    private Timestamp updatedAt;

    public Disaster() {
        this.status = "Reported";
        this.severity = "Medium";
    }

    public Disaster(String title, String disasterType, String description, String location, String severity, int peopleAffected) {
        this.title = title;
        this.disasterType = disasterType;
        this.description = description;
        this.location = location;
        this.severity = severity;
        this.peopleAffected = peopleAffected;
        this.status = "Reported";
    }

    public Disaster(int disasterId, String title, String disasterType, String description, String location, 
                    String severity, int peopleAffected, String status, Timestamp reportedAt, Timestamp updatedAt) {
        this.disasterId = disasterId;
        this.title = title;
        this.disasterType = disasterType;
        this.description = description;
        this.location = location;
        this.severity = severity;
        this.peopleAffected = peopleAffected;
        this.status = status;
        this.reportedAt = reportedAt;
        this.updatedAt = updatedAt;
    }

    // Priority ranking helper for the rule-based allocation engine
    public int getSeverityPriority() {
        if (severity == null) return 0;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    public boolean isActive() {
        return !"Resolved".equalsIgnoreCase(status) && !"Closed".equalsIgnoreCase(status);
    }

    // Reportable interface implementation
    @Override
    public String[] getReportHeaders() {
        return new String[]{"Incident ID", "Title", "Type", "Severity", "Location", "People Affected", "Status", "Reported At"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{disasterId, title, disasterType, severity, location, peopleAffected, status, reportedAt};
    }

    // Getters and Setters
    public int getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(int disasterId) {
        this.disasterId = disasterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(String disasterType) {
        this.disasterType = disasterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getPeopleAffected() {
        return peopleAffected;
    }

    public void setPeopleAffected(int peopleAffected) {
        this.peopleAffected = peopleAffected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Timestamp reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s [%s | %s]", disasterId, title, severity, status);
    }
}
