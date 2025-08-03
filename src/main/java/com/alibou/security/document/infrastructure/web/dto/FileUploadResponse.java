package com.alibou.security.document.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for file upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private boolean success;
    private String message;
    private String temporaryFileId;
    private String originalFileName;
    private String fileFormat;
    private Long fileSize;
    private String checksum;
    private String errorCode;
    
    /**
     * Create successful upload response
     */
    public static FileUploadResponse success(String tempFileId, String fileName, String format, Long size, String checksum) {
        return FileUploadResponse.builder()
            .success(true)
            .message("File uploaded successfully")
            .temporaryFileId(tempFileId)
            .originalFileName(fileName)
            .fileFormat(format)
            .fileSize(size)
            .checksum(checksum)
            .build();
    }
    
    /**
     * Create error response
     */
    public static FileUploadResponse error(String message, String errorCode) {
        return FileUploadResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
    
    /**
     * Create error response with default error code
     */
    public static FileUploadResponse error(String message) {
        return error(message, "UPLOAD_ERROR");
    }
}
