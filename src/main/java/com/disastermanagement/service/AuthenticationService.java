package com.disastermanagement.service;

import com.disastermanagement.dao.AuditLogDAO;
import com.disastermanagement.dao.UserDAO;
import com.disastermanagement.exception.AuthenticationException;
import com.disastermanagement.exception.DatabaseException;
import com.disastermanagement.exception.ValidationException;
import com.disastermanagement.model.User;
import com.disastermanagement.util.PasswordUtil;
import com.disastermanagement.util.ValidationUtil;

import java.util.Optional;

/**
 * Service orchestrating authentication, password verification, and session state.
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private final UserDAO userDAO;
    private final AuditLogDAO auditDAO;
    private User currentUser;

    private AuthenticationService() {
        this.userDAO = new UserDAO();
        this.auditDAO = new AuditLogDAO();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Authenticates a user by username and plain-text password.
     * Checks password hash using SHA-256.
     */
    public User login(String username, String plainPassword) throws AuthenticationException, DatabaseException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required.");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new AuthenticationException("Password is required.");
        }

        Optional<User> optUser = userDAO.findByUsername(username.trim());
        if (optUser.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = optUser.get();
        if (!user.isActive()) {
            throw new AuthenticationException("This account is inactive. Please contact an Administrator.");
        }

        boolean match = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        if (!match) {
            throw new AuthenticationException("Invalid username or password.");
        }

        this.currentUser = user;
        auditDAO.log(user.getUserId(), "LOGIN_SUCCESS", "User", user.getUserId(), 
                "User " + user.getUsername() + " successfully logged in with role: " + user.getRole());
        return user;
    }

    public void registerUser(User user, String plainPassword) throws DatabaseException, ValidationException {
        ValidationUtil.requireNonEmpty(user.getFullName(), "Full Name");
        ValidationUtil.requireNonEmpty(user.getUsername(), "Username");
        ValidationUtil.requireNonEmpty(plainPassword, "Password");
        ValidationUtil.requireNonEmpty(user.getRole(), "User Role");
        ValidationUtil.validatePhone(user.getPhone(), "Phone Number");

        if (plainPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        if (userDAO.existsByUsername(user.getUsername().trim())) {
            throw new ValidationException("Username '" + user.getUsername() + "' is already taken.");
        }

        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        userDAO.insertUser(user);

        int actorId = (currentUser != null) ? currentUser.getUserId() : user.getUserId();
        auditDAO.log(actorId, "REGISTER_USER", "User", user.getUserId(), 
                "Created user " + user.getUsername() + " with role " + user.getRole());
    }

    public void logout() {
        if (currentUser != null) {
            auditDAO.log(currentUser.getUserId(), "LOGOUT", "User", currentUser.getUserId(), 
                    "User " + currentUser.getUsername() + " logged out.");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
