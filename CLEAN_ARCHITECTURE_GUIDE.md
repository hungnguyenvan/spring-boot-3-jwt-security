# Clean Architecture Implementation Summary for Library Management System

## **Architecture Overview**

Bạn đã có một hệ thống Spring Boot JWT security khá hoàn chỉnh. Để mở rộng thành một ứng dụng quản lý thư viện online với khả năng thay đổi dữ liệu linh hoạt, đây là cấu trúc Clean Architecture được đề xuất:

## **1. Layer Structure (Cấu trúc lớp)**

```
src/main/java/com/alibou/security/
├── core/                           # Core layer - Business logic independent
│   ├── domain/
│   │   ├── entity/                 # Domain entities
│   │   │   └── BaseEntity.java     # Base for all entities with audit
│   │   ├── service/                # Domain services
│   │   │   ├── PermissionService.java
│   │   │   └── BookTypePermission.java
│   │   └── repository/             # Repository interfaces
│   │       └── BaseRepository.java
│   ├── application/
│   │   └── service/                # Use cases / Application services
│   │       ├── BaseApplicationService.java
│   │       └── impl/
│   │           └── BaseApplicationServiceImpl.java
│   ├── infrastructure/
│   │   └── repository/             # Repository implementations
│   │       └── BaseRepositoryImpl.java
│   └── config/
│       └── JpaConfig.java          # JPA configuration
├── book/                           # Book bounded context
│   ├── application/
│   │   └── service/
│   │       ├── BookApplicationService.java
│   │       └── impl/
│   │           └── BookApplicationServiceImpl.java
│   ├── Book.java                   # Entity
│   ├── BookRepository.java         # Repository
│   ├── BookService.java           # Current service
│   ├── BookController.java        # REST controller
│   ├── BookRequest.java           # DTO
│   └── BookResponse.java          # DTO
├── booktype/                      # BookType bounded context
├── user/                          # User bounded context
└── auth/                          # Authentication bounded context
```

## **2. Key Benefits (Lợi ích chính)**

### **A. Separation of Concerns**
- **Domain Layer**: Business logic thuần túy, không phụ thuộc framework
- **Application Layer**: Use cases và orchestration logic
- **Infrastructure Layer**: Database, file system, external services
- **Presentation Layer**: REST controllers, validation

### **B. Flexibility for Changing Data**
- **BaseEntity**: Cung cấp audit fields và common behaviors cho tất cả entities
- **Generic Repository**: CRUD operations, search, pagination cho mọi entity
- **Domain Events**: Publish events khi entity state thay đổi
- **Specifications**: Dynamic query building

### **C. Permission System**
- **PermissionService**: Centralized business rules cho authorization
- **Role-based**: ADMIN > EDITOR > USER hierarchy
- **Granular Control**: Editor chỉ được phép sửa specific book types

## **3. Implementation Patterns**

### **A. Entity Design Pattern**
```java
// BaseEntity - Foundation for all entities
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue
    protected Integer id;
    
    // Audit fields
    protected LocalDateTime createdDate;
    protected LocalDateTime lastModifiedDate;
    protected Integer createdBy;
    protected Integer lastModifiedBy;
    protected Boolean active = true;
    
    // Domain events
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Common behaviors
    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
}

// Concrete entity extends BaseEntity
@Entity
public class Book extends BaseEntity {
    private String title;
    private String author;
    // ... other fields
    
    // Business methods
    public void incrementDownloadCount() { /* logic */ }
    public boolean isDownloadAllowed() { /* logic */ }
}
```

### **B. Repository Pattern**
```java
// Generic interface
public interface BaseRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    Page<T> findAll(Pageable pageable);
    Page<T> findAllActive(Pageable pageable);
    Page<T> search(String query, Pageable pageable);
    // ... other common operations
}

// Specific repository extends base
public interface BookRepository extends BaseRepository<Book, Integer> {
    Page<Book> findByBookTypeIdAndActiveTrue(Integer bookTypeId, Pageable pageable);
    List<Book> findTopByDownloadCountDesc(int limit);
    // ... book-specific queries
}
```

### **C. Application Service Pattern**
```java
// Base application service with common operations
public abstract class BaseApplicationServiceImpl<Entity, Request, Response, ID> {
    protected final BaseRepository<Entity, ID> repository;
    
    public Response create(Request request) {
        Entity entity = mapToEntity(request);
        entity.activate();
        Entity saved = repository.save(entity);
        return mapToResponse(saved);
    }
    
    // Abstract methods for concrete implementations
    protected abstract Entity mapToEntity(Request request);
    protected abstract Response mapToResponse(Entity entity);
}

// Concrete service extends base with specific business logic
@Service
public class BookApplicationServiceImpl 
    extends BaseApplicationServiceImpl<Book, BookRequest, BookResponse, Integer> {
    
    private final PermissionService permissionService;
    
    public String getDownloadUrl(Integer userId, Integer bookId) {
        if (!permissionService.canDownloadBook(user, bookId)) {
            throw new SecurityException("Access denied");
        }
        // Business logic for download
    }
}
```

## **4. Handling Dynamic Data Requirements**

### **A. Entity Flexibility**
- **BaseEntity**: Common audit và lifecycle management
- **JSON Columns**: Lưu metadata linh hoạt (PostgreSQL JSON/JSONB)
- **EAV Pattern**: Entity-Attribute-Value cho dynamic fields khi cần
- **Event Sourcing**: Track all changes với domain events

### **B. Permission Flexibility**
- **Role-based + Resource-based**: Combine role permissions với specific resource permissions
- **Permission Matrix**: EditorBookTypePermission table cho granular control
- **Dynamic Rules**: PermissionService có thể implement complex business rules

### **C. Search & Query Flexibility**
- **Specifications**: Dynamic query building với JPA Criteria API
- **Full-text Search**: PostgreSQL full-text search hoặc Elasticsearch
- **Caching**: Redis cho frequently accessed data

## **5. Migration Strategy**

### **Phase 1: Foundation**
1. Tạo BaseEntity và update existing entities
2. Implement BaseRepository pattern
3. Setup domain events infrastructure

### **Phase 2: Application Layer**
1. Create application services
2. Migrate business logic từ current services
3. Implement permission system

### **Phase 3: Enhancement**
1. Add search capabilities
2. Implement file management
3. Add caching layer

## **6. Technology Stack Recommendations**

- **Core**: Spring Boot 3.x, Spring Data JPA
- **Database**: PostgreSQL (JSON support, full-text search)
- **Caching**: Redis
- **File Storage**: Local filesystem hoặc AWS S3
- **Search**: PostgreSQL full-text hoặc Elasticsearch
- **Monitoring**: Micrometer, Actuator
- **Documentation**: OpenAPI 3.0

## **7. Best Practices**

1. **Domain-Driven Design**: Group entities theo business contexts
2. **CQRS**: Separate read/write models cho complex queries
3. **Event-Driven**: Use domain events cho loose coupling
4. **Validation**: Bean Validation ở DTO layer, business validation ở domain layer
5. **Error Handling**: Custom exceptions với proper HTTP status codes
6. **Testing**: Unit tests cho domain logic, integration tests cho repositories

Kiến trúc này sẽ cho phép hệ thống library management dễ dàng mở rộng và thích ứng với những thay đổi về dữ liệu trong tương lai.
