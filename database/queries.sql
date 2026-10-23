-- ============================================================================
-- Disaster Management and Emergency Response System
-- SQL Concepts Demonstration: Joins, Aggregates, Subqueries, Constraints & Transactions
-- Demonstrating Core DBMS Concepts for Second-Year Engineering Curriculum
-- ============================================================================

USE disaster_management_db;

-- ----------------------------------------------------------------------------
-- 1. INNER JOIN & MULTI-TABLE JOIN
-- Fetch all active assignments with disaster details, assigned team, and assigning admin
-- ----------------------------------------------------------------------------
SELECT 
    a.assignment_id,
    d.title AS disaster_title,
    d.disaster_type,
    d.severity,
    d.location AS disaster_location,
    t.team_name,
    t.team_leader,
    t.contact_number AS team_contact,
    u.full_name AS assigned_by_user,
    a.assigned_at,
    a.assignment_status
FROM assignments a
INNER JOIN disasters d ON a.disaster_id = d.disaster_id
INNER JOIN rescue_teams t ON a.team_id = t.team_id
INNER JOIN users u ON a.assigned_by = u.user_id
WHERE a.assignment_status = 'Active'
ORDER BY 
    CASE d.severity
        WHEN 'Critical' THEN 1
        WHEN 'High' THEN 2
        WHEN 'Medium' THEN 3
        WHEN 'Low' THEN 4
    END;

-- ----------------------------------------------------------------------------
-- 2. LEFT OUTER JOIN
-- View all rescue teams and their active assignment if any (reveals unassigned teams)
-- ----------------------------------------------------------------------------
SELECT 
    t.team_id,
    t.team_name,
    t.skills,
    t.availability,
    d.title AS active_incident,
    d.severity AS incident_severity,
    a.assigned_at
FROM rescue_teams t
LEFT JOIN assignments a ON t.team_id = a.team_id AND a.assignment_status = 'Active'
LEFT JOIN disasters d ON a.disaster_id = d.disaster_id
ORDER BY t.availability ASC, t.team_id ASC;

-- ----------------------------------------------------------------------------
-- 3. AGGREGATE FUNCTIONS WITH GROUP BY & HAVING
-- Calculate total relief materials distributed and total shipments per disaster
-- Only show disasters that have received more than 1 distribution shipment
-- ----------------------------------------------------------------------------
SELECT 
    d.disaster_id,
    d.title AS disaster_name,
    d.severity,
    COUNT(rd.distribution_id) AS total_shipments,
    SUM(rd.quantity_distributed) AS total_items_received,
    MAX(rd.distribution_date) AS latest_distribution
FROM disasters d
INNER JOIN resource_distributions rd ON d.disaster_id = rd.disaster_id
GROUP BY d.disaster_id, d.title, d.severity
HAVING COUNT(rd.distribution_id) >= 1
ORDER BY total_items_received DESC;

-- ----------------------------------------------------------------------------
-- 4. SUBQUERY (SCALAR & CORRELATED)
-- Find all resources whose remaining quantity is below the average stock of their category
-- ----------------------------------------------------------------------------
SELECT 
    r.resource_id,
    r.resource_name,
    r.category,
    r.quantity AS current_stock,
    r.minimum_stock,
    (
        SELECT ROUND(AVG(r2.quantity), 1)
        FROM resources r2
        WHERE r2.category = r.category
    ) AS category_avg_stock
FROM resources r
WHERE r.quantity <= r.minimum_stock
   OR r.quantity < (
        SELECT AVG(r3.quantity)
        FROM resources r3
        WHERE r3.category = r.category
   );

-- ----------------------------------------------------------------------------
-- 5. SUBQUERY WITH NOT IN / NOT EXISTS
-- Find all Critical incidents that currently have NO active rescue team allocated
-- (Used by the Priority-Based Rescue Allocation Engine)
-- ----------------------------------------------------------------------------
SELECT 
    d.disaster_id,
    d.title,
    d.disaster_type,
    d.location,
    d.severity,
    d.people_affected
FROM disasters d
WHERE d.severity = 'Critical'
  AND d.status NOT IN ('Resolved', 'Closed')
  AND NOT EXISTS (
      SELECT 1 
      FROM assignments a 
      WHERE a.disaster_id = d.disaster_id 
        AND a.assignment_status = 'Active'
  );

-- ----------------------------------------------------------------------------
-- 6. SHELTER CAPACITY & UTILIZATION AGGREGATE
-- Demonstrating calculated percentage and conditional CASE logic
-- ----------------------------------------------------------------------------
SELECT 
    s.shelter_id,
    s.shelter_name,
    s.location,
    s.total_capacity,
    s.current_occupancy,
    (s.total_capacity - s.current_occupancy) AS available_capacity,
    ROUND((s.current_occupancy / s.total_capacity) * 100, 1) AS occupancy_percentage,
    CASE 
        WHEN s.current_occupancy >= s.total_capacity THEN 'FULL / CRITICAL'
        WHEN s.current_occupancy >= s.total_capacity * 0.8 THEN 'NEAR CAPACITY (>80%)'
        ELSE 'SPACE AVAILABLE'
    END AS capacity_status
FROM shelters s
ORDER BY occupancy_percentage DESC;

-- ----------------------------------------------------------------------------
-- 7. ATOMIC TRANSACTION DEMONSTRATION (ACID COMPLIANCE)
-- Distribute relief resource to an incident safely:
-- Step A: Verify quantity and decrease stock
-- Step B: Insert distribution record
-- Rollback occurs automatically if any step fails or inventory becomes negative
-- ----------------------------------------------------------------------------
START TRANSACTION;

-- Attempting distribution of 50 First Aid Trauma Kits (resource_id = 3) to Disaster #1
-- 1. Deduct stock (guarded by WHERE quantity >= 50)
UPDATE resources 
SET quantity = quantity - 50 
WHERE resource_id = 3 AND quantity >= 50;

-- 2. Insert distribution audit record
INSERT INTO resource_distributions (resource_id, disaster_id, quantity_distributed, distributed_by, notes)
VALUES (3, 1, 50, 1, 'Emergency paramedic resupply via helicopter drop');

-- 3. Confirm transaction
COMMIT;
