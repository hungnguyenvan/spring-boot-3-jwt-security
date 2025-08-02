-- Database schema for Spring Boot JWT Security Project
-- Compatible with PostgreSQL

-- Drop tables and sequences if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS editor_book_type_permission CASCADE;
DROP TABLE IF EXISTS user_profile CASCADE;
DROP TABLE IF EXISTS token CASCADE;
DROP TABLE IF EXISTS book CASCADE;
DROP TABLE IF EXISTS book_type CASCADE;
DROP TABLE IF EXISTS _user CASCADE;
DROP SEQUENCE IF EXISTS editor_permission_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_profile_id_seq CASCADE;
DROP SEQUENCE IF EXISTS _user_id_seq CASCADE;
DROP SEQUENCE IF EXISTS book_id_seq CASCADE;
DROP SEQUENCE IF EXISTS book_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS token_id_seq CASCADE;

-- Create sequences for auto-increment columns (match PostgreSQL naming convention)
CREATE SEQUENCE _user_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE book_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE book_type_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE token_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE user_profile_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE editor_permission_id_seq START 1 INCREMENT 1;

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
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20),
    description VARCHAR(1000),
    book_type_id INTEGER,
    
    -- File management fields
    file_path VARCHAR(500),
    file_name VARCHAR(100),
    file_format VARCHAR(10),
    file_size BIGINT,
    
    -- Pricing and access control
    is_free BOOLEAN NOT NULL DEFAULT TRUE,
    price DECIMAL(10,2),
    downloadable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Publication info
    publisher VARCHAR(255),
    publication_year INTEGER,
    language VARCHAR(50),
    page_count INTEGER,
    
    -- Rating and statistics
    rating DECIMAL(3,2) DEFAULT 0.00,
    download_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    last_modified_by INTEGER,
    
    CONSTRAINT fk_book_book_type FOREIGN KEY (book_type_id) REFERENCES book_type(id) ON DELETE SET NULL
);

-- Set sequence ownership
ALTER SEQUENCE book_id_seq OWNED BY book.id;

-- Create book_type table
CREATE TABLE book_type (
    id INTEGER NOT NULL DEFAULT nextval('book_type_id_seq') PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category VARCHAR(50),
    color_code VARCHAR(7),
    sort_order INTEGER DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    last_modified_by INTEGER
);

-- Set sequence ownership
ALTER SEQUENCE book_type_id_seq OWNED BY book_type.id;

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

-- Create editor_book_type_permission table
CREATE TABLE editor_book_type_permission (
    id INTEGER NOT NULL DEFAULT nextval('editor_permission_id_seq') PRIMARY KEY,
    user_id INTEGER NOT NULL,
    book_type_id INTEGER NOT NULL,
    can_edit BOOLEAN NOT NULL DEFAULT TRUE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    CONSTRAINT fk_editor_permission_user FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE,
    CONSTRAINT fk_editor_permission_book_type FOREIGN KEY (book_type_id) REFERENCES book_type(id) ON DELETE CASCADE,
    CONSTRAINT uk_editor_book_type UNIQUE (user_id, book_type_id)
);

-- Set sequence ownership
ALTER SEQUENCE editor_permission_id_seq OWNED BY editor_book_type_permission.id;

-- Create indexes for better performance
CREATE INDEX idx_user_email ON _user(email);
CREATE INDEX idx_user_username ON _user(username);
CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_value ON token(token);
CREATE INDEX idx_book_title ON book(title);
CREATE INDEX idx_book_author ON book(author);
CREATE INDEX idx_book_isbn ON book(isbn);
CREATE INDEX idx_book_type_id ON book(book_type_id);
CREATE INDEX idx_book_is_free ON book(is_free);
CREATE INDEX idx_book_downloadable ON book(downloadable);
CREATE INDEX idx_book_active ON book(active);
CREATE INDEX idx_book_file_format ON book(file_format);
CREATE INDEX idx_book_rating ON book(rating);
CREATE INDEX idx_user_profile_user_id ON user_profile(user_id);
CREATE INDEX idx_user_profile_activity_status ON user_profile(activity_status);
CREATE INDEX idx_user_profile_city ON user_profile(city);
CREATE INDEX idx_user_profile_country ON user_profile(country);
CREATE INDEX idx_book_type_name ON book_type(name);
CREATE INDEX idx_book_type_category ON book_type(category);
CREATE INDEX idx_book_type_active ON book_type(active);
CREATE INDEX idx_editor_permission_user_id ON editor_book_type_permission(user_id);
CREATE INDEX idx_editor_permission_book_type_id ON editor_book_type_permission(book_type_id);
CREATE INDEX idx_editor_permission_active ON editor_book_type_permission(active);

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

-- Sample book types
INSERT INTO book_type (name, description, active, category, color_code, sort_order) VALUES
('Tiểu thuyết', 'Các tác phẩm văn học tiểu thuyết', TRUE, 'FICTION', '#FF5733', 1),
('Khoa học kỹ thuật', 'Sách về khoa học và công nghệ', TRUE, 'TECHNICAL', '#3498DB', 2),
('Kinh doanh', 'Sách về quản trị và kinh doanh', TRUE, 'BUSINESS', '#2ECC71', 3),
('Lịch sử', 'Sách về lịch sử và văn hóa', TRUE, 'NON_FICTION', '#F39C12', 4),
('Giáo dục', 'Sách giáo khoa và tài liệu học tập', TRUE, 'ACADEMIC', '#9B59B6', 5);

-- Sample editor permissions (Editor user can edit Fiction and Business book types)
INSERT INTO editor_book_type_permission (user_id, book_type_id, can_edit, can_delete, active) VALUES
(2, 1, TRUE, FALSE, TRUE),  -- Editor can edit Fiction books
(2, 3, TRUE, TRUE, TRUE);   -- Editor can edit and delete Business books

