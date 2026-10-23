package com.disastermanagement.model;

/**
 * Model representing an emergency evacuation shelter.
 * Implements Reportable.
 */
public class Shelter implements Reportable {
    private int shelterId;
    private String shelterName;
    private String location;
    private int totalCapacity;
    private int currentOccupancy;
    private String contactNumber;
    private String status; // Open, Full, Closed

    public Shelter() {
        this.status = "Open";
        this.currentOccupancy = 0;
    }

    public Shelter(String shelterName, String location, int totalCapacity, String contactNumber) {
        this.shelterName = shelterName;
        this.location = location;
        this.totalCapacity = totalCapacity;
        this.currentOccupancy = 0;
        this.contactNumber = contactNumber;
        this.status = "Open";
    }

    public Shelter(int shelterId, String shelterName, String location, int totalCapacity, 
                   int currentOccupancy, String contactNumber, String status) {
        this.shelterId = shelterId;
        this.shelterName = shelterName;
        this.location = location;
        this.totalCapacity = totalCapacity;
        this.currentOccupancy = currentOccupancy;
        this.contactNumber = contactNumber;
        this.status = status;
    }

    public int getAvailableCapacity() {
        return Math.max(0, totalCapacity - currentOccupancy);
    }

    public double getOccupancyPercentage() {
        if (totalCapacity <= 0) return 0.0;
        return ((double) currentOccupancy / totalCapacity) * 100.0;
    }

    public boolean hasSpaceFor(int count) {
        return (currentOccupancy + count) <= totalCapacity;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Shelter ID", "Shelter Name", "Location", "Total Capacity", "Current Occupancy", "Available", "Occupancy %", "Status"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{
            shelterId, shelterName, location, totalCapacity, currentOccupancy, 
            getAvailableCapacity(), String.format("%.1f%%", getOccupancyPercentage()), status
        };
    }

    // Getters and Setters
    public int getShelterId() {
        return shelterId;
    }

    public void setShelterId(int shelterId) {
        this.shelterId = shelterId;
    }

    public String getShelterName() {
        return shelterName;
    }

    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d occupied) [%s]", shelterName, currentOccupancy, totalCapacity, status);
    }
}
