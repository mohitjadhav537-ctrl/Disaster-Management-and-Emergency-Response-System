package com.disastermanagement.service;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.ShelterDAO;
import com.disastermanagement.exception.CapacityExceededException;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.ValidationException;
import com.disastermanagement.model.Shelter;
import com.disastermanagement.model.ShelterRegistration;
import com.disastermanagement.model.User;
import com.disastermanagement.util.ValidationUtil;

import java.util.List;

/**
 * Service managing emergency shelter occupancy, capacities, and check-in transactions.
 */
public class ShelterService {

    private final ShelterDAO shelterDAO;
    private final AuditLogDAO auditDAO;

    public ShelterService() {
        this.shelterDAO = new ShelterDAO();
        this.auditDAO = new AuditLogDAO();
    }

    public void registerShelter(Shelter shelter, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requireNonEmpty(shelter.getShelterName(), "Shelter Name");
        ValidationUtil.requireNonEmpty(shelter.getLocation(), "Location");
        ValidationUtil.requirePositive(shelter.getTotalCapacity(), "Total Capacity");
        ValidationUtil.requireNonEmpty(shelter.getContactNumber(), "Contact Number");

        shelterDAO.insert(shelter);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "REGISTER_SHELTER", "Shelter", shelter.getShelterId(),
                String.format("Added shelter '%s' (Capacity: %d) at %s",
                        shelter.getShelterName(), shelter.getTotalCapacity(), shelter.getLocation()));
    }

    public void updateShelter(Shelter shelter, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requirePositive(shelter.getShelterId(), "Shelter ID");
        ValidationUtil.requireNonEmpty(shelter.getShelterName(), "Shelter Name");
        ValidationUtil.requirePositive(shelter.getTotalCapacity(), "Total Capacity");

        shelterDAO.update(shelter);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "UPDATE_SHELTER", "Shelter", shelter.getShelterId(),
                "Updated shelter details for " + shelter.getShelterName());
    }

    public boolean checkInEvacuees(int shelterId, int victimId, int peopleCount, User actor)
            throws DatabaseException, CapacityExceededException, ValidationException {
        
        ValidationUtil.requirePositive(peopleCount, "People Count");

        boolean success = shelterDAO.checkInVictim(shelterId, victimId, peopleCount);
        if (success) {
            int actorId = (actor != null) ? actor.getUserId() : 1;
            auditDAO.log(actorId, "SHELTER_CHECK_IN", "ShelterRegistration", shelterId,
                    String.format("Checked in %d evacuees (Victim #%d) into Shelter #%d",
                            peopleCount, victimId, shelterId));
        }
        return success;
    }

    public boolean checkOutEvacuees(int registrationId, User actor) throws DatabaseException {
        boolean success = shelterDAO.checkOutVictim(registrationId);
        if (success) {
            int actorId = (actor != null) ? actor.getUserId() : 1;
            auditDAO.log(actorId, "SHELTER_CHECK_OUT", "ShelterRegistration", registrationId,
                    "Checked out registration #" + registrationId);
        }
        return success;
    }

    public List<Shelter> getAllShelters() throws DatabaseException {
        return shelterDAO.findAll();
    }

    public List<ShelterRegistration> getAllRegistrations() throws DatabaseException {
        return shelterDAO.findAllRegistrations();
    }

    public int getTotalCapacity() throws DatabaseException {
        return shelterDAO.getTotalShelterCapacity();
    }

    public int getCurrentOccupancy() throws DatabaseException {
        return shelterDAO.getCurrentShelterOccupancy();
    }
}
