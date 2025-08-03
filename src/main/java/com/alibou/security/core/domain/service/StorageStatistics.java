package com.alibou.security.core.domain.service;

import java.util.Map;

/**
 * Storage statistics
 */
public class StorageStatistics {
    private final long totalFiles;
    private final long totalSizeBytes;
    private final long availableSpaceBytes;
    private final Map<String, Long> filesByCategory;
    private final Map<String, Long> sizeByCategory;
    
    public StorageStatistics(long totalFiles, long totalSizeBytes, long availableSpaceBytes,
                           Map<String, Long> filesByCategory, 
                           Map<String, Long> sizeByCategory) {
        this.totalFiles = totalFiles;
        this.totalSizeBytes = totalSizeBytes;
        this.availableSpaceBytes = availableSpaceBytes;
        this.filesByCategory = filesByCategory;
        this.sizeByCategory = sizeByCategory;
    }
    
    // Getters
    public long getTotalFiles() { return totalFiles; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public long getAvailableSpaceBytes() { return availableSpaceBytes; }
    public Map<String, Long> getFilesByCategory() { return filesByCategory; }
    public Map<String, Long> getSizeByCategory() { return sizeByCategory; }
}
