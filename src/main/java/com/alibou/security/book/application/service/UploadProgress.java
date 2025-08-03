package com.alibou.security.book.application.service;

/**
 * Upload progress tracking
 */
public class UploadProgress {
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
