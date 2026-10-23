package com.disastermanagement;

import com.disastermanagement.exception.InsufficientResourceException;
import com.disastermanagement.model.*;
import com.disastermanagement.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Java Object-Oriented Programming (OOP) concepts:
 * - Inheritance (Admin and RescueOfficer extending User)
 * - Polymorphism (hasAdminPrivileges, getRoleDescription)
 * - Encapsulation (Accessors and Mutators)
 * - Cryptographic password hashing
 * - Custom Exceptions
 */
public class ModelAndOOPTest {

    @Test
    @DisplayName("OOP Concept: Inheritance and Polymorphism in User Hierarchy")
    void testUserInheritanceAndPolymorphism() {
        // Polymorphic reference
        User admin = new Admin("Dr. Vance", "admin", "hash123", "+1-555-0100");
        User officer = new RescueOfficer("Capt. Connor", "officer1", "hash456", "+1-555-0200");
        User volunteer = new Volunteer("Emily Watson", "vol1", "hash789", "+1-555-0300", "Nursing");

        // Verify Polymorphic method behavior
        assertTrue(admin.hasAdminPrivileges(), "Admin should have admin privileges");
        assertFalse(officer.hasAdminPrivileges(), "Rescue Officer should not have admin privileges");
        assertFalse(volunteer.hasAdminPrivileges(), "Volunteer should not have admin privileges");

        assertEquals("Admin", admin.getRole());
        assertEquals("Rescue Officer", officer.getRole());
        assertEquals("Volunteer", volunteer.getRole());

        assertTrue(admin.getRoleDescription().contains("Administrator"));
        assertTrue(officer.getRoleDescription().contains("Rescue Officer"));
    }

    @Test
    @DisplayName("OOP Concept: Encapsulation and Invariants in Disaster & Shelter")
    void testEncapsulationAndCalculatedFields() {
        Disaster disaster = new Disaster("Flood Warning", "Flood", "Rising river", "River Basin", "Critical", 500);
        assertEquals(4, disaster.getSeverityPriority(), "Critical severity should have highest priority rank (4)");
        assertTrue(disaster.isActive());

        disaster.setSeverity("Low");
        assertEquals(1, disaster.getSeverityPriority(), "Low severity should rank 1");

        // Shelter capacity invariant calculations
        Shelter shelter = new Shelter(1, "Memorial Hall", "Downtown", 100, 75, "+1-555-001", "Open");
        assertEquals(25, shelter.getAvailableCapacity(), "Available capacity should be 100 - 75 = 25");
        assertEquals(75.0, shelter.getOccupancyPercentage(), 0.01);
        assertTrue(shelter.hasSpaceFor(20));
        assertFalse(shelter.hasSpaceFor(30), "Should reject space check exceeding capacity");
    }

    @Test
    @DisplayName("Security Concept: SHA-256 Cryptographic Password Hashing")
    void testPasswordHashing() {
        String raw = "admin123";
        String hash = PasswordUtil.hashPassword(raw);

        assertNotNull(hash);
        assertEquals(64, hash.length(), "SHA-256 hash must be 64 hexadecimal characters");
        assertTrue(PasswordUtil.verifyPassword("admin123", hash), "Password verification must succeed for valid credentials");
        assertFalse(PasswordUtil.verifyPassword("wrongpass", hash), "Password verification must fail for invalid credentials");
    }

    @Test
    @DisplayName("Custom Exception: InsufficientResourceException Properties")
    void testCustomException() {
        InsufficientResourceException ex = new InsufficientResourceException("Water Rations", 100, 40);
        assertEquals(100, ex.getRequested());
        assertEquals(40, ex.getAvailable());
        assertEquals("Water Rations", ex.getResourceName());
        assertTrue(ex.getMessage().contains("requested 100, available only 40"));
    }
}
