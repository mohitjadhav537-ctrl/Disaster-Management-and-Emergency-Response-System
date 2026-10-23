package com.disastermanagement.service;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.HelpRequestDAO;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.ValidationException;
import com.disastermanagement.model.HelpRequest;
import com.disastermanagement.model.User;
import com.disastermanagement.util.ValidationUtil;

import java.util.List;

/**
 * Service managing emergency distress calls and victim help requests.
 */
public class HelpRequestService {

    private final HelpRequestDAO helpRequestDAO;
    private final AuditLogDAO auditDAO;

    public HelpRequestService() {
        this.helpRequestDAO = new HelpRequestDAO();
        this.auditDAO = new AuditLogDAO();
    }

    public void submitRequest(HelpRequest req, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requirePositive(req.getVictimId(), "Victim ID");
        ValidationUtil.requirePositive(req.getDisasterId(), "Disaster ID");
        ValidationUtil.requireNonEmpty(req.getRequestType(), "Request Type");
        ValidationUtil.requireNonEmpty(req.getPriority(), "Priority");

        helpRequestDAO.insert(req);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "SUBMIT_HELP_REQUEST", "HelpRequest", req.getRequestId(),
                String.format("New [%s] %s request logged for Victim #%d at Disaster #%d",
                        req.getPriority(), req.getRequestType(), req.getVictimId(), req.getDisasterId()));
    }

    public boolean updateStatus(int requestId, String status, Integer teamId, User actor) throws DatabaseException {
        boolean success = helpRequestDAO.updateStatus(requestId, status, teamId);
        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "UPDATE_HELP_REQUEST_STATUS", "HelpRequest", requestId,
                String.format("Updated request #%d status to '%s' (Assigned Team: %s)",
                        requestId, status, (teamId != null ? teamId : "None")));
        return success;
    }

    public List<HelpRequest> getAllRequests() throws DatabaseException {
        return helpRequestDAO.findAllWithDetails();
    }

    public int getPendingRequestsCount() throws DatabaseException {
        return helpRequestDAO.countPending();
    }
}
