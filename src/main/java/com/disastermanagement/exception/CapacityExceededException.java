package com.disastermanagement.exception;

/**
 * Thrown when registering evacuees would exceed total shelter capacity.
 */
public class CapacityExceededException extends Exception {
    private final int shelterId;
    private final int totalCapacity;
    private final int currentOccupancy;
    private final int incomingCount;

    public CapacityExceededException(int shelterId, int totalCapacity, int currentOccupancy, int incomingCount) {
        super(String.format("Shelter #%d capacity exceeded: current occupancy %d + incoming %d > total capacity %d",
                shelterId, currentOccupancy, incomingCount, totalCapacity));
        this.shelterId = shelterId;
        this.totalCapacity = totalCapacity;
        this.currentOccupancy = currentOccupancy;
        this.incomingCount = incomingCount;
    }

    public int getShelterId() {
        return shelterId;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public int getIncomingCount() {
        return incomingCount;
    }
}
