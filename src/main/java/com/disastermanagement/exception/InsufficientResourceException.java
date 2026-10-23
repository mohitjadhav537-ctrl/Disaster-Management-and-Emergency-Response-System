package com.disastermanagement.exception;

/**
 * Thrown when requested relief resource distribution quantity exceeds available inventory.
 * Enforces non-negative stock invariants across atomic transactions.
 */
public class InsufficientResourceException extends Exception {
    private final int requested;
    private final int available;
    private final String resourceName;

    public InsufficientResourceException(String resourceName, int requested, int available) {
        super(String.format("Insufficient stock for resource '%s': requested %d, available only %d", 
                resourceName, requested, available));
        this.resourceName = resourceName;
        this.requested = requested;
        this.available = available;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }

    public String getResourceName() {
        return resourceName;
    }
}
