-- ============================================================================
-- Disaster Management and Emergency Response System
-- Database Schema: disaster_management_db
-- Normalized MySQL Schema (3NF) with Constraints, Indexes, and Foreign Keys
-- ============================================================================

CREATE DATABASE IF NOT EXISTS disaster_management_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE disaster_management_db;

-- ----------------------------------------------------------------------------
-- 1. Table: users
-- Core authentication and user management table with role-based access
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role ENUM('Admin', 'Rescue Officer', 'Volunteer') NOT NULL,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. Table: disasters
-- Disaster incidents tracked across lifecycles and severity levels
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS disasters (
    disaster_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    disaster_type ENUM('Flood', 'Earthquake', 'Fire', 'Cyclone', 'Landslide', 'Other') NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL,
    people_affected INT DEFAULT 0 CHECK (people_affected >= 0),
    status ENUM('Reported', 'Verified', 'Response in Progress', 'Resolved', 'Closed') DEFAULT 'Reported',
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_disaster_severity (severity),
    INDEX idx_disaster_status (status),
    INDEX idx_disaster_type (disaster_type)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. Table: rescue_teams
-- Emergency teams with specialist skills, gear, and availability
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rescue_teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL UNIQUE,
    team_leader VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    team_size INT NOT NULL CHECK (team_size > 0),
    skills VARCHAR(255) NOT NULL,
    equipment TEXT,
    availability ENUM('Available', 'Busy', 'Off-Duty') DEFAULT 'Available',
    INDEX idx_team_availability (availability)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. Table: assignments
-- Priority-based allocation of rescue teams to disaster incidents
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    team_id INT NOT NULL,
    assigned_by INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assignment_status ENUM('Active', 'Completed', 'Cancelled') DEFAULT 'Active',
    completed_at DATETIME NULL,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE RESTRICT,
    FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_assignment_status (assignment_status),
    INDEX idx_assignment_disaster (disaster_id),
    INDEX idx_assignment_team (team_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. Table: victims
-- Affected individuals and groups registered during incidents
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS victims (
    victim_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    age INT CHECK (age >= 0 AND age <= 125),
    contact_number VARCHAR(20),
    group_size INT DEFAULT 1 CHECK (group_size >= 1),
    location VARCHAR(255) NOT NULL,
    assistance_required VARCHAR(255) NOT NULL,
    medical_status VARCHAR(100) DEFAULT 'Normal',
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE RESTRICT,
    INDEX idx_victim_disaster (disaster_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 6. Table: help_requests
-- Emergency requests generated for victims with priority dispatching
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS help_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    victim_id INT NOT NULL,
    disaster_id INT NOT NULL,
    request_type ENUM('Medical assistance', 'Evacuation', 'Food and water', 'Shelter', 'Rescue assistance') NOT NULL,
    priority ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL,
    description TEXT,
    status ENUM('Pending', 'Assigned', 'In Progress', 'Resolved', 'Cancelled') DEFAULT 'Pending',
    assigned_team_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    FOREIGN KEY (victim_id) REFERENCES victims(victim_id) ON DELETE RESTRICT,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_team_id) REFERENCES rescue_teams(team_id) ON DELETE SET NULL,
    INDEX idx_request_status (status),
    INDEX idx_request_priority (priority)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. Table: resources
-- Relief inventory stock management with threshold alerts
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    category ENUM('Food', 'Water', 'Medical', 'Rescue Equipment', 'Shelter Supplies', 'Clothing', 'Other') NOT NULL,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit VARCHAR(30) NOT NULL,
    storage_location VARCHAR(150) NOT NULL,
    minimum_stock INT NOT NULL DEFAULT 10 CHECK (minimum_stock >= 0),
    INDEX idx_resource_category (category)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. Table: resource_distributions
-- Transactional tracking of relief supplies allocated to disaster zones
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resource_distributions (
    distribution_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id INT NOT NULL,
    disaster_id INT NOT NULL,
    quantity_distributed INT NOT NULL CHECK (quantity_distributed > 0),
    distributed_by INT NOT NULL,
    distribution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE RESTRICT,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE RESTRICT,
    FOREIGN KEY (distributed_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_dist_resource (resource_id),
    INDEX idx_dist_disaster (disaster_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 9. Table: shelters
-- Safe shelters, capacities, and live occupancy levels
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shelters (
    shelter_id INT AUTO_INCREMENT PRIMARY KEY,
    shelter_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    total_capacity INT NOT NULL CHECK (total_capacity > 0),
    current_occupancy INT NOT NULL DEFAULT 0 CHECK (current_occupancy >= 0),
    contact_number VARCHAR(20) NOT NULL,
    status ENUM('Open', 'Full', 'Closed') DEFAULT 'Open',
    CONSTRAINT chk_shelter_capacity CHECK (current_occupancy <= total_capacity),
    INDEX idx_shelter_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 10. Table: shelter_registrations
-- Tracking victims check-in and check-out at emergency shelters
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shelter_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    shelter_id INT NOT NULL,
    victim_id INT NOT NULL,
    people_count INT NOT NULL DEFAULT 1 CHECK (people_count > 0),
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    check_out_time DATETIME NULL,
    status ENUM('Active', 'Checked Out') DEFAULT 'Active',
    FOREIGN KEY (shelter_id) REFERENCES shelters(shelter_id) ON DELETE RESTRICT,
    FOREIGN KEY (victim_id) REFERENCES victims(victim_id) ON DELETE RESTRICT,
    INDEX idx_shelter_reg_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 11. Table: volunteers
-- Volunteer roster linked to registered user accounts
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS volunteers (
    volunteer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    skills VARCHAR(255) NOT NULL,
    availability ENUM('Available', 'Assigned', 'Unavailable') DEFAULT 'Available',
    assigned_task VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_volunteer_availability (availability)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 12. Table: audit_logs
-- System audit trail for security and regulatory compliance
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT NULL,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_audit_timestamp (action_timestamp)
) ENGINE=InnoDB;
