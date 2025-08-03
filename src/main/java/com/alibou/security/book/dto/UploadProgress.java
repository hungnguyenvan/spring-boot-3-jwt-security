package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for upload progress tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadProgress {
    
    private String uploadId;
    private String fileName;
    private Long totalBytes;
    private Long uploadedBytes;
    private Double percentage;
    private String status; // UPLOADING, COMPLETED, FAILED
    private String message;
    private Long startTime;
    private Long endTime;
    
    public static UploadProgress started(String uploadId, String fileName, Long totalBytes) {
        return UploadProgress.builder()
            .uploadId(uploadId)
            .fileName(fileName)
            .totalBytes(totalBytes)
            .uploadedBytes(0L)
            .percentage(0.0)
            .status("UPLOADING")
            .startTime(System.currentTimeMillis())
            .build();
    }
    
    public static UploadProgress completed(String uploadId, String fileName, Long totalBytes) {
        return UploadProgress.builder()
            .uploadId(uploadId)
            .fileName(fileName)
            .totalBytes(totalBytes)
            .uploadedBytes(totalBytes)
            .percentage(100.0)
            .status("COMPLETED")
            .endTime(System.currentTimeMillis())
            .build();
    }
    
    public static UploadProgress failed(String uploadId, String fileName, String message) {
        return UploadProgress.builder()
            .uploadId(uploadId)
            .fileName(fileName)
            .percentage(0.0)
            .status("FAILED")
            .message(message)
            .endTime(System.currentTimeMillis())
            .build();
    }
}
