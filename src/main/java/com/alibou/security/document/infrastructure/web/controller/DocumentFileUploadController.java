package com.alibou.security.document.infrastructure.web.controller;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.application.service.DocumentFileUploadService;
import com.alibou.security.document.application.service.HierarchyPermissionService;
import com.alibou.security.document.infrastructure.web.dto.CreateDocumentRequest;
import com.alibou.security.document.infrastructure.web.dto.FileUploadResponse;
import com.alibou.security.document.infrastructure.web.dto.UpdateDocumentMetadataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

/**
 * REST Controller for document file upload operations
 * Handles file uploads with 2-step and single-step processes
 */
@RestController
@RequestMapping("/api/v1/documents/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document File Upload", description = "APIs for uploading document files")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
public class DocumentFileUploadController {
    
    private final DocumentFileUploadService uploadService;
    private final HierarchyPermissionService permissionService;
    
    /**
     * Step 1: Upload file to temporary location
     * User uploads file first, gets temporary file ID
     */
    @PostMapping(value = "/temporary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload file to temporary location",
        description = "Upload a file to temporary storage and get a temporary file ID for later use",
        responses = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
        }
    )
    public ResponseEntity<FileUploadResponse> uploadTemporaryFile(
        @Parameter(description = "File to upload", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        try {
            log.info("Uploading temporary file: {}", file.getOriginalFilename());
            FileUploadResponse response = uploadService.uploadTemporaryFile(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.error(e.getMessage(), "INVALID_FILE"));
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(FileUploadResponse.error("Upload failed: " + e.getMessage(), "UPLOAD_ERROR"));
        }
    }
    
    /**
     * Step 2: Create document with metadata using temporary file
     * User provides metadata and temporary file ID to create final document
     */
    @PostMapping("/create-document")
    @Operation(
        summary = "Create document with uploaded file",
        description = "Create a document using a previously uploaded temporary file and metadata",
        responses = {
            @ApiResponse(responseCode = "200", description = "Document created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or temporary file not found"),
            @ApiResponse(responseCode = "403", description = "No permission to upload to this product"),
            @ApiResponse(responseCode = "500", description = "Document creation failed")
        }
    )
    public ResponseEntity<?> createDocumentWithFile(
        @Valid @RequestBody CreateDocumentRequest request,
        Authentication authentication
    ) {
        try {
            log.info("Creating document with temporary file: {}", request.getTemporaryFileId());
            
            // Check upload permission
            String username = authentication.getName();
            boolean canUpload = permissionService.canUploadToProduct(username, request.getProductId());
            
            if (!canUpload) {
                log.warn("User {} lacks upload permission for product: {}", username, request.getProductId());
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "success", false, 
                        "message", "No permission to upload to this product", 
                        "errorCode", "PERMISSION_DENIED"
                    ));
            }
            
            TechnicalDocumentDto document = uploadService.createDocumentWithFile(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document created successfully",
                "document", document
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage(), "errorCode", "INVALID_REQUEST"));
        } catch (IOException e) {
            log.error("Error creating document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Document creation failed: " + e.getMessage(), "errorCode", "CREATION_ERROR"));
        }
    }
    
    /**
     * Alternative: Upload file and create document in one step
     * For simpler workflows where user provides file and metadata together
     */
    @PostMapping(value = "/single-step", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload file and create document in one step",
        description = "Upload file and create document with metadata in a single operation",
        responses = {
            @ApiResponse(responseCode = "200", description = "File uploaded and document created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or metadata"),
            @ApiResponse(responseCode = "500", description = "Upload or creation failed")
        }
    )
    public ResponseEntity<?> uploadAndCreateDocument(
        @Parameter(description = "File to upload", required = true)
        @RequestParam("file") MultipartFile file,
        
        @Parameter(description = "Product ID", required = true)
        @RequestParam("productId") Integer productId,
        
        @Parameter(description = "Document title", required = true)
        @RequestParam("title") String title,
        
        @Parameter(description = "Document type", required = true)
        @RequestParam("documentType") String documentType,
        
        @Parameter(description = "Description")
        @RequestParam(value = "description", required = false) String description,
        
        @Parameter(description = "Category")
        @RequestParam(value = "category", required = false) String category,
        
        @Parameter(description = "Sub category")
        @RequestParam(value = "subCategory", required = false) String subCategory,
        
        @Parameter(description = "Version")
        @RequestParam(value = "version", required = false) String version,
        
        @Parameter(description = "Language")
        @RequestParam(value = "language", required = false) String language,
        
        @Parameter(description = "Is public")
        @RequestParam(value = "isPublic", required = false) Boolean isPublic,
        
        @Parameter(description = "Is downloadable")
        @RequestParam(value = "downloadable", required = false) Boolean downloadable,
        
        @Parameter(description = "Sort order")
        @RequestParam(value = "sortOrder", required = false) Integer sortOrder
    ) {
        try {
            log.info("Single-step upload for document: {}", title);
            
            // Build request object
            CreateDocumentRequest request = CreateDocumentRequest.builder()
                .productId(productId)
                .title(title)
                .documentType(com.alibou.security.document.domain.enums.DocumentType.fromString(documentType))
                .description(description)
                .category(category)
                .subCategory(subCategory)
                .version(version)
                .language(language)
                .isPublic(isPublic)
                .downloadable(downloadable)
                .sortOrder(sortOrder)
                .build();
            
            TechnicalDocumentDto document = uploadService.uploadAndCreateDocument(file, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "File uploaded and document created successfully",
                "document", document
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid single-step upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage(), "errorCode", "INVALID_REQUEST"));
        } catch (IOException e) {
            log.error("Error in single-step upload: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Upload failed: " + e.getMessage(), "errorCode", "UPLOAD_ERROR"));
        }
    }
    
    /**
     * Get temporary file information
     */
    @GetMapping("/temporary/{tempFileId}")
    @Operation(
        summary = "Get temporary file information",
        description = "Retrieve information about a previously uploaded temporary file"
    )
    public ResponseEntity<?> getTemporaryFileInfo(
        @PathVariable String tempFileId
    ) {
        try {
            FileUploadResponse response = uploadService.getTemporaryFileInfo(tempFileId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage(), "errorCode", "FILE_NOT_FOUND"));
        }
    }
    
    /**
     * Delete temporary file
     */
    @DeleteMapping("/temporary/{tempFileId}")
    @Operation(
        summary = "Delete temporary file",
        description = "Delete a temporary file that is no longer needed"
    )
    public ResponseEntity<?> deleteTemporaryFile(
        @PathVariable String tempFileId
    ) {
        try {
            uploadService.deleteTemporaryFile(tempFileId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Temporary file deleted successfully"
            ));
        } catch (IOException e) {
            log.error("Error deleting temporary file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Delete failed: " + e.getMessage(), "errorCode", "DELETE_ERROR"));
        }
    }
    
    /**
     * Update document metadata without changing file
     */
    @PutMapping("/{documentId}/metadata")
    @Operation(
        summary = "Update document metadata",
        description = "Update document metadata without changing the file"
    )
    public ResponseEntity<?> updateDocumentMetadata(
        @PathVariable Integer documentId,
        @Valid @RequestBody UpdateDocumentMetadataRequest request
    ) {
        try {
            TechnicalDocumentDto document = uploadService.updateDocumentMetadata(documentId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document metadata updated successfully",
                "document", document
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage(), "errorCode", "INVALID_REQUEST"));
        }
    }
    
    /**
     * Replace document file while keeping metadata
     */
    @PutMapping(value = "/{documentId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Replace document file",
        description = "Replace the file of an existing document while keeping its metadata"
    )
    public ResponseEntity<?> replaceDocumentFile(
        @PathVariable Integer documentId,
        @RequestParam("file") MultipartFile newFile
    ) {
        try {
            log.info("Replacing file for document: {}", documentId);
            TechnicalDocumentDto document = uploadService.replaceDocumentFile(documentId, newFile);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document file replaced successfully",
                "document", document
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage(), "errorCode", "INVALID_REQUEST"));
        } catch (IOException e) {
            log.error("Error replacing document file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "File replacement failed: " + e.getMessage(), "errorCode", "REPLACEMENT_ERROR"));
        }
    }
    
    /**
     * Get upload statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get upload statistics",
        description = "Get statistics about uploaded files and documents"
    )
    public ResponseEntity<Map<String, Object>> getUploadStatistics() {
        Map<String, Object> stats = uploadService.getUploadStatistics();
        return ResponseEntity.ok(stats);
    }
}
