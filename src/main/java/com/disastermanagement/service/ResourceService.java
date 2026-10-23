package com.disastermanagement.service;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.ResourceDAO;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.InsufficientResourceException;
import com.disastermanagement.exception.ValidationException;
import com.disastermanagement.model.Resource;
import com.disastermanagement.model.ResourceDistribution;
import com.disastermanagement.model.User;
import com.disastermanagement.util.ValidationUtil;

import java.util.List;

/**
 * Service managing relief resources, inventory stock levels, and distribution transactions.
 */
public class ResourceService {

    private final ResourceDAO resourceDAO;
    private final AuditLogDAO auditDAO;

    public ResourceService() {
        this.resourceDAO = new ResourceDAO();
        this.auditDAO = new AuditLogDAO();
    }

    public void addResource(Resource res, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requireNonEmpty(res.getResourceName(), "Resource Name");
        ValidationUtil.requireNonEmpty(res.getCategory(), "Category");
        ValidationUtil.requireNonNegative(res.getQuantity(), "Quantity");
        ValidationUtil.requireNonEmpty(res.getUnit(), "Unit");
        ValidationUtil.requireNonEmpty(res.getStorageLocation(), "Storage Location");
        ValidationUtil.requireNonNegative(res.getMinimumStock(), "Minimum Stock");

        resourceDAO.insert(res);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "ADD_RESOURCE", "Resource", res.getResourceId(),
                String.format("Added %d %s of '%s' [%s] to %s",
                        res.getQuantity(), res.getUnit(), res.getResourceName(), res.getCategory(), res.getStorageLocation()));
    }

    public void updateResource(Resource res, User actor) throws DatabaseException, ValidationException {
        ValidationUtil.requirePositive(res.getResourceId(), "Resource ID");
        ValidationUtil.requireNonEmpty(res.getResourceName(), "Resource Name");
        ValidationUtil.requireNonNegative(res.getQuantity(), "Quantity");

        resourceDAO.update(res);

        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "UPDATE_RESOURCE", "Resource", res.getResourceId(),
                String.format("Updated resource #%d '%s' new quantity=%d",
                        res.getResourceId(), res.getResourceName(), res.getQuantity()));
    }

    public boolean deleteResource(int resourceId, User actor) throws DatabaseException {
        boolean success = resourceDAO.delete(resourceId);
        int actorId = (actor != null) ? actor.getUserId() : 1;
        auditDAO.log(actorId, "DELETE_RESOURCE", "Resource", resourceId,
                "Deleted relief supply item #" + resourceId);
        return success;
    }

    public boolean distributeResource(int resourceId, int disasterId, int quantity, String notes, User actor)
            throws DatabaseException, InsufficientResourceException, ValidationException {
        
        ValidationUtil.requirePositive(quantity, "Distributed Quantity");

        int actorId = (actor != null) ? actor.getUserId() : 1;
        boolean success = resourceDAO.distributeResource(resourceId, disasterId, quantity, actorId, notes);

        if (success) {
            auditDAO.log(actorId, "DISTRIBUTE_RESOURCE", "ResourceDistribution", resourceId,
                    String.format("Dispatched %d units of resource #%d to disaster #%d. Notes: %s",
                            quantity, resourceId, disasterId, notes));
        }
        return success;
    }

    public List<Resource> getAllResources() throws DatabaseException {
        return resourceDAO.findAll();
    }

    public List<Resource> getLowStockAlerts() throws DatabaseException {
        return resourceDAO.findLowStock();
    }

    public List<ResourceDistribution> getDistributionHistory() throws DatabaseException {
        return resourceDAO.findAllDistributions();
    }
}
