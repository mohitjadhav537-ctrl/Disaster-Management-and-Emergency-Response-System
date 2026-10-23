package com.disastermanagement.model;

/**
 * Model representing a specialized emergency rescue team.
 * Implements Assignable and Reportable interfaces.
 */
public class RescueTeam implements Assignable, Reportable {
    private int teamId;
    private String teamName;
    private String teamLeader;
    private String contactNumber;
    private int teamSize;
    private String skills;
    private String equipment;
    private String availability; // Available, Busy, Off-Duty

    public RescueTeam() {
        this.availability = "Available";
        this.teamSize = 1;
    }

    public RescueTeam(String teamName, String teamLeader, String contactNumber, int teamSize, String skills, String equipment) {
        this.teamName = teamName;
        this.teamLeader = teamLeader;
        this.contactNumber = contactNumber;
        this.teamSize = teamSize;
        this.skills = skills;
        this.equipment = equipment;
        this.availability = "Available";
    }

    public RescueTeam(int teamId, String teamName, String teamLeader, String contactNumber, 
                      int teamSize, String skills, String equipment, String availability) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamLeader = teamLeader;
        this.contactNumber = contactNumber;
        this.teamSize = teamSize;
        this.skills = skills;
        this.equipment = equipment;
        this.availability = availability;
    }

    // Assignable interface implementation
    @Override
    public int getAssignableId() {
        return teamId;
    }

    @Override
    public String getAssignableName() {
        return teamName;
    }

    @Override
    public String getAssignmentStatus() {
        return availability;
    }

    @Override
    public void setAssignmentStatus(String status) {
        this.availability = status;
    }

    // Reportable interface implementation
    @Override
    public String[] getReportHeaders() {
        return new String[]{"Team ID", "Team Name", "Leader", "Contact", "Size", "Skills", "Availability"};
    }

    @Override
    public Object[] getReportRowData() {
        return new Object[]{teamId, teamName, teamLeader, contactNumber, teamSize, skills, availability};
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(availability);
    }

    // Getters and Setters
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

    public String getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(String teamLeader) {
        this.teamLeader = teamLeader;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return String.format("%s (Leader: %s | %s)", teamName, teamLeader, availability);
    }
}
