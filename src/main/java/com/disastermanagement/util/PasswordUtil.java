package com.disastermanagement.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for cryptographic password hashing and verification.
 * Adheres to security requirement: Never store plain-text passwords.
 * Implements standard SHA-256 cryptographic hashing.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using SHA-256.
     * @param plainText the plain password
     * @return 64-character lowercase hexadecimal hash string
     */
    public static String hashPassword(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 cryptographic algorithm unavailable", e);
        }
    }

    /**
     * Verifies if a given plain-text password matches a stored SHA-256 hash.
     * @param plainText the candidate plain password
     * @param storedHash the stored hexadecimal hash
     * @return true if credentials match
     */
    public static boolean verifyPassword(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) {
            return false;
        }
        String computed = hashPassword(plainText);
        return computed.equalsIgnoreCase(storedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