-- Sample books with enhanced data
INSERT INTO book (title, author, isbn, description, book_type_id, file_path, file_name, file_format, file_size, is_free, price, downloadable, publisher, publication_year, language, page_count) VALUES
('Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', 'Hướng dẫn viết code sạch và hiệu quả cho developer', 2, '/books/technical/clean-code.pdf', 'clean-code.pdf', 'PDF', 15728640, FALSE, 29.99, TRUE, 'Prentice Hall', 2008, 'English', 464),
('Effective Java', 'Joshua Bloch', '978-0134685991', 'Best practices cho lập trình Java', 2, '/books/technical/effective-java.pdf', 'effective-java.pdf', 'PDF', 18350080, FALSE, 34.99, TRUE, 'Addison-Wesley', 2018, 'English', 416),
('Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', '978-0201633610', 'Các mẫu thiết kế phần mềm cơ bản', 2, '/books/technical/design-patterns.pdf', 'design-patterns.pdf', 'PDF', 12582912, TRUE, NULL, TRUE, 'Addison-Wesley', 1994, 'English', 395),
('Refactoring: Improving the Design of Existing Code', 'Martin Fowler', '978-0321127426', 'Kỹ thuật tái cấu trúc code hiệu quả', 2, '/books/technical/refactoring.pdf', 'refactoring.pdf', 'PDF', 14680064, FALSE, 39.99, TRUE, 'Addison-Wesley', 2019, 'English', 448),
('Test Driven Development: By Example', 'Kent Beck', '978-0321146533', 'Phương pháp phát triển phần mềm hướng test', 2, '/books/technical/tdd-by-example.pdf', 'tdd-by-example.pdf', 'PDF', 8388608, TRUE, NULL, TRUE, 'Addison-Wesley', 2002, 'English', 240),
('Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', '978-604-1-00001-1', 'Truyện thiếu nhi kinh điển Việt Nam', 1, '/books/fiction/de-men-phieu-luu-ky.pdf', 'de-men-phieu-luu-ky.pdf', 'PDF', 5242880, TRUE, NULL, TRUE, 'NXB Kim Đồng', 1941, 'Vietnamese', 180),
('Khởi Nghiệp Lean', 'Eric Ries', '978-604-1-12345-6', 'Phương pháp khởi nghiệp tinh gọn', 3, '/books/business/khoi-nghiep-lean.pdf', 'khoi-nghiep-lean.pdf', 'PDF', 10485760, FALSE, 19.99, TRUE, 'NXB Trẻ', 2011, 'Vietnamese', 320);

UPDATE book SET rating = 4.5, download_count = 1250, view_count = 3420 WHERE id = 1;
UPDATE book SET rating = 4.7, download_count = 980, view_count = 2150 WHERE id = 2;
UPDATE book SET rating = 4.3, download_count = 2100, view_count = 5680 WHERE id = 3;
UPDATE book SET rating = 4.4, download_count = 750, view_count = 1890 WHERE id = 4;
UPDATE book SET rating = 4.2, download_count = 1680, view_count = 3240 WHERE id = 5;
UPDATE book SET rating = 4.8, download_count = 5200, view_count = 12500 WHERE id = 6;
UPDATE book SET rating = 4.1, download_count = 340, view_count = 890 WHERE id = 7;

-- Add comments to tables
COMMENT ON TABLE _user IS 'User accounts table';
COMMENT ON COLUMN _user.role IS 'User role: USER, ADMIN, or EDITOR';
COMMENT ON COLUMN _user.locked IS 'Account lock status';

COMMENT ON TABLE token IS 'JWT tokens table for authentication';
COMMENT ON COLUMN token.token_type IS 'Token type, typically BEARER';
COMMENT ON COLUMN token.revoked IS 'Whether token has been revoked';
COMMENT ON COLUMN token.expired IS 'Whether token has expired';

COMMENT ON TABLE book IS 'Books management table with file storage and pricing';
COMMENT ON COLUMN book.file_path IS 'File storage path on server';
COMMENT ON COLUMN book.is_free IS 'Whether the book is free or requires payment';
COMMENT ON COLUMN book.downloadable IS 'Whether users can download the book file';
COMMENT ON COLUMN book.active IS 'Whether the book is active and available';

COMMENT ON TABLE user_profile IS 'User profile information table';
COMMENT ON COLUMN user_profile.activity_status IS 'User activity status: ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION, or BANNED';
COMMENT ON COLUMN user_profile.is_email_verified IS 'Email verification status';
COMMENT ON COLUMN user_profile.is_phone_verified IS 'Phone verification status';

COMMENT ON TABLE book_type IS 'Book types and categories table';
COMMENT ON COLUMN book_type.active IS 'Whether the book type is active/available';
COMMENT ON COLUMN book_type.category IS 'Book category: FICTION, NON_FICTION, ACADEMIC, TECHNICAL, BUSINESS, etc.';
COMMENT ON COLUMN book_type.color_code IS 'Hex color code for UI display';

COMMENT ON TABLE editor_book_type_permission IS 'Editor permissions for specific book types';
COMMENT ON COLUMN editor_book_type_permission.can_edit IS 'Whether editor can edit this book type';
COMMENT ON COLUMN editor_book_type_permission.can_delete IS 'Whether editor can delete this book type';
COMMENT ON COLUMN editor_book_type_permission.active IS 'Whether permission is active';

-- Show table information
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
    AND table_name IN ('_user', 'token', 'book', 'book_type', 'user_profile', 'editor_book_type_permission')
ORDER BY table_name, ordinal_position;
