package com.disastermanagement.exception;

/**
 * Base custom database exception wrapping JDBC and SQL anomalies.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
