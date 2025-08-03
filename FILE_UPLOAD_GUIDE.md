# 📁 HƯỚNG DẪN UPLOAD FILE VÀ THIẾT LẬP THÔNG TIN

## 🎯 **TRẢ LỜI CÂU HỎI: "Upload file rồi thiết lập thông tin cho file thì dùng API nào?"**

### **PHƯƠNG PHÁP 1: QUY TRÌNH 2 BƯỚC (KHUYẾN NGHỊ)**

#### **Bước 1: Upload file tạm thời**
```http
POST /api/v1/documents/upload/temporary
Content-Type: multipart/form-data

file: [your-file.pdf]
```
**Response**: 
```json
{
  "success": true,
  "temporaryFileId": "uuid-temp-file-id",
  "originalFileName": "engine_schematic.pdf",
  "fileFormat": "PDF",
  "fileSize": 1024000,
  "checksum": "sha256:abc123..."
}
```

#### **Bước 2: Thiết lập thông tin và tạo document chính thức**
```http
POST /api/v1/documents/upload/create-document
Content-Type: application/json

{
  "temporaryFileId": "uuid-temp-file-id",  // ID từ bước 1
  "productId": 1,
  "title": "Engine Schematic V2024",
  "documentType": "SCHEMATIC",
  "description": "Detailed engine schematic",
  "category": "Engine",
  "subCategory": "Schematic",
  "version": "v2024.1",
  "language": "EN",
  "isPublic": true,
  "downloadable": true,
  "sortOrder": 1
}
```

### **PHƯƠNG PHÁP 2: QUY TRÌNH 1 BƯỚC (NHANH CHÓNG)**

```http
POST /api/v1/documents/upload/single-step
Content-Type: multipart/form-data

file: [your-file.pdf]
productId: 1
title: Engine Schematic V2024
documentType: SCHEMATIC
description: Detailed engine schematic
category: Engine
subCategory: Schematic
version: v2024.1
language: EN
isPublic: true
downloadable: true
sortOrder: 1
```

## 🛠️ **CÁC API BỔ SUNG**

### **Cập nhật thông tin document (không đổi file)**
```http
PUT /api/v1/documents/upload/{documentId}/metadata
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "version": "v2024.2"
}
```

### **Thay thế file (giữ nguyên metadata)**
```http
PUT /api/v1/documents/upload/{documentId}/file
Content-Type: multipart/form-data

file: [new-file.pdf]
```

### **Xem thông tin file tạm thời**
```http
GET /api/v1/documents/upload/temporary/{tempFileId}
```

### **Xóa file tạm thời**
```http
DELETE /api/v1/documents/upload/temporary/{tempFileId}
```

## 📊 **DOCUMENT TYPES HỖ TRỢ**

```
MANUAL - User manuals
SCHEMATIC - Technical schematics  
SPECIFICATION - Product specifications
REPAIR_GUIDE - Repair guides
PARTS_CATALOG - Parts catalogs
SERVICE_BULLETIN - Service bulletins
WIRING_DIAGRAM - Wiring diagrams
TECHNICAL_NOTE - Technical notes
SAFETY_SHEET - Safety sheets
INSTALLATION_GUIDE - Installation guides
TROUBLESHOOTING - Troubleshooting guides
FIRMWARE - Firmware documentation
CALIBRATION - Calibration procedures
TEST_PROCEDURE - Test procedures
TRAINING_MATERIAL - Training materials
OTHER - Other types
```

## ⚙️ **CẤU HÌNH FILE UPLOAD**

- **Max file size**: 10MB
- **Allowed extensions**: pdf, doc, docx, xls, xlsx, ppt, pptx, dwg, png, jpg, jpeg
- **Upload path**: ./uploads/{field}/{year}/{manufacturer}/{series}/{product}/
- **Temp storage**: ./uploads/temp/

## 🔗 **LIÊN KẾT VỚI HỆ THỐNG**

Sau khi upload và thiết lập thông tin, documents sẽ được tích hợp vào hệ thống phân cấp:
```
documents/Auto/2008/Toyota/Mazda/Mazda2/Engine_Schematic.pdf
```

Và có thể truy cập qua các API khác:
- Search: `GET /api/v1/technical-documents/search?query=engine`
- By Product: `GET /api/v1/technical-documents/product/1`
- By Hierarchy: `GET /api/v1/technical-documents/hierarchy`

## 🧪 **TESTING**

Sử dụng file test HTTP: `http/file-upload-test.http` để test tất cả các workflow upload.
