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
    
    /**
     * Get download URL for book file
     */
    String getDownloadUrl(Integer bookId, Integer userId);
}
