-- ============================================================================
-- Disaster Management and Emergency Response System
-- Realistic Sample Data Seed Script
-- All passwords are stored as SHA-256 hashes (never plain text)
-- 'admin123'    -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'rescue123'   -> a684b3979ea2ca9997193f2f8a42f6d21798ec4e1f7d5ca760086bc113b2c159
-- 'volunteer123'-> 1a7d65f5739bf8d3dfeb8bb1db4cfc3b392a83262438ea2316e6d7285183db76
-- ============================================================================

USE disaster_management_db;

-- Clear previous sample records if needed (respecting foreign key order)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE shelter_registrations;
TRUNCATE TABLE resource_distributions;
TRUNCATE TABLE help_requests;
TRUNCATE TABLE victims;
TRUNCATE TABLE assignments;
TRUNCATE TABLE volunteers;
TRUNCATE TABLE shelters;
TRUNCATE TABLE resources;
TRUNCATE TABLE rescue_teams;
TRUNCATE TABLE disasters;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 1. Insert Users
-- ----------------------------------------------------------------------------
INSERT INTO users (user_id, full_name, username, password_hash, phone, role, status) VALUES
(1, 'Dr. Robert Vance (Director)', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '+1-555-0100', 'Admin', 'Active'),
(2, 'Capt. Sarah Connor (NDRF Lead)', 'officer_sarah', '1ae660dae3897fd562e05c36ca0acd1fe7f1547a2f9e038d23b11c2dbc705573', '+1-555-0201', 'Rescue Officer', 'Active'),
(3, 'Lt. Marcus Brody (Coast Guard)', 'officer_marcus', '1ae660dae3897fd562e05c36ca0acd1fe7f1547a2f9e038d23b11c2dbc705573', '+1-555-0202', 'Rescue Officer', 'Active'),
(4, 'Emily Watson (Medical Volunteer)', 'volunteer_emily', '25a21eab5feca60534fc732ff65e27984b61e43d0c7a4614b9710cd01456c37a', '+1-555-0301', 'Volunteer', 'Active'),
(5, 'David Kim (Logistics Volunteer)', 'volunteer_david', '25a21eab5feca60534fc732ff65e27984b61e43d0c7a4614b9710cd01456c37a', '+1-555-0302', 'Volunteer', 'Active');

-- ----------------------------------------------------------------------------
-- 2. Insert Disaster Incidents
-- ----------------------------------------------------------------------------
INSERT INTO disasters (disaster_id, title, disaster_type, description, location, severity, people_affected, status, reported_at) VALUES
(1, 'River Valley Flash Flood', 'Flood', 'Catastrophic flash flooding following heavy torrential monsoon rains. 3 residential sectors inundated.', 'Sector 4 & 5, River Valley Basin', 'Critical', 450, 'Response in Progress', NOW() - INTERVAL 12 HOUR),
(2, 'Downtown High-Rise Commercial Fire', 'Fire', 'Multi-alarm industrial structure fire with heavy toxic smoke spreading to adjacent apartment blocks.', '742 Evergreen Terrace, Downtown', 'High', 120, 'Response in Progress', NOW() - INTERVAL 6 HOUR),
(3, 'Coastal Cyclone Warning & Storm Surge', 'Cyclone', 'Category 3 Cyclone approaching coastal shoreline with storm surge up to 4 meters anticipated.', 'Bay Area Coastal District', 'Critical', 1200, 'Reported', NOW() - INTERVAL 3 HOUR),
(4, 'Eastern Foothills Rockslide', 'Landslide', 'Sudden landslide blocking State Highway 10 and threatening 15 rural hillside residences.', 'Mile Marker 45, Foothills Highway', 'Medium', 35, 'Verified', NOW() - INTERVAL 1 DAY),
(5, 'Suburban Seismic Tremor', 'Earthquake', 'Magnitude 5.2 seismic tremor causing structural cracks in community hospital and local schools.', 'North Ridge Suburb', 'Low', 15, 'Resolved', NOW() - INTERVAL 3 DAY);

