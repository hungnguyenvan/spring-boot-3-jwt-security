package com.alibou.security.book.application.service;

import com.alibou.security.book.BookRequest;
import com.alibou.security.book.BookResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Application service for book file upload and management
 * Orchestrates file storage with book entity management
 */
public interface BookUploadService {
    
    /**
     * Upload a new book with file
     */
    BookResponse uploadBook(BookRequest bookRequest, MultipartFile file, Integer userId);
    
    /**
     * Upload multiple books with files
     */
    List<BookResponse> uploadBooksInBatch(List<BookRequest> bookRequests, 
                                         List<MultipartFile> files, Integer userId);
    
    /**
     * Update book file (replace existing file)
     */
    BookResponse updateBookFile(Integer bookId, MultipartFile file, Integer userId);
    
    /**
     * Upload cover image for book
     */
    BookResponse uploadBookCover(Integer bookId, MultipartFile coverImage, Integer userId);
    
    /**
     * Upload preview file for book (sample pages)
     */
    BookResponse uploadBookPreview(Integer bookId, MultipartFile previewFile, Integer userId);
    
    /**
     * Validate book file before upload
     */
    BookFileValidationResult validateBookFile(MultipartFile file, String bookTypeCategory);
    
    /**
     * Get upload progress for large files
     */
    UploadProgress getUploadProgress(String uploadId);
    
    /**
     * Cancel ongoing upload
     */
    void cancelUpload(String uploadId);
    
    /**
     * Process uploaded book file (extract metadata, generate thumbnails, etc.)
     */
    BookProcessingResult processBookFile(Integer bookId, Integer userId);
    
    /**
     * Move book file to different storage category
     */
    BookResponse moveBookFile(Integer bookId, String newCategory, Integer userId);
    
    /**
     * Create backup of book file
     */
    String createBookFileBackup(Integer bookId, Integer userId);
    
    /**
     * Restore book file from backup
     */
    BookResponse restoreBookFileFromBackup(Integer bookId, String backupPath, Integer userId);
    
    /**
     * Get file download statistics
     */
    BookFileStatistics getBookFileStatistics(Integer bookId);
    
    /**
     * Clean up orphaned files (files without corresponding book records)
     */
    OrphanCleanupResult cleanupOrphanedFiles();
}

/**
 * Book file validation result
 */
class BookFileValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final BookFileMetadata extractedMetadata;
    
    public BookFileValidationResult(boolean valid, List<String> errors, List<String> warnings, 
                                  BookFileMetadata extractedMetadata) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.extractedMetadata = extractedMetadata;
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public BookFileMetadata getExtractedMetadata() { return extractedMetadata; }
}

/**
 * Extracted metadata from book file
 */
class BookFileMetadata {
    private final String title;
    private final String author;
    private final String language;
    private final Integer pageCount;
    private final String isbn;
    private final String publisher;
    private final Integer publicationYear;
    private final String description;
    private final List<String> subjects;
    
