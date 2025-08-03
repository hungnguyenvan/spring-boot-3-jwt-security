package com.alibou.security.book.application.service;

import java.util.List;

/**
 * Book processing result
 */
public class BookProcessingResult {
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
