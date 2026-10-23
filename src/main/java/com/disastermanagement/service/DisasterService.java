package com.disastermanagement.service;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.DisasterDAO;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.ValidationException;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.User;
import com.disastermanagement.util.ValidationUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing disaster incident lifecycle operations and validations.
 */
public class DisasterService {

    private final DisasterDAO disasterDAO;
    private final AuditLogDAO auditDAO;

    public DisasterService() {
        this.disasterDAO = new DisasterDAO();
        this.auditDAO = new AuditLogDAO();
    }

    public void registerDisaster(Disaster d, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requireNonEmpty(d.getTitle(), "Disaster Title");
        ValidationUtil.requireNonEmpty(d.getDisasterType(), "Disaster Type");
        ValidationUtil.requireNonEmpty(d.getLocation(), "Location");
        ValidationUtil.requireNonEmpty(d.getSeverity(), "Severity");
        ValidationUtil.requireNonNegative(d.getPeopleAffected(), "People Affected");

        disasterDAO.insert(d);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "CREATE_DISASTER", "Disaster", d.getDisasterId(),
                String.format("Reported %s disaster: '%s' at %s (Severity: %s, Affected: %d)",
                        d.getDisasterType(), d.getTitle(), d.getLocation(), d.getSeverity(), d.getPeopleAffected()));
    }

    public void updateDisaster(Disaster d, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requirePositive(d.getDisasterId(), "Disaster ID");
        ValidationUtil.requireNonEmpty(d.getTitle(), "Disaster Title");
        ValidationUtil.requireNonEmpty(d.getLocation(), "Location");
        ValidationUtil.requireNonNegative(d.getPeopleAffected(), "People Affected");

        disasterDAO.update(d);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "UPDATE_DISASTER", "Disaster", d.getDisasterId(),
                String.format("Updated disaster #%d: '%s' status=%s, severity=%s",
                        d.getDisasterId(), d.getTitle(), d.getStatus(), d.getSeverity()));
    }

    public boolean deleteOrClose(int disasterId, User actor) throws DatabaseException {
        boolean res = disasterDAO.delete(disasterId);
        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "DELETE_OR_CLOSE_DISASTER", "Disaster", disasterId,
                "Closed or deleted disaster incident #" + disasterId);
        return res;
    }

    public Optional<Disaster> getDisaster(int disasterId) throws DatabaseException {
        return disasterDAO.findById(disasterId);
    }

    public List<Disaster> getAllDisasters() throws DatabaseException {
        return disasterDAO.findAll();
    }

    public List<Disaster> getActiveDisasters() throws DatabaseException {
        return disasterDAO.findActiveDisasters();
    }

    public List<Disaster> searchDisasters(String query, String type, String severity, String status) throws DatabaseException {
        return disasterDAO.search(query, type, severity, status);
    }

    public Map<String, Integer> getSeverityCounts() throws DatabaseException {
        return disasterDAO.getSeveritySummary();
    }

    public Map<String, Integer> getTypeCounts() throws DatabaseException {
        return disasterDAO.getTypeSummary();
    }
}