-- ----------------------------------------------------------------------------
-- 3. Insert Rescue Teams
-- ----------------------------------------------------------------------------
INSERT INTO rescue_teams (team_id, team_name, team_leader, contact_number, team_size, skills, equipment, availability) VALUES
(1, 'Alpha Aquatic Taskforce', 'Capt. Sarah Connor', '+1-555-0201', 12, 'Flood Rescue, Swiftwater Navigation, Boat Operation, CPR', '4 Inflatable Rafts, 30 Life Jackets, 2 Outboard Motors, Medical Kits', 'Busy'),
(2, 'Bravo Hazmat & Fire Brigade', 'Capt. Jason Hayes', '+1-555-0210', 16, 'Structural Firefighting, Smoke Extraction, Hazmat Containment, Search & Rescue', '2 Fire Trucks, Breathing Apparatus, Thermal Cameras, Hydraulic Cutters', 'Busy'),
(3, 'Charlie Mountain & Urban SAR', 'Lt. Marcus Brody', '+1-555-0202', 10, 'Canine Search, Trench Rescue, Drone Reconnaissance, Heavy Lifting', '2 Search Drones, Acoustic Listening Devices, Winches, Hydraulic Jacks', 'Available'),
(4, 'Delta Emergency Medical Squad', 'Dr. Angela Miller', '+1-555-0230', 8, 'Trauma Triage, Advanced Life Support, Mobile Surgical Care, Field First Aid', '2 Mobile Ambulances, Defibrillators, Oxygen Concentrators, Trauma Bags', 'Available'),
(5, 'Echo Rapid Evacuation Corps', 'Sgt. Frank Castle', '+1-555-0240', 14, 'Mass Evacuation, Shoreline Transport, Traffic Management, Crowd Control', '3 All-Terrain Troop Transports, Megaphones, High-Water Ford Vehicles', 'Available');

-- ----------------------------------------------------------------------------
-- 4. Insert Rescue Team Assignments
-- ----------------------------------------------------------------------------
INSERT INTO assignments (assignment_id, disaster_id, team_id, assigned_by, assigned_at, assignment_status) VALUES
(1, 1, 1, 1, NOW() - INTERVAL 10 HOUR, 'Active'),
(2, 2, 2, 1, NOW() - INTERVAL 5 HOUR, 'Active');

-- ----------------------------------------------------------------------------
-- 5. Insert Victims
-- ----------------------------------------------------------------------------
INSERT INTO victims (victim_id, disaster_id, full_name, age, contact_number, group_size, location, assistance_required, medical_status) VALUES
(1, 1, 'Johnathan Miller', 42, '+1-555-4001', 4, 'Roof of House 42, River Valley Sector 4', 'Rooftop evacuation needed; water rising fast', 'Hypothermia symptoms'),
(2, 1, 'Elena Rostova', 68, '+1-555-4002', 2, 'Apartment 2B, Greenview Avenue', 'Wheelchair-bound senior citizen needing transport', 'Diabetic, needs insulin & dry shelter'),
(3, 2, 'Carlos Mendoza', 35, '+1-555-4003', 1, '3rd Floor Balcony, Downtown Commercial Hub', 'Smoke inhalation, trapped by stairwell flames', 'Acute Respiratory Distress'),
(4, 3, 'Priya Sharma', 29, '+1-555-4004', 5, 'Fisherman Colony Pier 3, Bay Area', 'Early evacuation shelter for family with 2 toddlers', 'Normal'),
(5, 4, 'Robert Jenkins', 55, '+1-555-4005', 3, 'Mile Marker 46 Hill Cabin', 'Isolated by debris flow, power cut, road blocked', 'Minor abrasions');

-- ----------------------------------------------------------------------------
-- 6. Insert Help Requests
-- ----------------------------------------------------------------------------
INSERT INTO help_requests (request_id, victim_id, disaster_id, request_type, priority, description, status, assigned_team_id, created_at) VALUES
(1, 1, 1, 'Rescue assistance', 'Critical', 'Water reached 2nd floor ceiling; family of 4 stranded on sloping corrugated roof.', 'In Progress', 1, NOW() - INTERVAL 9 HOUR),
(2, 2, 1, 'Medical assistance', 'High', 'Elderly diabetic lady needs immediate insulin dose and dry transfer.', 'Assigned', 1, NOW() - INTERVAL 8 HOUR),
(3, 3, 2, 'Evacuation', 'Critical', 'Individual stranded on balcony due to burning stairwell; heavy black toxic smoke.', 'In Progress', 2, NOW() - INTERVAL 4 HOUR),
(4, 4, 3, 'Shelter', 'High', 'Coastal family needs early transport to inland concrete shelter ahead of storm surge.', 'Pending', NULL, NOW() - INTERVAL 2 HOUR),
(5, 5, 4, 'Food and water', 'Medium', 'Debris road cut-off; family requires 48-hour rations and potable water supply.', 'Pending', NULL, NOW() - INTERVAL 18 HOUR);

