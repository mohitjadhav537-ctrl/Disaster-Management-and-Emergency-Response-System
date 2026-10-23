package com.disastermanagement.model;

import java.sql.Timestamp;

/**
 * Model representing an affected individual or family group.
 * Implements Reportable.
 */
public class Victim implements Reportable {
    private int victimId;
    private int disasterId;
    private String disasterTitle;
    private String fullName;
    private int age;
    private String contactNumber;
    private int groupSize;
    private String location;
    private String assistanceRequired;
    private String medicalStatus;
    private Timestamp registeredAt;

    public Victim() {
        this.groupSize = 1;
        this.medicalStatus = "Normal";
    }

    public Victim(int disasterId, String fullName, int age, String contactNumber, int groupSize, 
                  String location, String assistanceRequired, String medicalStatus) {
        this.disasterId = disasterId;
        this.fullName = fullName;
        this.age = age;
        this.contactNumber = contactNumber;
        this.groupSize = groupSize;
        this.location = location;
        this.assistanceRequired = assistanceRequired;
        this.medicalStatus = medicalStatus;
    }

    public Victim(int victimId, int disasterId, String disasterTitle, String fullName, int age, 
                  String contactNumber, int groupSize, String location, String assistanceRequired, 
                  String medicalStatus, Timestamp registeredAt) {
        this.victimId = victimId;
        this.disasterId = disasterId;
        this.disasterTitle = disasterTitle;
        this.fullName = fullName;
        this.age = age;
        this.contactNumber = contactNumber;
        this.groupSize = groupSize;
        this.location = location;
        this.assistanceRequired = assistanceRequired;
        this.medicalStatus = medicalStatus;
        this.registeredAt = registeredAt;
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Victim ID", "Name", "Age", "Contact", "Group Size", "Incident", "Location", "Assistance Required", "Medical Status"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{victimId, fullName, age, contactNumber, groupSize, disasterTitle, location, assistanceRequired, medicalStatus};
    }

    // Getters and Setters
    public int getVictimId() {
        return victimId;
    }

    public void setVictimId(int victimId) {
        this.victimId = victimId;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAssistanceRequired() {
        return assistanceRequired;
    }

    public void setAssistanceRequired(String assistanceRequired) {
        this.assistanceRequired = assistanceRequired;
    }

    public String getMedicalStatus() {
        return medicalStatus;
    }

    public void setMedicalStatus(String medicalStatus) {
        this.medicalStatus = medicalStatus;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return String.format("%s (Group: %d, Needs: %s)", fullName, groupSize, assistanceRequired);
    }
}
