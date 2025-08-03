package com.alibou.security.book.controller;

import com.alibou.security.book.BookRequest;
import com.alibou.security.book.BookResponse;
import com.alibou.security.book.application.service.*;
import com.alibou.security.book.dto.*;
import com.alibou.security.core.domain.service.FileStorageService;
import com.alibou.security.core.domain.service.PermissionService;
import com.alibou.security.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for book file upload and management
 * Handles file uploads with proper validation and security
 */
@RestController
@RequestMapping("/api/v1/books/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book File Management", description = "APIs for book file upload, download, and management")
public class BookFileController {

    private final BookUploadService bookUploadService;
    private final FileStorageService fileStorageService;
    private final PermissionService permissionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new book with file", 
               description = "Upload a book file along with metadata to create a new book entry")
    public ResponseEntity<BookResponse> uploadBook(
            @Parameter(description = "Book file to upload") 
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Book metadata") 
            @Valid @ModelAttribute BookRequest bookRequest,
            
            Authentication authentication) {
        
        log.info("Uploading book file: {} by user: {}", 
                file.getOriginalFilename(), authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        // Upload book
        BookResponse response = bookUploadService.uploadBook(bookRequest, file, user.getId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple books with files")
    public ResponseEntity<List<BookResponse>> uploadBooksInBatch(
            @Parameter(description = "Book files to upload") 
            @RequestParam("files") List<MultipartFile> files,
            
            @Parameter(description = "Book metadata list (JSON)") 
            @RequestParam("bookRequests") String bookRequestsJson,
            
            Authentication authentication) {
        
        log.info("Batch uploading {} book files by user: {}", 
                files.size(), authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        // Parse book requests from JSON (would need ObjectMapper)
        // List<BookRequest> bookRequests = parseBookRequests(bookRequestsJson);
        
        // For now, return empty list
        return ResponseEntity.ok(List.of());
    }

    @PutMapping(value = "/{bookId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update book file", 
               description = "Replace the existing book file with a new one")
    public ResponseEntity<BookResponse> updateBookFile(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            @Parameter(description = "New book file") 
            @RequestParam("file") MultipartFile file,
            
            Authentication authentication) {
        
        log.info("Updating book file for book ID: {} by user: {}", 
                bookId, authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookResponse response = bookUploadService.updateBookFile(bookId, file, user.getId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bookId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload book cover image")
    public ResponseEntity<BookResponse> uploadBookCover(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            @Parameter(description = "Cover image file") 
            @RequestParam("cover") MultipartFile coverImage,
            
            Authentication authentication) {
        
        log.info("Uploading cover for book ID: {} by user: {}", 
                bookId, authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookResponse response = bookUploadService.uploadBookCover(bookId, coverImage, user.getId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bookId}/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload book preview file")
    public ResponseEntity<BookResponse> uploadBookPreview(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            @Parameter(description = "Preview file") 
            @RequestParam("preview") MultipartFile previewFile,
            
            Authentication authentication) {
        
        log.info("Uploading preview for book ID: {} by user: {}", 
                bookId, authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookResponse response = bookUploadService.uploadBookPreview(bookId, previewFile, user.getId());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookId}/download")
    @Operation(summary = "Download book file", 
               description = "Download the book file if user has permission")
    public ResponseEntity<InputStreamResource> downloadBook(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            Authentication authentication) {
        
        log.info("Download request for book ID: {} by user: {}", 
                bookId, authentication.getName());
        
        User user = (User) authentication.getPrincipal();
        
        // Check download permission
        if (!permissionService.canDownloadBook(user, bookId)) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            // Get download URL (this would trigger download count increment)
            String downloadUrl = bookUploadService.getDownloadUrl(bookId, user.getId());
            
            // Get file stream
            InputStream fileStream = fileStorageService.getFileAsStream(downloadUrl);
            
            // Get file metadata for headers
            var metadata = fileStorageService.getFileMetadata(downloadUrl);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(fileStream));
                
        } catch (Exception e) {
            log.error("Failed to download book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{bookId}/download-url")
    @Operation(summary = "Get secure download URL")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            @Parameter(description = "URL expiration in minutes (default: 60)") 
            @RequestParam(defaultValue = "60") int expirationMinutes,
            
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Check download permission
        if (!permissionService.canDownloadBook(user, bookId)) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            String downloadUrl = bookUploadService.getDownloadUrl(bookId, user.getId());
            String secureUrl = fileStorageService.generateDownloadUrl(
                downloadUrl, user.getId(), expirationMinutes);
            
            return ResponseEntity.ok(Map.of(
                "downloadUrl", secureUrl,
                "expiresIn", String.valueOf(expirationMinutes),
                "bookId", bookId.toString()
            ));
            
        } catch (Exception e) {
            log.error("Failed to generate download URL for book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{bookId}/validate-file")
    @Operation(summary = "Validate book file before upload")
    public ResponseEntity<BookFileValidationResult> validateBookFile(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            @Parameter(description = "File to validate") 
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Book type category") 
            @RequestParam("category") String category,
            
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookFileValidationResult result = bookUploadService.validateBookFile(file, category);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{bookId}/process")
    @Operation(summary = "Process uploaded book file", 
               description = "Extract metadata and generate thumbnails for uploaded book")
    public ResponseEntity<BookProcessingResult> processBookFile(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookProcessingResult result = bookUploadService.processBookFile(bookId, user.getId());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{bookId}/statistics")
    @Operation(summary = "Get book file statistics")
    public ResponseEntity<BookFileStatistics> getBookFileStatistics(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Check if user can view statistics (editors and admins)
        if (!permissionService.hasEditorAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        BookFileStatistics statistics = bookUploadService.getBookFileStatistics(bookId);
        
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/{bookId}/backup")
    @Operation(summary = "Create backup of book file")
    public ResponseEntity<Map<String, String>> createBookFileBackup(
            @Parameter(description = "Book ID") 
            @PathVariable Integer bookId,
            
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission (admin only)
        if (!permissionService.hasAdminAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            String backupPath = bookUploadService.createBookFileBackup(bookId, user.getId());
            
            return ResponseEntity.ok(Map.of(
                "backupPath", backupPath,
                "bookId", bookId.toString(),
                "message", "Backup created successfully"
            ));
            
        } catch (Exception e) {
            log.error("Failed to create backup for book {}: {}", bookId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to create backup: " + e.getMessage()));
        }
    }

    @PostMapping("/cleanup-orphaned")
    @Operation(summary = "Clean up orphaned files", 
               description = "Remove files that no longer have corresponding book records")
    public ResponseEntity<OrphanCleanupResult> cleanupOrphanedFiles(
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Validate permission (admin only)
        if (!permissionService.hasAdminAccess(user)) {
            return ResponseEntity.status(403).build();
        }
        
        OrphanCleanupResult result = bookUploadService.cleanupOrphanedFiles();
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upload-progress/{uploadId}")
    @Operation(summary = "Get upload progress")
    public ResponseEntity<UploadProgress> getUploadProgress(
            @Parameter(description = "Upload ID") 
            @PathVariable String uploadId,
            
            Authentication authentication) {
        
        UploadProgress progress = bookUploadService.getUploadProgress(uploadId);
        
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(progress);
    }

    @DeleteMapping("/upload/{uploadId}")
    @Operation(summary = "Cancel ongoing upload")
    public ResponseEntity<Map<String, String>> cancelUpload(
            @Parameter(description = "Upload ID") 
            @PathVariable String uploadId,
            
            Authentication authentication) {
        
        try {
            bookUploadService.cancelUpload(uploadId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Upload cancelled successfully",
                "uploadId", uploadId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to cancel upload: " + e.getMessage()));
        }
    }
}
