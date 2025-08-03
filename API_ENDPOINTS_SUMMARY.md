# 📚 HỆ THỐNG API QUẢN LÝ THƯ VIỆN TÀI LIỆU KỸ THUẬT

## 🏗️ **KIẾN TRÚC PHÂN CẤP 6 TẦNG**

```
documents/
├── DocumentField (Lĩnh vực: Auto, Electrical Bike)
│   └── ProductionYear (Năm sản xuất: 2008, 2010, 2015)
│       └── Manufacturer (Nhà sản xuất: Toyota, Honda, BMW)
│           └── ProductSeries (Dòng sản phẩm: Camry, Civic, X5)
│               └── Product (Sản phẩm: Camry 2.0, Civic Si, X5 xDrive)
│                   └── TechnicalDocument (Tài liệu: Engine Schematic, Wiring Diagram)
```

## � **FILE UPLOAD SYSTEM**

### **Upload Workflow Options:**

1. **2-Step Process**: Upload file → Set metadata → Create document
2. **Single-Step Process**: Upload file + metadata in one request
3. **File Management**: Update metadata, replace files, temporary storage

## �🔗 **CÁC API ENDPOINTS**

### **🗂️ FILE UPLOAD APIs (Tải lên tài liệu)**

#### **POST /api/v1/documents/upload/temporary**
- **Mục đích**: Tải file lên vị trí tạm thời (Step 1 của quy trình 2 bước)
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (MultipartFile)
- **Response**: Temporary file ID, checksum, file info
- **Use case**: Tải file lên trước, sau đó thiết lập metadata

#### **POST /api/v1/documents/upload/create-document**
- **Mục đích**: Tạo tài liệu với file đã tải (Step 2 của quy trình 2 bước)
- **Body**: CreateDocumentRequest (temporaryFileId, productId, title, documentType, etc.)
- **Response**: TechnicalDocumentDto đã tạo
- **Use case**: Sau khi upload file tạm, dùng API này để thiết lập thông tin và tạo document chính thức

#### **POST /api/v1/documents/upload/single-step**
- **Mục đích**: Tải file và tạo tài liệu trong một bước
- **Content-Type**: `multipart/form-data`
- **Body**: file + metadata (productId, title, documentType, description, etc.)
- **Response**: TechnicalDocumentDto đã tạo
- **Use case**: Quy trình nhanh khi có đầy đủ thông tin

#### **GET /api/v1/documents/upload/temporary/{tempFileId}**
- **Mục đích**: Lấy thông tin file tạm thời
- **Response**: File info (name, size, format, checksum)

#### **DELETE /api/v1/documents/upload/temporary/{tempFileId}**
- **Mục đích**: Xóa file tạm thời không cần thiết
- **Response**: Success confirmation

#### **PUT /api/v1/documents/upload/{documentId}/metadata**
- **Mục đích**: Cập nhật metadata tài liệu (không thay đổi file)
- **Body**: UpdateDocumentMetadataRequest
- **Response**: TechnicalDocumentDto đã cập nhật
- **Use case**: Sửa thông tin tài liệu sau khi đã upload

#### **PUT /api/v1/documents/upload/{documentId}/file**
- **Mục đích**: Thay thế file tài liệu (giữ nguyên metadata)
- **Content-Type**: `multipart/form-data`
- **Body**: newFile (MultipartFile)
- **Response**: TechnicalDocumentDto với file mới
- **Use case**: Cập nhật version mới của file

#### **GET /api/v1/documents/upload/statistics**
- **Mục đích**: Thống kê upload (tổng documents, file size, temp files)
- **Response**: Upload statistics

### **📋 DOCUMENT TYPE ENUM**
```
MANUAL, SCHEMATIC, SPECIFICATION, REPAIR_GUIDE, PARTS_CATALOG, 
SERVICE_BULLETIN, WIRING_DIAGRAM, TECHNICAL_NOTE, SAFETY_SHEET, 
INSTALLATION_GUIDE, TROUBLESHOOTING, FIRMWARE, CALIBRATION, 
TEST_PROCEDURE, TRAINING_MATERIAL, OTHER
```

### **🔧 FILE UPLOAD CONFIGURATION**
- **Max file size**: 10MB
- **Allowed extensions**: pdf, doc, docx, xls, xlsx, ppt, pptx, dwg, png, jpg, jpeg
- **Upload directory**: ./uploads
- **Temp directory**: ./uploads/temp

