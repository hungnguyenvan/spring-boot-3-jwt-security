package com.alibou.security.document.web.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String uploadTime;
    private boolean success;
    private String message;
}
