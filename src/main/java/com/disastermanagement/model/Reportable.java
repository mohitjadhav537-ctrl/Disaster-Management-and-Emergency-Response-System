package com.disastermanagement.model;

/**
 * Interface for domain models that can be exported into tabular reports or CSV.
 * Demonstrates Abstraction and Interface Polymorphism.
 */
public interface Reportable {
    String[] getReportHeaders();
    Object[] getReportRowData();
}
