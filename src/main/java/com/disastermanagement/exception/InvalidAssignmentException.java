package com.disastermanagement.exception;

/**
 * Thrown when an invalid or conflicting rescue team allocation is attempted.
 * Examples: assigning a busy or off-duty team, duplicate active assignment,
 * or assigning a team lacking requisite skills/equipment.
 */
public class InvalidAssignmentException extends Exception {
    private final int disasterId;
    private final int teamId;

    public InvalidAssignmentException(String message) {
        super(message);
        this.disasterId = -1;
        this.teamId = -1;
    }

    public InvalidAssignmentException(String message, int disasterId, int teamId) {
        super(message);
        this.disasterId = disasterId;
        this.teamId = teamId;
    }

    public int getDisasterId() {
        return disasterId;
    }

    public int getTeamId() {
        return teamId;
    }
}
