package com.alibou.security.core.domain.service;

/**
 * File storage result with metadata
 */
public class FileStorageResult {
    private final String filePath;
    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private final long fileSize;
    private final String checksum;
    private final String category;
    private final boolean success;
    private final String errorMessage;
    
    // Constructor for successful storage
    public FileStorageResult(String filePath, String fileName, String originalFileName,
                           String contentType, long fileSize, String checksum, String category) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.category = category;
        this.success = true;
        this.errorMessage = null;
    }
    
    // Constructor for failed storage
    public FileStorageResult(String errorMessage) {
        this.filePath = null;
        this.fileName = null;
        this.originalFileName = null;
        this.contentType = null;
        this.fileSize = 0;
        this.checksum = null;
        this.category = null;
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getOriginalFileName() { return originalFileName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getChecksum() { return checksum; }
    public String getCategory() { return category; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
