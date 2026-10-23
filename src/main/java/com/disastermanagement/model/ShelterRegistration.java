package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing an evacuee check-in at a relief shelter.
 * Implements Reportable.
 */
public class ShelterRegistration implements Reportable {
    private int registrationId;
    private int shelterId;
    private String shelterName;
    private int victimId;
    private String victimName;
    private int peopleCount;
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private String status; // Active, Checked Out

    public ShelterRegistration() {
        this.status = "Active";
        this.peopleCount = 1;
    }

    public ShelterRegistration(int shelterId, int victimId, int peopleCount) {
        this.shelterId = shelterId;
        this.victimId = victimId;
        this.peopleCount = peopleCount;
        this.status = "Active";
    }

    public ShelterRegistration(int registrationId, int shelterId, String shelterName, int victimId, 
                               String victimName, int peopleCount, Timestamp checkInTime, 
                               Timestamp checkOutTime, String status) {
        this.registrationId = registrationId;
        this.shelterId = shelterId;
        this.shelterName = shelterName;
        this.victimId = victimId;
        this.victimName = victimName;
        this.peopleCount = peopleCount;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Registration ID", "Shelter", "Victim", "People Count", "Check In", "Check Out", "Status"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{registrationId, shelterName, victimName, peopleCount, checkInTime, checkOutTime, status};
    }

    // Getters and Setters
    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

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

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Timestamp getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Timestamp checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("#%d [%s -> %s (%d people)]", registrationId, victimName, shelterName, peopleCount);
    }
}
