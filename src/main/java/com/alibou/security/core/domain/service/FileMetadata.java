package com.alibou.security.core.domain.service;

import java.time.LocalDateTime;

/**
 * File metadata information
 */
public class FileMetadata {
    private final String filePath;
    private final String fileName;
    private final String contentType;
    private final long fileSize;
    private final String checksum;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModified;
    private final String category;
    private final Integer uploadedBy;
    
    public FileMetadata(String filePath, String fileName, String contentType, long fileSize,
                       String checksum, LocalDateTime createdDate, 
                       LocalDateTime lastModified, String category, Integer uploadedBy) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
        this.category = category;
        this.uploadedBy = uploadedBy;
    }
    
    // Getters
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getChecksum() { return checksum; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    public String getCategory() { return category; }
    public Integer getUploadedBy() { return uploadedBy; }
}
