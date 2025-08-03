package com.alibou.security.core.domain.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Domain service for file storage operations
 * Abstracts the underlying storage mechanism (local, S3, etc.)
 */
public interface FileStorageService {
    
    /**
     * Store a file and return storage metadata
     */
    FileStorageResult storeFile(MultipartFile file, String category, Integer userId);
    
    /**
     * Store file from input stream
     */
    FileStorageResult storeFile(InputStream inputStream, String originalFileName, 
                               String contentType, long size, String category, Integer userId);
    
    /**
     * Delete a file
     */
    void deleteFile(String filePath);
    
    /**
     * Get file as input stream for download
     */
    InputStream getFileAsStream(String filePath);
    
    /**
     * Get file metadata
     */
    FileMetadata getFileMetadata(String filePath);
    
    /**
     * Generate secure download URL (for temporary access)
     */
    String generateDownloadUrl(String filePath, Integer userId, long expirationMinutes);
    
    /**
     * Validate file before storage
     */
    FileValidationResult validateFile(MultipartFile file, String category);
    
    /**
     * Get storage statistics
     */
    StorageStatistics getStorageStatistics();
    
    /**
     * Cleanup old temporary files
     */
    void cleanupTemporaryFiles();
    
    /**
     * Move file to different category/location
     */
    FileStorageResult moveFile(String currentPath, String newCategory);
    
    /**
     * Check if file exists
     */
    boolean fileExists(String filePath);
    
    /**
     * Get file size
     */
    long getFileSize(String filePath);
    
    /**
     * Create backup of file
     */
    String createBackup(String filePath);
    
    /**
     * Restore file from backup
     */
    void restoreFromBackup(String backupPath, String originalPath);
}