---

### **1. DocumentField APIs (Lĩnh Vực Tài Liệu)**

#### **GET /api/v1/document-fields**
- **Mục đích**: Lấy danh sách tất cả lĩnh vực hoạt động
- **Response**: Danh sách các lĩnh vực (Auto, Electrical Bike, ...)
- **Sắp xếp**: Theo sortOrder

#### **GET /api/v1/document-fields/search?query={keyword}**
- **Mục đích**: Tìm kiếm lĩnh vực theo tên hoặc mô tả
- **Parameters**: 
  - `query`: từ khóa tìm kiếm
  - `page`, `size`: phân trang

#### **GET /api/v1/document-fields/{id}/statistics**
- **Mục đích**: Thống kê tài liệu theo lĩnh vực
- **Response**: Số lượng năm, nhà sản xuất, tài liệu

#### **POST /api/v1/document-fields**
- **Mục đích**: Tạo lĩnh vực mới
- **Body**: DocumentFieldDto (name, code, description, colorCode)

#### **PUT /api/v1/document-fields/{id}**
- **Mục đích**: Cập nhật thông tin lĩnh vực
- **Body**: DocumentFieldDto

#### **DELETE /api/v1/document-fields/{id}**
- **Mục đích**: Xóa lĩnh vực (soft delete)

---

### **2. ProductionYear APIs (Năm Sản Xuất)**

#### **GET /api/v1/production-years?fieldId={fieldId}**
- **Mục đích**: Lấy danh sách năm sản xuất theo lĩnh vực
- **Parameters**: `fieldId` (optional)
- **Sắp xếp**: Năm giảm dần

#### **GET /api/v1/production-years/range?fieldId={fieldId}&startYear={start}&endYear={end}**
- **Mục đích**: Lấy năm trong khoảng thời gian
- **Parameters**: fieldId, startYear, endYear

#### **GET /api/v1/production-years/{id}/manufacturers**
- **Mục đích**: Lấy danh sách nhà sản xuất theo năm

#### **POST /api/v1/production-years**
- **Body**: ProductionYearDto (year, fieldId, description)

---

### **3. Manufacturer APIs (Nhà Sản Xuất)**

#### **GET /api/v1/manufacturers?yearId={yearId}**
- **Mục đích**: Lấy danh sách nhà sản xuất theo năm
- **Parameters**: `yearId` (optional)

#### **GET /api/v1/manufacturers/search?query={name}&yearId={yearId}**
- **Mục đích**: Tìm kiếm nhà sản xuất theo tên

#### **GET /api/v1/manufacturers/{id}/product-series**
- **Mục đích**: Lấy các dòng sản phẩm của nhà sản xuất

#### **GET /api/v1/manufacturers/{id}/statistics**
- **Mục đích**: Thống kê dòng sản phẩm, sản phẩm, tài liệu

#### **POST /api/v1/manufacturers**
- **Body**: ManufacturerDto (name, yearId, description, website)

---

### **4. ProductSeries APIs (Dòng Sản Phẩm)**

#### **GET /api/v1/product-series?manufacturerId={manufacturerId}**
- **Mục đích**: Lấy danh sách dòng sản phẩm theo nhà sản xuất

#### **GET /api/v1/product-series/search?query={name}&manufacturerId={manufacturerId}**
- **Mục đích**: Tìm kiếm dòng sản phẩm

#### **GET /api/v1/product-series/{id}/products**
- **Mục đích**: Lấy các sản phẩm trong dòng

#### **POST /api/v1/product-series**
- **Body**: ProductSeriesDto (name, manufacturerId, description)

---

### **5. Product APIs (Sản Phẩm)**

#### **GET /api/v1/products?seriesId={seriesId}**
- **Mục đích**: Lấy danh sách sản phẩm theo dòng

#### **GET /api/v1/products/search?query={name}&seriesId={seriesId}**
- **Mục đích**: Tìm kiếm sản phẩm

#### **GET /api/v1/products/{id}/documents**
- **Mục đích**: Lấy tài liệu kỹ thuật của sản phẩm

#### **GET /api/v1/products/{id}/document-types**
- **Mục đích**: Lấy các loại tài liệu có sẵn