-- ----------------------------------------------------------------------------
-- 7. Insert Relief Resources
-- ----------------------------------------------------------------------------
INSERT INTO resources (resource_id, resource_name, category, quantity, unit, storage_location, minimum_stock) VALUES
(1, 'Ready-to-Eat Emergency Meal Rations', 'Food', 2500, 'Packets', 'Central Disaster Logistics Depot - Warehouse A', 500),
(2, 'Purified Drinking Water (1L Bottles)', 'Water', 4800, 'Bottles', 'Central Disaster Logistics Depot - Warehouse B', 1000),
(3, 'Comprehensive First Aid Trauma Kits', 'Medical', 120, 'Kits', 'Red Cross Medical Station - Bay 1', 30),
(4, 'Broad-Spectrum Antibiotics & Antiseptics', 'Medical', 45, 'Boxes', 'Red Cross Medical Station - Bay 2', 50), -- LOW STOCK WARNING
(5, 'Thermal Emergency Blankets', 'Shelter Supplies', 850, 'Pieces', 'Civil Defense Armory - Sector North', 200),
(6, 'High-Grade Rescue Life Jackets', 'Rescue Equipment', 25, 'Units', 'Port Authority Maritime Store', 50), -- LOW STOCK WARNING
(7, 'Heavy-Duty Inflatable Rescue Dinghies', 'Rescue Equipment', 6, 'Boats', 'Coast Guard Boathouse Pier 1', 4),
(8, 'Family Temporary Relief Tents (6-person)', 'Shelter Supplies', 180, 'Tents', 'Civil Defense Armory - Sector South', 40),
(9, 'Assorted Emergency Weather Clothing Kits', 'Clothing', 620, 'Sets', 'Community Relief Center Annex', 150);

-- ----------------------------------------------------------------------------
-- 8. Insert Resource Distributions
-- ----------------------------------------------------------------------------
INSERT INTO resource_distributions (distribution_id, resource_id, disaster_id, quantity_distributed, distributed_by, distribution_date, notes) VALUES
(1, 1, 1, 350, 1, NOW() - INTERVAL 8 HOUR, 'Initial ration delivery to Sector 4 flood relief staging point'),
(2, 2, 1, 600, 1, NOW() - INTERVAL 8 HOUR, 'Drinking water bottles dispatched via Zodiac rafts'),
(3, 3, 2, 25, 1, NOW() - INTERVAL 4 HOUR, 'Trauma kits dispatched to on-scene triage tent at Downtown fire'),
(4, 5, 1, 150, 1, NOW() - INTERVAL 7 HOUR, 'Thermal blankets provided to flood victims rescued from rooftop');

-- ----------------------------------------------------------------------------
-- 9. Insert Shelters
-- ----------------------------------------------------------------------------
INSERT INTO shelters (shelter_id, shelter_name, location, total_capacity, current_occupancy, contact_number, status) VALUES
(1, 'Civic Memorial Community Center', '100 Public Square, Midtown', 300, 85, '+1-555-7001', 'Open'),
(2, 'Westside Sports Complex Arena', '450 Stadium Way, Westside', 600, 240, '+1-555-7002', 'Open'),
(3, 'Highland Secondary School Gym', '1200 Highland Road, North Ridge', 200, 195, '+1-555-7003', 'Open'),
(4, 'Bay Shore Maritime Terminal Hall', 'Pier 9 Harbor Boulevard, Coastal District', 150, 150, '+1-555-7004', 'Full');

-- ----------------------------------------------------------------------------
-- 10. Insert Shelter Registrations
-- ----------------------------------------------------------------------------
INSERT INTO shelter_registrations (registration_id, shelter_id, victim_id, people_count, check_in_time, status) VALUES
(1, 1, 2, 2, NOW() - INTERVAL 6 HOUR, 'Active'),
(2, 2, 4, 5, NOW() - INTERVAL 1 HOUR, 'Active');

-- ----------------------------------------------------------------------------
-- 11. Insert Volunteers
-- ----------------------------------------------------------------------------
INSERT INTO volunteers (volunteer_id, user_id, skills, availability, assigned_task) VALUES
(1, 4, 'Registered Nurse, Triage Nursing, Wound Dressing', 'Assigned', 'Managing medical triage desk at Civic Memorial Center'),
(2, 5, 'Heavy Vehicle Driving, Forklift Certified, Inventory Tracking', 'Available', 'Standby for logistics truck dispatch');

-- ----------------------------------------------------------------------------
-- 12. Insert Audit Logs
-- ----------------------------------------------------------------------------
INSERT INTO audit_logs (log_id, user_id, action, entity_type, entity_id, action_timestamp, details) VALUES
(1, 1, 'USER_LOGIN', 'User', 1, NOW() - INTERVAL 12 HOUR, 'Administrator logged in from headquarters terminal'),
(2, 1, 'CREATE_DISASTER', 'Disaster', 1, NOW() - INTERVAL 12 HOUR, 'Registered new Critical Flood disaster at River Valley'),
(3, 1, 'ASSIGN_TEAM', 'Assignment', 1, NOW() - INTERVAL 10 HOUR, 'Allocated Alpha Aquatic Taskforce to River Valley Flood'),
(4, 1, 'DISTRIBUTE_RESOURCE', 'ResourceDistribution', 1, NOW() - INTERVAL 8 HOUR, 'Distributed 350 food packets to River Valley Flood');
