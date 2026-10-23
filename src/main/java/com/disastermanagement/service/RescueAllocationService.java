package com.disastermanagement.service;

import com.disastermanagement.dao.AssignmentDAO;
import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.DisasterDAO;
import com.disastermanagement.dao.RescueTeamDAO;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.InvalidAssignmentException;
import com.disastermanagement.model.Assignment;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.RescueTeam;
import com.disastermanagement.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Priority-Based Rescue Team Allocation Engine.
 * Implements rule-based matching, severity prioritization, availability validation,
 * and eligibility explanations.
 */
public class RescueAllocationService {

    private final DisasterDAO disasterDAO;
    private final RescueTeamDAO teamDAO;
    private final AssignmentDAO assignmentDAO;
    private final AuditLogDAO auditDAO;

    public RescueAllocationService() {
        this.disasterDAO = new DisasterDAO();
        this.teamDAO = new RescueTeamDAO();
        this.assignmentDAO = new AssignmentDAO();
        this.auditDAO = new AuditLogDAO();
    }

    /**
     * Data transfer object explaining team eligibility for a given incident.
     */
    public static class EligibilityAssessment {
        private final boolean eligible;
        private final int score;
        private final String explanation;

        public EligibilityAssessment(boolean eligible, int score, String explanation) {
            this.eligible = eligible;
            this.score = score;
            this.explanation = explanation;
        }

        public boolean isEligible() {
            return eligible;
        }

