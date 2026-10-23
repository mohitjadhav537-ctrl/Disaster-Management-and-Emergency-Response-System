package com.disastermanagement.model;

/**
 * Interface representing any emergency response entity that can be assigned or tracked.
 * Demonstrates Abstraction and Interface design in Java OOP.
 */
public interface Assignable {
    int getAssignableId();
    String getAssignableName();
    String getAssignmentStatus();
    void setAssignmentStatus(String status);
}
