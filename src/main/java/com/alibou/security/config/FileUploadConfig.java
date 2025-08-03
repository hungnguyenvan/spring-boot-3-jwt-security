package com.alibou.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for file upload functionality
 */
@Configuration
@ConfigurationProperties(prefix = "app.file-upload")
public class FileUploadConfig {
    
    private String basePath = "./uploads";
    private long maxFileSize = 50L * 1024 * 1024; // 50MB
    private long maxRequestSize = 100L * 1024 * 1024; // 100MB
    private String allowedTypes = "pdf,epub,mobi,txt,doc,docx";
    private String tempDirectory = "./uploads/temp";
    private int tempCleanupHours = 24;
    private boolean enableAsyncProcessing = true;
    private int maxConcurrentUploads = 5;
    
    // Getters and setters
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public long getMaxRequestSize() { return maxRequestSize; }
    public void setMaxRequestSize(long maxRequestSize) { this.maxRequestSize = maxRequestSize; }
    
    public String getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }
    
    public String getTempDirectory() { return tempDirectory; }
    public void setTempDirectory(String tempDirectory) { this.tempDirectory = tempDirectory; }
    
    public int getTempCleanupHours() { return tempCleanupHours; }
    public void setTempCleanupHours(int tempCleanupHours) { this.tempCleanupHours = tempCleanupHours; }
    
    public boolean isEnableAsyncProcessing() { return enableAsyncProcessing; }
    public void setEnableAsyncProcessing(boolean enableAsyncProcessing) { this.enableAsyncProcessing = enableAsyncProcessing; }
    
    public int getMaxConcurrentUploads() { return maxConcurrentUploads; }
    public void setMaxConcurrentUploads(int maxConcurrentUploads) { this.maxConcurrentUploads = maxConcurrentUploads; }
}
