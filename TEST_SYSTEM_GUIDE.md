# 🧪 HƯỚNG DẪN KIỂM TRA HỆ THỐNG API

## 📋 **CHUẨN BỊ KIỂM TRA**

### **1. Khởi động ứng dụng:**
```bash
# Chạy PostgreSQL database
docker-compose up -d postgres

# Khởi động Spring Boot application
mvn spring-boot:run

# Hoặc sử dụng VS Code Task
# Ctrl+Shift+P > Tasks: Run Task > "Run Spring Boot Application"
```

### **2. Endpoints cơ bản:**
- **Base URL**: `http://localhost:8080`
- **API Docs**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`

### **3. Authentication:**
```bash
# Đăng ký tài khoản mới
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Test",
    "lastname": "User", 
    "email": "test@example.com",
    "password": "password123"
  }'

# Đăng nhập để lấy JWT token
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

## 🎯 **TEST CASES CHI TIẾT**

### **Test Case 1: Tạo và Quản Lý Lĩnh Vực Tài Liệu**

#### **1.1. Tạo lĩnh vực "Auto":**
```bash
curl -X POST http://localhost:8080/api/v1/document-fields \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Auto",
    "code": "AUTO",
    "description": "Automotive documents and manuals",
    "color": "#FF5733",
    "sortOrder": 1
  }'
```

#### **1.2. Tạo lĩnh vực "Electrical Bike":**
```bash
curl -X POST http://localhost:8080/api/v1/document-fields \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electrical Bike",
    "code": "EBIKE", 
    "description": "Electric bicycle technical documents",
    "color": "#33C3FF",
    "sortOrder": 2
  }'
```

#### **1.3. Kiểm tra danh sách lĩnh vực:**
```bash
curl -X GET http://localhost:8080/api/v1/document-fields \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Auto",
    "code": "AUTO",
    "description": "Automotive documents and manuals",
    "color": "#FF5733",
    "sortOrder": 1,
    "active": true,
    "totalDocumentCount": 0
  },
  {
    "id": 2,
    "name": "Electrical Bike", 
    "code": "EBIKE",
    "description": "Electric bicycle technical documents",
    "color": "#33C3FF",
    "sortOrder": 2,
    "active": true,
    "totalDocumentCount": 0
  }
]
```

---

### **Test Case 2: Xây Dựng Hierarchy Hoàn Chỉnh**

#### **2.1. Tạo năm sản xuất cho Auto:**
```bash
curl -X POST http://localhost:8080/api/v1/production-years \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2008,
    "fieldId": 1,
    "description": "Automotive products from 2008"
  }'
```

#### **2.2. Tạo nhà sản xuất Toyota:**
```bash
curl -X POST http://localhost:8080/api/v1/manufacturers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Toyota",
    "yearId": 1,
    "description": "Toyota Motor Corporation",
    "website": "https://toyota.com",
    "sortOrder": 1
  }'
```

#### **2.3. Tạo dòng sản phẩm Mazda:**
```bash
curl -X POST http://localhost:8080/api/v1/product-series \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mazda",
    "manufacturerId": 1,
    "description": "Mazda series vehicles",
    "sortOrder": 1
  }'
```

#### **2.4. Tạo sản phẩm Mazda2:**
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mazda2",
    "seriesId": 1,
    "description": "Mazda2 compact car",
    "specifications": {
      "engine": "1.5L",
      "transmission": "Manual/Automatic",
      "fuelType": "Gasoline"
    },
    "sortOrder": 1
  }'
```

#### **2.5. Tạo tài liệu kỹ thuật:**
```bash
curl -X POST http://localhost:8080/api/v1/technical-documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Engine Schematic",
    "documentType": "ENGINE_SCHEMATIC",
    "description": "Detailed engine schematic and parts diagram",
    "category": "MECHANICAL",
    "productId": 1,
    "fileName": "mazda2_engine_schematic.pdf",
    "filePath": "/documents/auto/2008/toyota/mazda/mazda2/engine_schematic.pdf",
    "fileFormat": "PDF",
    "version": "v1.0",
    "language": "EN",
    "isPublic": true,
    "sortOrder": 1
  }'
```

---

### **Test Case 3: Kiểm Tra Tìm Kiếm Phân Cấp**

#### **3.1. Tìm kiếm theo hierarchy đầy đủ:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/search?fieldName=Auto&year=2008&manufacturerName=Toyota&seriesName=Mazda&productName=Mazda2&documentType=ENGINE_SCHEMATIC" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **3.2. Tìm kiếm toàn văn:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/search?query=engine+mazda" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **3.3. Tìm theo lĩnh vực và năm:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/search?fieldName=Auto&year=2008" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Engine Schematic",
      "documentType": "ENGINE_SCHEMATIC",
      "fieldName": "Auto",
      "year": 2008,
      "manufacturerName": "Toyota",
      "seriesName": "Mazda",
      "productName": "Mazda2",
      "hierarchyPath": "Auto / 2008 / Toyota / Mazda / Mazda2 / Engine Schematic",
      "downloadCount": 0,
      "viewCount": 0,
      "rating": 0.0
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "total": 1
  }
}
```

