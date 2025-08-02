# 📋 **API ENDPOINTS DOCUMENTATION**
# Spring Boot JWT Security Project

## 🔐 **Authentication APIs** - `/api/v1/auth`

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| POST | `/api/v1/auth/register` | Đăng ký tài khoản mới | ❌ | - |
| POST | `/api/v1/auth/authenticate` | Đăng nhập và lấy JWT token | ❌ | - |
| POST | `/api/v1/auth/refresh-token` | Làm mới JWT token | ❌ | - |
| POST | `/api/v1/auth/reset-password` | Đặt lại mật khẩu | ❌ | - |
| POST | `/api/v1/auth/delete-user` | Xóa tài khoản (chỉ admin) | ✅ | ADMIN |

## 👥 **User Management APIs** - `/api/v1/users`

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| PATCH | `/api/v1/users` | Đổi mật khẩu của user hiện tại | ✅ | ANY |
| GET | `/api/v1/users` | Lấy danh sách users (phân trang, tìm kiếm) | ✅ | ANY |
| PUT | `/api/v1/users/role` | Cập nhật role của user | ✅ | ANY |
| PATCH | `/api/v1/users/lock` | Khóa/mở khóa tài khoản user | ✅ | ADMIN |

## 📚 **Book Management APIs** - `/api/v1/books`

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| POST | `/api/v1/books` | Tạo book mới | ✅ | ANY |
| GET | `/api/v1/books` | Lấy danh sách tất cả books | ✅ | ANY |

## 🎯 **Demo/Testing APIs**

### Demo Controller - `/api/v1/demo-controller`
| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/v1/demo-controller` | Test endpoint bảo mật cơ bản | ✅ | ANY |

### Admin Controller - `/api/v1/admin`
| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/v1/admin` | Admin read operations | ✅ | ADMIN + admin:read |
| POST | `/api/v1/admin` | Admin create operations | ✅ | ADMIN + admin:create |
| PUT | `/api/v1/admin` | Admin update operations | ✅ | ADMIN + admin:update |
| DELETE | `/api/v1/admin` | Admin delete operations | ✅ | ADMIN + admin:delete |

### Management Controller - `/api/v1/management`
| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/v1/management` | Management read operations | ✅ | EDITOR/ADMIN |
| POST | `/api/v1/management` | Management create operations | ✅ | EDITOR/ADMIN |
| PUT | `/api/v1/management` | Management update operations | ✅ | EDITOR/ADMIN |
| DELETE | `/api/v1/management` | Management delete operations | ✅ | EDITOR/ADMIN |

## 🏥 **System Health APIs** - `/actuator`

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/actuator/health` | Kiểm tra trạng thái hệ thống | ❌ | - |
| GET | `/actuator/info` | Thông tin ứng dụng | ❌ | - |
| GET | `/actuator/metrics` | Metrics hệ thống | ❌ | - |

---

## 🔑 **Authentication Flow**

### 1. Register New User
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstname": "John",
  "lastname": "Doe", 
  "email": "john@example.com",
  "password": "password123"
}
```

### 2. Login & Get Token
```bash
POST /api/v1/auth/authenticate
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response:
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Use Token for Protected APIs
```bash
GET /api/v1/books
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 👑 **Default Users**

| Email | Password | Role | Permissions |
|-------|----------|------|-------------|
| admin@mail.com | password | ADMIN | Full access |
| editor@mail.com | password | EDITOR | Management access |
| user@mail.com | password | USER | Basic access |

## 🎯 **Role-Based Access**

- **USER**: Truy cập basic APIs (books, demo, user profile)
- **EDITOR**: Truy cập management APIs + USER permissions  
- **ADMIN**: Full access + user management + admin APIs

## 📊 **API Response Formats**

### Success Response
```json
{
  "data": "...",
  "message": "Success",
  "status": 200
}
```

### Error Response  
```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2025-08-02T10:30:00"
}
```

## 🔧 **Testing Tools**

1. **curl** commands
2. **Postman** collection
3. **VS Code REST Client** (.http files)
4. **Custom test script** (comprehensive-api-test.sh)

---

## 🚀 **Recommendations for Production Enhancement**

### 1. **Enhanced Password Security**
```java
// Implement strong password policy
@Component
public class PasswordValidator {
    public void validatePassword(String password) {
        if (password.length() < 8 
            || !password.matches(".*[A-Z].*")
            || !password.matches(".*[a-z].*") 
            || !password.matches(".*[0-9].*")
            || !password.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException(
                "Password must be 8+ chars with uppercase, lowercase, number, and special character"
            );
        }
    }
}
```

### 2. **Security Audit Logging**
```java
// Add comprehensive audit trail
@EventListener
public class SecurityAuditListener {
    @EventListener
    public void handleAuthSuccess(AuthenticationSuccessEvent event) {
        auditService.logSecurityEvent("LOGIN_SUCCESS", event.getAuthentication());
    }
    
    @EventListener 
    public void handleAuthFailure(AbstractAuthenticationFailureEvent event) {
        auditService.logSecurityEvent("LOGIN_FAILED", event.getException());
    }
}
```

### 3. **Advanced Rate Limiting**
```java
// IP-based and user-based rate limiting
@Component
public class AdvancedRateLimitingFilter {
    // Global: 100 requests/minute per IP
    // Login: 5 attempts/15 minutes per IP
    // Registration: 3 attempts/hour per IP
    // User-specific: 1000 requests/hour per authenticated user
}
```

### 4. **Input Validation & Sanitization**
```java
// Add comprehensive input validation
@Valid @RequestBody RegisterRequest request
public class RegisterRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name can only contain letters and spaces")
    @Size(min = 2, max = 50, message = "Name must be between 2-50 characters")
    private String firstname;
}
```

### 5. **Session & Concurrent User Management**
```java
// Control concurrent sessions
@Override
public void configure(HttpSecurity http) {
    http.sessionManagement()
        .maximumSessions(3) // Max 3 concurrent sessions per user
        .maxSessionsPreventsLogin(false) // Kick out oldest session
        .sessionRegistry(sessionRegistry());
}
```

### 6. **Enhanced Error Messages**
```java
// Prevent information disclosure in error messages
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
    // Don't reveal whether email exists or password is wrong
    return ResponseEntity.status(401)
        .body(Map.of("error", "Invalid credentials", "code", "AUTH_001"));
}
```

### 7. **API Versioning Strategy**
```java
// Implement proper API versioning
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    // Current: v1 endpoints
}

@RestController  
@RequestMapping("/api/v2/auth")
public class AuthenticationControllerV2 {
    // Future: v2 with enhanced features
}
```

### 8. **Monitoring & Metrics**
```yaml
# application.yml - Enhanced monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    health:
      show-details: when-authorized
```

### 9. **Database Connection Security**
```yaml
# Enhanced database security
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      connection-timeout: 20000
      leak-detection-threshold: 60000
  jpa:
    show-sql: false # Never show SQL in production
    properties:
      hibernate:
        format_sql: false
```

### 10. **Production Security Headers**
```java
// Add security headers
@Configuration
public class SecurityHeadersConfig {
    @Bean
    public SecurityFilterChain addSecurityHeaders(HttpSecurity http) {
        return http
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
            )
            .build();
    }
}
```

---

**Total APIs: 21 endpoints across 6 controllers**
**Security Level: Production-Ready with Enhancement Recommendations**
