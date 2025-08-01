-- Database schema for Spring Boot JWT Security Project
-- Compatible with PostgreSQL

-- Drop tables if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS token CASCADE;
DROP TABLE IF EXISTS book CASCADE;
DROP TABLE IF EXISTS _user CASCADE;

-- Create _user table
CREATE TABLE _user (
    id SERIAL PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ADMIN', 'EDITOR')),
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Create token table
CREATE TABLE token (
    id SERIAL PRIMARY KEY,
    token TEXT UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL DEFAULT 'BEARER',
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE
);

-- Create book table
CREATE TABLE book (
    id SERIAL PRIMARY KEY,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX idx_user_email ON _user(email);
CREATE INDEX idx_user_username ON _user(username);
CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_value ON token(token);
CREATE INDEX idx_book_isbn ON book(isbn);

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