#### **POST /api/v1/products**
- **Body**: ProductDto (name, seriesId, description, specifications)

---

### **6. TechnicalDocument APIs (Tài Liệu Kỹ Thuật)**

#### **GET /api/v1/technical-documents?productId={productId}**
- **Mục đích**: Lấy tài liệu theo sản phẩm

#### **GET /api/v1/technical-documents/search**
- **Mục đích**: Tìm kiếm tài liệu phức tạp theo hierarchy
- **Parameters**: 
  - `fieldName`: lĩnh vực
  - `year`: năm sản xuất  
  - `manufacturerName`: nhà sản xuất
  - `seriesName`: dòng sản phẩm
  - `productName`: sản phẩm
  - `documentType`: loại tài liệu
  - `query`: từ khóa

#### **GET /api/v1/technical-documents/popular?limit={limit}**
- **Mục đích**: Lấy tài liệu phổ biến nhất

#### **GET /api/v1/technical-documents/highly-rated?limit={limit}**
- **Mục đích**: Lấy tài liệu đánh giá cao

#### **GET /api/v1/technical-documents/recent?limit={limit}**
- **Mục đích**: Lấy tài liệu mới nhất

#### **GET /api/v1/technical-documents/{id}/download**
- **Mục đích**: Tải xuống tài liệu (tăng download count)

#### **POST /api/v1/technical-documents/{id}/view**
- **Mục đích**: Đánh dấu đã xem (tăng view count)

#### **POST /api/v1/technical-documents/{id}/rate**
- **Mục đích**: Đánh giá tài liệu (1-5 sao)
- **Body**: { "rating": 4.5 }

#### **POST /api/v1/technical-documents**
- **Mục đích**: Upload tài liệu mới
- **Body**: TechnicalDocumentDto + file upload

#### **GET /api/v1/technical-documents/statistics**
- **Mục đích**: Thống kê tổng quan
- **Response**: 
  - totalDocuments: tổng số tài liệu
  - totalDownloads: tổng lượt tải
  - totalViews: tổng lượt xem
  - averageRating: đánh giá trung bình

---

### **7. Hierarchy Navigation APIs (Điều Hướng Phân Cấp)**

#### **GET /api/v1/hierarchy/fields-by-year/{year}**
- **Mục đích**: Lĩnh vực có sản phẩm năm cụ thể

#### **GET /api/v1/hierarchy/manufacturers-by-field/{fieldName}**
- **Mục đích**: Nhà sản xuất trong lĩnh vực

#### **GET /api/v1/hierarchy/breadcrumb/{documentId}**
- **Mục đích**: Lấy đường dẫn phân cấp đầy đủ
- **Response**: "Auto > 2008 > Toyota > Camry > Camry 2.0 > Engine Schematic"

#### **GET /api/v1/hierarchy/tree?fieldId={fieldId}&year={year}**
- **Mục đích**: Cây phân cấp từ lĩnh vực xuống tài liệu

---

## 🎯 **CÁC TÍNH NĂNG ĐẶC BIỆT**

### **1. Tìm Kiếm Thông Minh**
```http
GET /api/v1/search/documents?q=engine+toyota+2008
```
- Tìm kiếm toàn văn qua tất cả cấp độ
- Hỗ trợ nhiều từ khóa
- Sắp xếp theo độ phổ biến, đánh giá

### **2. Thống Kê và Báo Cáo**
```http
GET /api/v1/analytics/dashboard
```
- Top documents by downloads
- Popular manufacturers
- Document trends by year
- Field distribution

### **3. Upload và Quản Lý File**
```http
POST /api/v1/files/upload
Content-Type: multipart/form-data

- file: document file
- metadata: document information
- hierarchy: position in tree
```

### **4. Bulk Operations**
```http
POST /api/v1/bulk/documents/import
```
- Import hàng loạt tài liệu
- Validate hierarchy structure
- Error reporting

---

## 🔒 **AUTHENTICATION & AUTHORIZATION**

### **Headers Required:**
```http
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### **Role-Based Access:**
- **ADMIN**: Full CRUD operations
- **MANAGER**: Read + Limited write
- **USER**: Read-only + download
- **GUEST**: Public documents only

---

## 📦 **RESPONSE FORMATS**

### **Success Response:**
```json
{
  "status": "success",
  "data": {...},
  "timestamp": "2025-08-03T10:30:00Z"
}
```

### **Error Response:**
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [...],
  "timestamp": "2025-08-03T10:30:00Z"
}
```

