package com.disastermanagement;

import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.RescueTeam;
import com.disastermanagement.service.RescueAllocationService;
import com.disastermanagement.service.RescueAllocationService.EligibilityAssessment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the Priority-Based Rescue Allocation rules:
 * - Prioritizes Critical incidents
 * - Checks availability
 * - Skill & equipment matching
 * - Explanation generation
 */
public class RescueAllocationRuleTest {

    private final RescueAllocationService service = new RescueAllocationService();

    @Test
    @DisplayName("Rule: Unavailable or Busy team must be judged Ineligible")
    void testBusyTeamIsIneligible() {
        Disaster flood = new Disaster("Major Flood", "Flood", "Rising waters", "Zone A", "Critical", 200);
        RescueTeam busyTeam = new RescueTeam(1, "Aquatic 1", "Leader 1", "+1", 10, "Flood Rescue", "Boats", "Busy");

        EligibilityAssessment eval = service.evaluateTeam(flood, busyTeam);
        assertFalse(eval.isEligible(), "Busy team must not be eligible");
        assertTrue(eval.getExplanation().contains("INELIGIBLE"));
    }

    @Test
    @DisplayName("Rule: Matching skills and equipment grant high eligibility score")
    void testMatchingSkillsGrantHighScore() {
        Disaster flood = new Disaster("Major Flood", "Flood", "Rising waters", "Zone A", "Critical", 200);
        RescueTeam aquaticTeam = new RescueTeam(2, "Aquatic Taskforce", "Leader 2", "+1", 12, "Flood Rescue, Swiftwater Navigation", "Inflatable Rafts", "Available");

        EligibilityAssessment eval = service.evaluateTeam(flood, aquaticTeam);
        assertTrue(eval.isEligible(), "Available team with aquatic skills should be eligible for Flood");
        assertTrue(eval.getScore() >= 100, "Match score should reflect strong skill and equipment alignment");
        assertTrue(eval.getExplanation().contains("Aquatic / Flood Rescue skills"));
    }

    @Test
    @DisplayName("Rule: Fire Brigade skills match Fire incidents")
    void testFireBrigadeMatching() {
        Disaster fire = new Disaster("Factory Fire", "Fire", "Chemical fire", "Industrial Area", "High", 50);
        RescueTeam fireTeam = new RescueTeam(3, "Hazmat & Fire", "Leader 3", "+1", 15, "Structural Firefighting, Smoke", "Fire Trucks, Breathing", "Available");

        EligibilityAssessment eval = service.evaluateTeam(fire, fireTeam);
        assertTrue(eval.isEligible());
        assertTrue(eval.getExplanation().contains("Fire suppression"));
    }
}
