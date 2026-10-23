package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing an audit trail log entry.
 * Implements Reportable.
 */
public class AuditLog implements Reportable {
    private int logId;
    private Integer userId;
    private String userName;
    private String action;
    private String entityType;
    private Integer entityId;
    private Timestamp actionTimestamp;
    private String details;

    public AuditLog() {}

    public AuditLog(Integer userId, String action, String entityType, Integer entityId, String details) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public AuditLog(int logId, Integer userId, String userName, String action, String entityType, 
                    Integer entityId, Timestamp actionTimestamp, String details) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actionTimestamp = actionTimestamp;
        this.details = details;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Log ID", "Timestamp", "User", "Action", "Entity", "Entity ID", "Details"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{logId, actionTimestamp, userName, action, entityType, entityId, details};
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Timestamp getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(Timestamp actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by User %s: %s", actionTimestamp, action, userName, details);
    }
}
