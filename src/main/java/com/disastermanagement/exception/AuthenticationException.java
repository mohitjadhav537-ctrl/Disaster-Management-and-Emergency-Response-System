package com.disastermanagement.exception;

/**
 * Thrown when user credentials or permissions fail authentication/authorization.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