        public int getScore() {
            return score;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    public static class TeamEvaluationResult {
        private final RescueTeam team;
        private final EligibilityAssessment assessment;

        public TeamEvaluationResult(RescueTeam team, EligibilityAssessment assessment) {
            this.team = team;
            this.assessment = assessment;
        }

        public RescueTeam getTeam() {
            return team;
        }

        public EligibilityAssessment getAssessment() {
            return assessment;
        }
    }

    /**
     * Evaluates whether a rescue team is eligible to be dispatched to a disaster incident.
     * Evaluates availability, skill fit, equipment match, and severity alignment.
     */
    public EligibilityAssessment evaluateTeam(Disaster incident, RescueTeam team) {
        if (incident == null || team == null) {
            return new EligibilityAssessment(false, 0, "Incident or Team record is null.");
        }

        // Rule 1: Check availability
        if (!team.isAvailable()) {
            return new EligibilityAssessment(false, 0,
                    String.format("INELIGIBLE: Team is currently %s and cannot accept new missions.", team.getAvailability()));
        }

        // Rule 2: Evaluate skills and equipment matching the disaster type
        String disasterType = (incident.getDisasterType() == null) ? "" : incident.getDisasterType().toLowerCase(Locale.ROOT);
        String skills = (team.getSkills() == null) ? "" : team.getSkills().toLowerCase(Locale.ROOT);
        String equipment = (team.getEquipment() == null) ? "" : team.getEquipment().toLowerCase(Locale.ROOT);

        int matchScore = 50; // Base score for available teams
        List<String> matchedCriteria = new ArrayList<>();

        switch (disasterType) {
            case "flood" -> {
                if (skills.contains("flood") || skills.contains("water") || skills.contains("boat") || skills.contains("swiftwater")) {
                    matchScore += 40;
                    matchedCriteria.add("Aquatic / Flood Rescue skills");
                }
                if (equipment.contains("raft") || equipment.contains("boat") || equipment.contains("jacket")) {
                    matchScore += 30;
                    matchedCriteria.add("Watercraft / Life-Saving equipment");
                }
            }
            case "fire" -> {
                if (skills.contains("fire") || skills.contains("smoke") || skills.contains("hazmat")) {
                    matchScore += 40;
                    matchedCriteria.add("Fire suppression / Hazmat skills");
                }
                if (equipment.contains("truck") || equipment.contains("breathing") || equipment.contains("thermal")) {
                    matchScore += 30;
                    matchedCriteria.add("Breathing apparatus / Firefighting gear");
                }
            }
            case "earthquake" -> {
                if (skills.contains("urban") || skills.contains("sar") || skills.contains("collapse") || skills.contains("trench") || skills.contains("canine")) {
                    matchScore += 40;
                    matchedCriteria.add("Urban Search & Rescue (USAR) / K9 search");
                }
                if (equipment.contains("drone") || equipment.contains("jack") || equipment.contains("listening") || equipment.contains("camera")) {
                    matchScore += 30;
                    matchedCriteria.add("Acoustic / Structural search equipment");
                }
            }
            case "landslide" -> {
                if (skills.contains("mountain") || skills.contains("debris") || skills.contains("trench") || skills.contains("sar")) {
                    matchScore += 40;
                    matchedCriteria.add("Terrain SAR & Debris excavation skills");
                }
                if (equipment.contains("winch") || equipment.contains("drone") || equipment.contains("jack")) {
                    matchScore += 30;
                    matchedCriteria.add("Heavy winches & Search gear");
                }
            }
            case "cyclone" -> {
                if (skills.contains("evacuation") || skills.contains("crowd") || skills.contains("flood") || skills.contains("water")) {
                    matchScore += 40;
                    matchedCriteria.add("Mass evacuation & Flood response skills");
                }
                if (equipment.contains("transport") || equipment.contains("vehicle") || equipment.contains("raft")) {
                    matchScore += 30;
                    matchedCriteria.add("High-water transport vehicles & Rafts");
                }
            }
            default -> {
                if (skills.contains("first aid") || skills.contains("medical") || skills.contains("cpr") || skills.contains("rescue")) {
                    matchScore += 30;
                    matchedCriteria.add("General first aid & Search skills");
                }
            }
        }

        // Rule 3: Add severity weight bonus
        matchScore += incident.getSeverityPriority() * 10;

        // Build detailed explanation
        if (!matchedCriteria.isEmpty()) {
            String explanation = String.format("ELIGIBLE (Score: %d): Highly qualified for %s. Matched: %s. Team is 100%% Available.",
                    matchScore, incident.getDisasterType(), String.join(", ", matchedCriteria));
            return new EligibilityAssessment(true, matchScore, explanation);
        } else {
            String explanation = String.format("ELIGIBLE WITH RESERVATION (Score: %d): Team is Available, but primary skills (%s) do not directly match %s requirements. Can be assigned for secondary or logistics support.",
                    matchScore, team.getSkills(), incident.getDisasterType());
            return new EligibilityAssessment(true, matchScore, explanation);
        }
    }

    /**
     * Evaluates all teams for a given incident and ranks them from best match to least match.
     */
    public List<TeamEvaluationResult> getEvaluatedTeams(Disaster incident) throws DatabaseException {
        List<RescueTeam> allTeams = teamDAO.findAll();
        List<TeamEvaluationResult> results = new ArrayList<>();

        for (RescueTeam team : allTeams) {
            EligibilityAssessment assessment = evaluateTeam(incident, team);
            results.add(new TeamEvaluationResult(team, assessment));
        }

        // Sort: Eligible first, then descending by match score
        results.sort((a, b) -> {
            if (a.getAssessment().isEligible() != b.getAssessment().isEligible()) {
                return a.getAssessment().isEligible() ? -1 : 1;
            }
            return Integer.compare(b.getAssessment().getScore(), a.getAssessment().getScore());
        });

        return results;
    }

    /**
     * Finalizes the assignment with atomic transaction guarantees and audit logging.
     */
    public boolean allocateTeam(int disasterId, int teamId, User actor) 
            throws DatabaseException, InvalidAssignmentException {
        
        int actorId = (actor != null) ? actor.getUserId() : 1;
        boolean success = assignmentDAO.createAssignment(disasterId, teamId, actorId);

        if (success) {
            auditDAO.log(actorId, "ALLOCATE_RESCUE_TEAM", "Assignment", disasterId,
                    String.format("Assigned Rescue Team #%d to Disaster Incident #%d", teamId, disasterId));
        }
        return success;
    }

    /**
     * Completes an active mission and resets the team availability.
     */
    public boolean completeMission(int assignmentId, User actor) throws DatabaseException {
        int actorId = (actor != null) ? actor.getUserId() : 1;
        boolean success = assignmentDAO.completeAssignment(assignmentId);
        if (success) {
            auditDAO.log(actorId, "COMPLETE_MISSION", "Assignment", assignmentId,
                    "Completed mission for Assignment #" + assignmentId);
        }
        return success;
    }

    public List<Assignment> getAllAssignments() throws DatabaseException {
        return assignmentDAO.findAllWithDetails();
    }

    public List<Assignment> getActiveAssignments() throws DatabaseException {
        return assignmentDAO.findActiveAssignments();
    }
}
