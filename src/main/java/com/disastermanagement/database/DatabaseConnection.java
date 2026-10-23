package com.disastermanagement.database;

import com.disastermanagement.exception.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager providing reliable JDBC connectivity,
 * connection pooling semantics, transaction lifecycle controls, and property configuration.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final Properties properties = new Properties();
    private Connection activeConnection;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;

    private DatabaseConnection() {
        loadConfiguration();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Loads database credentials and options from config/db.properties or classpath fallback,
     * with optional environment variable overrides.
     */
    private void loadConfiguration() {
        boolean loaded = false;

        // Candidate paths for external config/db.properties
        File[] candidateFiles = new File[]{
                new File("config/db.properties"),
                new File("d:/oops cp/config/db.properties"),
                new File("D:\\oops cp\\config\\db.properties"),
                new File(System.getProperty("user.dir"), "config/db.properties")
        };

        for (File externalConfigFile : candidateFiles) {
            if (externalConfigFile.exists() && externalConfigFile.isFile()) {
                try (InputStream is = new FileInputStream(externalConfigFile)) {
                    properties.load(is);
                    loaded = true;
                    System.out.println("[DB] Loaded configuration from " + externalConfigFile.getAbsolutePath());
                    break;
                } catch (Exception e) {
                    System.err.println("[DB] Warning: Could not read " + externalConfigFile.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

        // 2. Fallback to classpath /db.properties
        if (!loaded) {
            try (InputStream is = getClass().getResourceAsStream("/db.properties")) {
                if (is != null) {
                    properties.load(is);
                    loaded = true;
                    System.out.println("[DB] Loaded configuration from classpath /db.properties");
                }
            } catch (Exception e) {
                System.err.println("[DB] Warning: Could not read classpath /db.properties: " + e.getMessage());
            }
        }

        // Extract settings with defaults
        String host = getPropOrEnv("db.host", "DB_HOST", "localhost");
        String port = getPropOrEnv("db.port", "DB_PORT", "3306");
        String dbName = getPropOrEnv("db.database", "DB_NAME", "disaster_management_db");
        this.dbUsername = getPropOrEnv("db.username", "DB_USER", "root");
        this.dbPassword = getPropOrEnv("db.password", "DB_PASSWORD", "mohit@123");
        this.dbDriver = getPropOrEnv("db.driver", "DB_DRIVER", "com.mysql.cj.jdbc.Driver");

        String defaultUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                host, port, dbName);
        this.dbUrl = properties.getProperty("db.url", defaultUrl);

        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Error: MySQL JDBC Driver not found on classpath: " + dbDriver);
        }
    }

    private String getPropOrEnv(String propKey, String envKey, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        return properties.getProperty(propKey, defaultValue).trim();
    }

    /**
     * Obtains an active, valid JDBC connection to the MySQL database.
     */
    public synchronized Connection getConnection() throws DatabaseException {
        try {
            if (activeConnection == null || activeConnection.isClosed() || !activeConnection.isValid(2)) {
                activeConnection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            }
            return activeConnection;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish MySQL connection: " + e.getMessage() + 
                    "\nURL: " + dbUrl + " (User: " + dbUsername + ")\nCheck that MySQL is running and credentials in config/db.properties are correct.", e);
        }
    }

    /**
     * Creates a new dedicated connection for isolated transactions.
     */
    public Connection createNewConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create dedicated connection: " + e.getMessage(), e);
        }
    }

    /**
     * Tests if MySQL server is reachable and database is accessible.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void closeConnection() {
        if (activeConnection != null) {
            try {
                if (!activeConnection.isClosed()) {
                    activeConnection.close();
                }
            } catch (SQLException ignored) {}
            activeConnection = null;
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }
}