### **Paginated Response:**
```json
{
  "content": [...],
  "pageable": {
    "page": 0,
    "size": 20,
    "total": 150
  }
}
```

---

## 🔐 **HIERARCHICAL PERMISSION SYSTEM APIs**

### **🔧 ADMIN PERMISSION MANAGEMENT**

#### **POST /api/v1/admin/hierarchy-permissions**
- **Mục đích**: Admin tạo quyền phân cấp cho editor
- **Role**: ADMIN only
- **Body**: CreateHierarchyPermissionRequest
```json
{
  "editorId": 2,
  "documentFieldId": 1,  // null = all fields
  "productionYearId": null,  // null = all years
  "manufacturerId": null,
  "productSeriesId": null,
  "productId": null,
  "canUpload": true,
  "canEdit": true,
  "canDelete": false,
  "canView": true,
  "scopeDescription": "Full access to Engine documents"
}
```

#### **PUT /api/v1/admin/hierarchy-permissions/{permissionId}**
- **Mục đích**: Admin cập nhật quyền hiện tại
- **Role**: ADMIN only

#### **DELETE /api/v1/admin/hierarchy-permissions/{permissionId}**
- **Mục đích**: Admin xóa quyền
- **Role**: ADMIN only

#### **GET /api/v1/admin/hierarchy-permissions**
- **Mục đích**: Admin xem tất cả quyền trong hệ thống
- **Role**: ADMIN only

#### **GET /api/v1/admin/hierarchy-permissions/editor/{editorId}**
- **Mục đích**: Admin xem quyền của editor cụ thể
- **Role**: ADMIN only

#### **GET /api/v1/admin/hierarchy-permissions/statistics**
- **Mục đích**: Admin xem thống kê quyền hệ thống
- **Role**: ADMIN only

### **👤 EDITOR PERMISSION APIS**

#### **GET /api/v1/editor/my-permissions**
- **Mục đích**: Editor xem quyền của mình
- **Role**: EDITOR only
- **Response**: Permission tree structure

#### **GET /api/v1/editor/my-permissions/uploadable-products**
- **Mục đích**: Editor xem sản phẩm có thể upload
- **Role**: EDITOR only

#### **GET /api/v1/editor/my-permissions/check/upload/{productId}**
- **Mục đích**: Editor kiểm tra quyền upload cho sản phẩm
- **Role**: EDITOR only

#### **GET /api/v1/editor/my-permissions/check/edit/{productId}**
- **Mục đích**: Editor kiểm tra quyền edit
- **Role**: EDITOR only

#### **GET /api/v1/editor/my-permissions/check/delete/{productId}**
- **Mục đích**: Editor kiểm tra quyền delete
- **Role**: EDITOR only

### **🔐 PERMISSION SYSTEM FEATURES**

#### **Permission Levels:**
- **Field Level**: Quyền áp dụng cho toàn bộ lĩnh vực (auto/electrical bike)
- **Year Level**: Quyền cho năm sản xuất cụ thể (tất cả xe 2020)
- **Manufacturer Level**: Quyền cho nhà sản xuất (tất cả docs Toyota)
- **Series Level**: Quyền cho dòng sản phẩm (tất cả Camry models)
- **Product Level**: Quyền cho sản phẩm cụ thể (chỉ Camry 2.0)

#### **Permission Types:**
- **UPLOAD**: Có thể upload tài liệu mới
- **EDIT**: Có thể chỉnh sửa tài liệu hiện tại
- **DELETE**: Có thể xóa tài liệu
- **VIEW**: Có thể xem tài liệu (luôn bắt buộc)

#### **Inheritance Rules:**
- **NULL = All**: null tại cấp nào = quyền cho tất cả items tại cấp đó
- **Specific ID**: ID cụ thể = chỉ áp dụng cho item đó và con của nó
- **Override**: Quyền cụ thể sẽ override quyền tổng quát

#### **Permission Integration:**
- Upload APIs validate permissions before creating documents
- Editor APIs show only permitted resources
- Admin APIs provide full permission management
- Conflict detection prevents overlapping permissions