---

### **Test Case 4: Kiểm Tra Tương Tác Người Dùng**

#### **4.1. Đánh dấu đã xem tài liệu:**
```bash
curl -X POST http://localhost:8080/api/v1/technical-documents/1/view \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **4.2. Tải xuống tài liệu:**
```bash
curl -X POST http://localhost:8080/api/v1/technical-documents/1/download \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **4.3. Đánh giá tài liệu:**
```bash
curl -X POST http://localhost:8080/api/v1/technical-documents/1/rate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"rating": 4.5}'
```

#### **4.4. Kiểm tra thống kê:**
```bash
curl -X GET http://localhost:8080/api/v1/technical-documents/statistics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "totalActive": 1,
  "totalPublic": 1,
  "totalDownloads": 1,
  "totalViews": 1,
  "averageRating": 4.5
}
```

---

### **Test Case 5: Kiểm Tra Tài Liệu Phổ Biến**

#### **5.1. Lấy tài liệu phổ biến nhất:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/popular?limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **5.2. Lấy tài liệu đánh giá cao:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/highly-rated?limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### **5.3. Lấy tài liệu mới nhất:**
```bash
curl -X GET "http://localhost:8080/api/v1/technical-documents/recent?limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔧 **AUTOMATED TESTING**

### **1. Tạo test script bash:**
```bash
#!/bin/bash
# test-api.sh

BASE_URL="http://localhost:8080"
TOKEN=""

# Function to get JWT token
get_token() {
  RESPONSE=$(curl -s -X POST $BASE_URL/api/v1/auth/authenticate \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"password123"}')
  TOKEN=$(echo $RESPONSE | jq -r '.access_token')
  echo "Token: $TOKEN"
}

# Function to test document field creation
test_create_field() {
  echo "Testing document field creation..."
  curl -X POST $BASE_URL/api/v1/document-fields \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Auto",
      "code": "AUTO",
      "description": "Automotive documents",
      "color": "#FF5733",
      "sortOrder": 1
    }' | jq .
}

# Function to test hierarchy search
test_hierarchy_search() {
  echo "Testing hierarchy search..."
  curl -X GET "$BASE_URL/api/v1/technical-documents/search?fieldName=Auto&year=2008" \
    -H "Authorization: Bearer $TOKEN" | jq .
}

# Main test execution
echo "Starting API tests..."
get_token
test_create_field
test_hierarchy_search
echo "Tests completed!"
```

### **2. Chạy test script:**
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 📊 **PERFORMANCE TESTING**

### **1. Load testing với Apache Bench:**
```bash
# Test đăng nhập
ab -n 100 -c 10 -p login.json -T application/json http://localhost:8080/api/v1/auth/authenticate

# Test tìm kiếm tài liệu
ab -n 1000 -c 50 -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/v1/technical-documents/search?query=engine
```

### **2. Database performance:**
```sql
-- Kiểm tra slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
WHERE mean_time > 100 
ORDER BY mean_time DESC;

-- Kiểm tra index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes 
ORDER BY idx_scan DESC;
```

---

## ✅ **VERIFICATION CHECKLIST**

### **Functional Tests:**
- [ ] ✅ User authentication & authorization
- [ ] ✅ Document field CRUD operations
- [ ] ✅ Hierarchy navigation (Field → Year → Manufacturer → Series → Product → Document)  
- [ ] ✅ Document search across all levels
- [ ] ✅ File upload and download
- [ ] ✅ View/download counting
- [ ] ✅ Document rating system
- [ ] ✅ Statistics and analytics

### **Performance Tests:**
- [ ] ✅ Response time < 200ms for simple queries
- [ ] ✅ Response time < 1s for complex hierarchy searches
- [ ] ✅ Support 100+ concurrent users
- [ ] ✅ Database queries optimized with proper indexes

### **Security Tests:**
- [ ] ✅ JWT token validation
- [ ] ✅ Role-based access control
- [ ] ✅ Input validation and sanitization
- [ ] ✅ SQL injection prevention
- [ ] ✅ File upload security

### **Integration Tests:**
- [ ] ✅ Database transactions
- [ ] ✅ Error handling and rollback
- [ ] ✅ Audit logging
- [ ] ✅ Domain events
- [ ] ✅ Clean Architecture layers integration

---

## 🚀 **KẾT QUẢ MONG ĐỢI**

Sau khi chạy tất cả test cases, hệ thống sẽ có:

1. **Hierarchy hoàn chỉnh:** Auto > 2008 > Toyota > Mazda > Mazda2 > Engine Schematic
2. **Search functionality:** Tìm kiếm theo bất kỳ cấp độ nào trong hierarchy
3. **User interactions:** View, download, rating đều hoạt động
4. **Statistics:** Tracking đầy đủ usage patterns
5. **Performance:** Response time nhanh với database optimized
6. **Security:** Authentication/authorization hoạt động đúng

Hệ thống đã sẵn sàng để triển khai production! 🎉
