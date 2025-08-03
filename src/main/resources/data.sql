-- H2 Database initialization script
-- This will run automatically when app starts with H2

-- Create users
INSERT INTO _user (id, firstname, lastname, username, email, password, role, account_non_expired, account_non_locked, credentials_non_expired, enabled) VALUES
(1, 'Admin', 'User', 'admin', 'admin@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', true, true, true, true),
(2, 'Editor', 'User', 'editor', 'editor@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'EDITOR', true, true, true, true),
(3, 'Test', 'User', 'user', 'user@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', true, true, true, true);

-- Create document fields
INSERT INTO document_field (id, name, description, sort_order) VALUES
(1, 'Automotive', 'Automotive technical documents', 1),
(2, 'Electrical Bike', 'Electric bike technical documents', 2);

-- Create production years
INSERT INTO production_year (id, year, description, document_field_id, sort_order) VALUES
(1, 2020, '2020 Production Year', 1, 1),
(2, 2021, '2021 Production Year', 1, 2),
(3, 2022, '2022 Production Year', 2, 1);

-- Create manufacturers
INSERT INTO manufacturer (id, name, description, production_year_id, sort_order) VALUES
(1, 'Toyota', 'Toyota Motor Corporation', 1, 1),
(2, 'Honda', 'Honda Motor Company', 1, 2),
(3, 'Tesla', 'Tesla Electric Vehicles', 2, 1);

-- Create product series
INSERT INTO product_series (id, name, description, manufacturer_id, sort_order) VALUES
(1, 'Camry', 'Toyota Camry Series', 1, 1),
(2, 'Civic', 'Honda Civic Series', 2, 1),
(3, 'Model 3', 'Tesla Model 3 Series', 3, 1);

-- Create products
INSERT INTO product (id, name, description, specifications, product_series_id, sort_order) VALUES
(1, 'Camry 2.0', 'Toyota Camry 2.0L Engine', 'Engine: 2.0L, Power: 150HP', 1, 1),
(2, 'Civic Si', 'Honda Civic Si Performance', 'Engine: 1.5L Turbo, Power: 200HP', 2, 1),
(3, 'Model 3 Standard', 'Tesla Model 3 Standard Range', 'Battery: 54kWh, Range: 350km', 3, 1);

-- Default password for all test accounts is: password
