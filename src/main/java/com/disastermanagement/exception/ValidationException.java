package com.disastermanagement.exception;

/**
 * Thrown when input validation fails on business logic models or forms.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
