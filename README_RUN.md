# Hướng dẫn chạy project Spring Boot JWT Security

## Yêu cầu hệ thống
- Java 17 hoặc cao hơn
- PostgreSQL 12 trở lên
- Maven 3.6+

## Cách chạy project

### 1. Chuẩn bị Database
```sql
-- Tạo database trong PostgreSQL
CREATE DATABASE jwt_security;

-- Tạo user (tùy chọn)
CREATE USER hungcop WITH PASSWORD 'hungcop290987';
GRANT ALL PRIVILEGES ON DATABASE jwt_security TO hungcop;
```

### 2. Chạy script tạo bảng
```bash
psql -U hungcop -d jwt_security -f database_schema.sql
```

### 3. Build và chạy project
```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc build jar và chạy
mvn clean package
java -jar target/spring-boot-3-jwt-security-0.0.1-SNAPSHOT.jar
```

### 4. Test API

#### Đăng nhập Admin:
```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@mail.com",
    "password": "password"
  }'
```

#### Đăng ký user mới:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe", 
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123",
    "role": "USER"
  }'
```

#### Lấy danh sách users (cần token admin):
```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Tài khoản mặc định
- **Admin**: admin@mail.com / password
- **Editor**: editor@mail.com / password  
- **User**: user@mail.com / password

## Cấu hình
- Database: `application.yml`
- JWT Secret: Được cấu hình trong `application.yml`
- Rate Limiting: 5 requests/phút cho endpoint đăng ký

## Swagger UI
Truy cập: http://localhost:8080/swagger-ui.html
