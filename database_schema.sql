-- Database schema for Spring Boot JWT Security Project
-- Compatible with PostgreSQL

-- Drop tables and sequences if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS user_profile CASCADE;
DROP TABLE IF EXISTS token CASCADE;
DROP TABLE IF EXISTS book CASCADE;
DROP TABLE IF EXISTS _user CASCADE;
DROP SEQUENCE IF EXISTS user_profile_id_seq CASCADE;
DROP SEQUENCE IF EXISTS _user_id_seq CASCADE;
DROP SEQUENCE IF EXISTS book_id_seq CASCADE;
DROP SEQUENCE IF EXISTS token_id_seq CASCADE;

-- Create sequences for auto-increment columns (match PostgreSQL naming convention)
CREATE SEQUENCE _user_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE book_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE token_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE user_profile_id_seq START 1 INCREMENT 1;

-- Create _user table
CREATE TABLE _user (
    id INTEGER NOT NULL DEFAULT nextval('_user_id_seq') PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'EDITOR')),
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    last_modified_by INTEGER
);

-- Set sequence ownership
ALTER SEQUENCE _user_id_seq OWNED BY _user.id;

-- Create token table
CREATE TABLE token (
    id INTEGER NOT NULL DEFAULT nextval('token_id_seq') PRIMARY KEY,
    token TEXT UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL DEFAULT 'BEARER',
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE
);

-- Set sequence ownership
ALTER SEQUENCE token_id_seq OWNED BY token.id;

-- Create book table
CREATE TABLE book (
    id INTEGER NOT NULL DEFAULT nextval('book_id_seq') PRIMARY KEY,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    last_modified_by INTEGER
);

-- Set sequence ownership
ALTER SEQUENCE book_id_seq OWNED BY book.id;

-- Create user_profile table
CREATE TABLE user_profile (
    id INTEGER NOT NULL DEFAULT nextval('user_profile_id_seq') PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    date_of_birth DATE,
    activity_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (activity_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'BANNED')),
    bio VARCHAR(1000),
    profile_image_url VARCHAR(500),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE
);

-- Set sequence ownership
ALTER SEQUENCE user_profile_id_seq OWNED BY user_profile.id;

-- Create indexes for better performance
CREATE INDEX idx_user_email ON _user(email);
CREATE INDEX idx_user_username ON _user(username);
CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_value ON token(token);
CREATE INDEX idx_book_isbn ON book(isbn);
CREATE INDEX idx_user_profile_user_id ON user_profile(user_id);
CREATE INDEX idx_user_profile_activity_status ON user_profile(activity_status);
CREATE INDEX idx_user_profile_city ON user_profile(city);
CREATE INDEX idx_user_profile_country ON user_profile(country);

-- Insert sample data
-- Admin user (password: "password" - encoded with BCrypt)
INSERT INTO _user (firstname, lastname, email, username, password, role, locked) VALUES
('Admin', 'Administrator', 'admin@mail.com', 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', FALSE);

-- Editor user (password: "password" - encoded with BCrypt)
INSERT INTO _user (firstname, lastname, email, username, password, role, locked) VALUES
('Editor', 'Editor', 'editor@mail.com', 'editor', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'EDITOR', FALSE);

-- Regular user (password: "password" - encoded with BCrypt)
INSERT INTO _user (firstname, lastname, email, username, password, role, locked) VALUES
('User', 'User', 'user@mail.com', 'user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', FALSE);

-- Sample user profiles
INSERT INTO user_profile (user_id, full_name, phone_number, address, city, country, activity_status, bio) VALUES
(1, 'Administrator User', '+84-123-456-789', '123 Admin Street', 'Ho Chi Minh City', 'Vietnam', 'ACTIVE', 'System administrator with full access privileges'),
(2, 'Editor User', '+84-987-654-321', '456 Editor Avenue', 'Hanoi', 'Vietnam', 'ACTIVE', 'Content editor and moderator'),
(3, 'Regular User', '+84-555-123-456', '789 User Boulevard', 'Da Nang', 'Vietnam', 'ACTIVE', 'Regular user with basic access rights');

-- Sample books
INSERT INTO book (author, isbn) VALUES
('Robert C. Martin', '978-0132350884'),
('Joshua Bloch', '978-0134685991'),
('Gang of Four', '978-0201633610'),
('Martin Fowler', '978-0321127426'),
('Kent Beck', '978-0321146533');

-- Add comments to tables
COMMENT ON TABLE _user IS 'User accounts table';
COMMENT ON COLUMN _user.role IS 'User role: USER, ADMIN, or EDITOR';
COMMENT ON COLUMN _user.locked IS 'Account lock status';

COMMENT ON TABLE token IS 'JWT tokens table for authentication';
COMMENT ON COLUMN token.token_type IS 'Token type, typically BEARER';
COMMENT ON COLUMN token.revoked IS 'Whether token has been revoked';
COMMENT ON COLUMN token.expired IS 'Whether token has expired';

COMMENT ON TABLE book IS 'Books management table';

COMMENT ON TABLE user_profile IS 'User profile information table';
COMMENT ON COLUMN user_profile.activity_status IS 'User activity status: ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION, or BANNED';
COMMENT ON COLUMN user_profile.is_email_verified IS 'Email verification status';
COMMENT ON COLUMN user_profile.is_phone_verified IS 'Phone verification status';

-- Show table information
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
    AND table_name IN ('_user', 'token', 'book')
ORDER BY table_name, ordinal_position;
