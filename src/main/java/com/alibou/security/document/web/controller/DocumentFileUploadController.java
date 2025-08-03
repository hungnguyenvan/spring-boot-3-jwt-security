package com.alibou.security.document.web.controller;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.application.service.TechnicalDocumentApplicationService;
import com.alibou.security.document.application.service.DocumentFileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST Controller for Document File Upload and Management
 */
@RestController
@RequestMapping("/api/v1/document-files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document File Upload", description = "APIs for uploading and managing document files")
public class DocumentFileUploadController {
    
    private final DocumentFileUploadService fileUploadService;
    private final TechnicalDocumentApplicationService documentService;
    
    /**
     * Step 1: Upload file only (without metadata)
     * Returns temporary file information
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document file", description = "Upload a document file and get temporary file information")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "Document file to upload") 
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading file: {}", file.getOriginalFilename());
        
        try {
            FileUploadResponse response = fileUploadService.uploadTemporaryFile(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FileUploadResponse.error("Upload failed: " + e.getMessage()));
        }
    }
    
    /**
     * Step 2: Create document with metadata and link to uploaded file
     * Moves file from temporary location to final location
     */
    @PostMapping("/create-document")
    @Operation(summary = "Create document with metadata", description = "Create technical document with metadata and link to uploaded file")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TechnicalDocumentDto> createDocumentWithFile(
            @Valid @RequestBody CreateDocumentRequest request) {
        
        log.info("Creating document: {} with file: {}", request.getTitle(), request.getTemporaryFileId());
        
        try {
            TechnicalDocumentDto document = fileUploadService.createDocumentWithFile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (Exception e) {
            log.error("Error creating document: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Step 3: Upload file and create document in one step (alternative approach)
     */
    @PostMapping(value = "/upload-and-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file and create document", description = "Upload file and create document with metadata in one step")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TechnicalDocumentDto> uploadAndCreateDocument(
            @Parameter(description = "Document file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document title") @RequestParam("title") String title,
            @Parameter(description = "Document type") @RequestParam("documentType") String documentType,
            @Parameter(description = "Product ID") @RequestParam("productId") Integer productId,
            @Parameter(description = "Description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Sub-category") @RequestParam(value = "subCategory", required = false) String subCategory,
            @Parameter(description = "Version") @RequestParam(value = "version", required = false) String version,
            @Parameter(description = "Language") @RequestParam(value = "language", defaultValue = "EN") String language,
            @Parameter(description = "Is public") @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic) {
        
        log.info("Uploading and creating document: {} for product: {}", title, productId);
        
        try {
            CreateDocumentRequest request = CreateDocumentRequest.builder()
                .title(title)
                .documentType(documentType)
                .description(description)
                .category(category)
                .subCategory(subCategory)
                .productId(productId)
                .version(version)
                .language(language)
                .isPublic(isPublic)
                .build();
            
            TechnicalDocumentDto document = fileUploadService.uploadAndCreateDocument(file, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (Exception e) {
            log.error("Error uploading and creating document: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get file information by temporary ID
     */
    @GetMapping("/temp/{tempFileId}")
    @Operation(summary = "Get temporary file info", description = "Get information about uploaded temporary file")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<FileUploadResponse> getTemporaryFileInfo(
            @Parameter(description = "Temporary file ID") @PathVariable String tempFileId) {
        
        log.info("Getting temporary file info: {}", tempFileId);
        
        try {
            FileUploadResponse response = fileUploadService.getTemporaryFileInfo(tempFileId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting temporary file info: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete temporary file
     */
    @DeleteMapping("/temp/{tempFileId}")
    @Operation(summary = "Delete temporary file", description = "Delete uploaded temporary file")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteTemporaryFile(
            @Parameter(description = "Temporary file ID") @PathVariable String tempFileId) {
        
        log.info("Deleting temporary file: {}", tempFileId);
        
        try {
            fileUploadService.deleteTemporaryFile(tempFileId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting temporary file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update document metadata (without changing file)
     */
    @PutMapping("/{documentId}/metadata")
    @Operation(summary = "Update document metadata", description = "Update document metadata without changing the file")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TechnicalDocumentDto> updateDocumentMetadata(
            @Parameter(description = "Document ID") @PathVariable Integer documentId,
            @Valid @RequestBody UpdateDocumentMetadataRequest request) {
        
        log.info("Updating metadata for document: {}", documentId);
        
        try {
            TechnicalDocumentDto updated = fileUploadService.updateDocumentMetadata(documentId, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating document metadata: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Replace document file (keep metadata)
     */
    @PutMapping(value = "/{documentId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Replace document file", description = "Replace document file while keeping metadata")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TechnicalDocumentDto> replaceDocumentFile(
            @Parameter(description = "Document ID") @PathVariable Integer documentId,
            @Parameter(description = "New document file") @RequestParam("file") MultipartFile file) {
        
        log.info("Replacing file for document: {}", documentId);
        
        try {
            TechnicalDocumentDto updated = fileUploadService.replaceDocumentFile(documentId, file);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error replacing document file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get upload statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get upload statistics", description = "Get file upload statistics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUploadStatistics() {
        
        log.info("Getting upload statistics");
        
        try {
            Map<String, Object> stats = fileUploadService.getUploadStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting upload statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

/**
 * Response class for file upload
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class FileUploadResponse {
    private boolean success;
    private String message;
    private String temporaryFileId;
    private String originalFileName;
    private String fileFormat;
    private Long fileSize;
    private String checksum;
    private String uploadedAt;
    private String error;
    
    public static FileUploadResponse success(String tempFileId, String fileName, String format, Long size, String checksum) {
        return FileUploadResponse.builder()
            .success(true)
            .message("File uploaded successfully")
            .temporaryFileId(tempFileId)
            .originalFileName(fileName)
            .fileFormat(format)
            .fileSize(size)
            .checksum(checksum)
            .uploadedAt(java.time.LocalDateTime.now().toString())
            .build();
    }
    
    public static FileUploadResponse error(String errorMessage) {
        return FileUploadResponse.builder()
            .success(false)
            .error(errorMessage)
            .build();
    }
}
