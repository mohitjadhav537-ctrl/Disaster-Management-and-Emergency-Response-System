package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing a distributed shipment of relief inventory.
 * Implements Reportable.
 */
public class ResourceDistribution implements Reportable {
    private int distributionId;
    private int resourceId;
    private String resourceName;
    private int disasterId;
    private String disasterTitle;
    private int quantityDistributed;
    private String unit;
    private int distributedBy;
    private String distributedByName;
    private Timestamp distributionDate;
    private String notes;

    public ResourceDistribution() {}

    public ResourceDistribution(int resourceId, int disasterId, int quantityDistributed, int distributedBy, String notes) {
        this.resourceId = resourceId;
        this.disasterId = disasterId;
        this.quantityDistributed = quantityDistributed;
        this.distributedBy = distributedBy;
        this.notes = notes;
    }

    public ResourceDistribution(int distributionId, int resourceId, String resourceName, int disasterId, 
                                String disasterTitle, int quantityDistributed, String unit, int distributedBy, 
                                String distributedByName, Timestamp distributionDate, String notes) {
        this.distributionId = distributionId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.disasterId = disasterId;
        this.disasterTitle = disasterTitle;
        this.quantityDistributed = quantityDistributed;
        this.unit = unit;
        this.distributedBy = distributedBy;
        this.distributedByName = distributedByName;
        this.distributionDate = distributionDate;
        this.notes = notes;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Distribution ID", "Resource", "Disaster Incident", "Qty Distributed", "Unit", "Distributed By", "Date", "Notes"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{distributionId, resourceName, disasterTitle, quantityDistributed, unit, distributedByName, distributionDate, notes};
    }

    // Getters and Setters
    public int getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(int distributionId) {
        this.distributionId = distributionId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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

    public int getQuantityDistributed() {
        return quantityDistributed;
    }

    public void setQuantityDistributed(int quantityDistributed) {
        this.quantityDistributed = quantityDistributed;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDistributedBy() {
        return distributedBy;
    }

    public void setDistributedBy(int distributedBy) {
        this.distributedBy = distributedBy;
    }

    public String getDistributedByName() {
        return distributedByName;
    }

    public void setDistributedByName(String distributedByName) {
        this.distributedByName = distributedByName;
    }

    public Timestamp getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(Timestamp distributionDate) {
        this.distributionDate = distributionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return String.format("#%d: %d %s of %s -> %s", distributionId, quantityDistributed, unit, resourceName, disasterTitle);
    }
}
