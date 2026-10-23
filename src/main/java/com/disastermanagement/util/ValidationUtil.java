package com.disastermanagement.util;

import com.disastermanagement.exception.ValidationException;

/**
 * Validation utility for user inputs, form entries, and business constraints.
 */
public class ValidationUtil {

    private ValidationUtil() {}

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void requirePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than zero.");
        }
    }

    public static void requireNonNegative(int value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validateRange(int value, int min, int max, String fieldName) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(String.format("%s must be between %d and %d.", fieldName, min, max));
        }
    }

    public static void validatePhone(String phone, String fieldName) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty()) {
            // Basic international or local phone pattern check
            if (!phone.matches("^[+0-9\\-\\s()]{7,25}$")) {
                throw new ValidationException(fieldName + " contains invalid telephone format.");
            }
        }
    }
}