    public BookFileMetadata(String title, String author, String language, Integer pageCount,
                           String isbn, String publisher, Integer publicationYear, 
                           String description, List<String> subjects) {
        this.title = title;
        this.author = author;
        this.language = language;
        this.pageCount = pageCount;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.description = description;
        this.subjects = subjects;
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getLanguage() { return language; }
    public Integer getPageCount() { return pageCount; }
    public String getIsbn() { return isbn; }
    public String getPublisher() { return publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getDescription() { return description; }
    public List<String> getSubjects() { return subjects; }
}

/**
 * Upload progress tracking
 */
class UploadProgress {
    private final String uploadId;
    private final String fileName;
    private final long totalBytes;
    private final long uploadedBytes;
    private final double percentage;
    private final UploadStatus status;
    private final String message;
    private final java.time.LocalDateTime startTime;
    private final java.time.LocalDateTime lastUpdate;
    
    public UploadProgress(String uploadId, String fileName, long totalBytes, long uploadedBytes,
                         UploadStatus status, String message, java.time.LocalDateTime startTime,
                         java.time.LocalDateTime lastUpdate) {
        this.uploadId = uploadId;
        this.fileName = fileName;
        this.totalBytes = totalBytes;
        this.uploadedBytes = uploadedBytes;
        this.percentage = totalBytes > 0 ? (double) uploadedBytes / totalBytes * 100 : 0;
        this.status = status;
        this.message = message;
        this.startTime = startTime;
        this.lastUpdate = lastUpdate;
    }
    
    // Getters
    public String getUploadId() { return uploadId; }
    public String getFileName() { return fileName; }
    public long getTotalBytes() { return totalBytes; }
    public long getUploadedBytes() { return uploadedBytes; }
    public double getPercentage() { return percentage; }
    public UploadStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public java.time.LocalDateTime getStartTime() { return startTime; }
    public java.time.LocalDateTime getLastUpdate() { return lastUpdate; }
}

/**
 * Upload status enumeration
 */
enum UploadStatus {
    PENDING,
    IN_PROGRESS,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Book processing result
 */
class BookProcessingResult {
    private final boolean success;
    private final String message;
    private final BookFileMetadata extractedMetadata;
    private final String thumbnailPath;
    private final List<String> generatedFiles;
    private final java.time.LocalDateTime processedAt;
    
    public BookProcessingResult(boolean success, String message, BookFileMetadata extractedMetadata,
                               String thumbnailPath, List<String> generatedFiles,
                               java.time.LocalDateTime processedAt) {
        this.success = success;
        this.message = message;
        this.extractedMetadata = extractedMetadata;
        this.thumbnailPath = thumbnailPath;
        this.generatedFiles = generatedFiles;
        this.processedAt = processedAt;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public BookFileMetadata getExtractedMetadata() { return extractedMetadata; }
    public String getThumbnailPath() { return thumbnailPath; }
    public List<String> getGeneratedFiles() { return generatedFiles; }
    public java.time.LocalDateTime getProcessedAt() { return processedAt; }
}

/**
 * Book file statistics
 */
class BookFileStatistics {
    private final Integer bookId;
    private final String fileName;
    private final long fileSizeBytes;
    private final Integer downloadCount;
    private final Integer viewCount;
    private final java.time.LocalDateTime lastDownload;
    private final java.time.LocalDateTime lastView;
    private final java.util.Map<String, Integer> downloadsByCountry;
    private final java.util.Map<String, Integer> downloadsByMonth;
    
    public BookFileStatistics(Integer bookId, String fileName, long fileSizeBytes,
                             Integer downloadCount, Integer viewCount,
                             java.time.LocalDateTime lastDownload, java.time.LocalDateTime lastView,
                             java.util.Map<String, Integer> downloadsByCountry,
                             java.util.Map<String, Integer> downloadsByMonth) {
        this.bookId = bookId;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.downloadCount = downloadCount;
        this.viewCount = viewCount;
        this.lastDownload = lastDownload;
        this.lastView = lastView;
        this.downloadsByCountry = downloadsByCountry;
        this.downloadsByMonth = downloadsByMonth;
    }
    
    // Getters
    public Integer getBookId() { return bookId; }
    public String getFileName() { return fileName; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public Integer getDownloadCount() { return downloadCount; }
    public Integer getViewCount() { return viewCount; }
    public java.time.LocalDateTime getLastDownload() { return lastDownload; }
    public java.time.LocalDateTime getLastView() { return lastView; }
    public java.util.Map<String, Integer> getDownloadsByCountry() { return downloadsByCountry; }
    public java.util.Map<String, Integer> getDownloadsByMonth() { return downloadsByMonth; }
}

/**
 * Orphan cleanup result
 */
class OrphanCleanupResult {
    private final int orphanedFilesFound;
    private final int filesDeleted;
    private final long spaceFreedBytes;
    private final List<String> deletedFiles;
    private final List<String> errors;
    private final java.time.LocalDateTime cleanupTime;
    
    public OrphanCleanupResult(int orphanedFilesFound, int filesDeleted, long spaceFreedBytes,
                              List<String> deletedFiles, List<String> errors,
                              java.time.LocalDateTime cleanupTime) {
        this.orphanedFilesFound = orphanedFilesFound;
        this.filesDeleted = filesDeleted;
        this.spaceFreedBytes = spaceFreedBytes;
        this.deletedFiles = deletedFiles;
        this.errors = errors;
        this.cleanupTime = cleanupTime;
    }
    
    // Getters
    public int getOrphanedFilesFound() { return orphanedFilesFound; }
    public int getFilesDeleted() { return filesDeleted; }
    public long getSpaceFreedBytes() { return spaceFreedBytes; }
    public List<String> getDeletedFiles() { return deletedFiles; }
    public List<String> getErrors() { return errors; }
    public java.time.LocalDateTime getCleanupTime() { return cleanupTime; }
}
