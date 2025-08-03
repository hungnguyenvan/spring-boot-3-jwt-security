package com.alibou.security.book.application.service;

import java.util.List;

/**
 * Orphan cleanup result
 */
public class OrphanCleanupResult {
    private final int orphanedFilesFound;
    private final int filesDeleted;
    private final long spaceFreedBytes;
    private final List<String> deletedFiles;
    private final List<String> errors;
    private final java.time.LocalDateTime cleanupTime;
    
    public OrphanCleanupResult(int orphanedFilesFound, int filesDeleted, long spaceFreedBytes,
                              List<String> deletedFiles, List<String> errors,
                              java.time.LocalDateTime cleanupTime) {
        this.orphanedFilesFound = orphanedFilesFound;
        this.filesDeleted = filesDeleted;
        this.spaceFreedBytes = spaceFreedBytes;
        this.deletedFiles = deletedFiles;
        this.errors = errors;
        this.cleanupTime = cleanupTime;
    }
    
    // Getters
    public int getOrphanedFilesFound() { return orphanedFilesFound; }
    public int getFilesDeleted() { return filesDeleted; }
    public long getSpaceFreedBytes() { return spaceFreedBytes; }
    public List<String> getDeletedFiles() { return deletedFiles; }
    public List<String> getErrors() { return errors; }
    public java.time.LocalDateTime getCleanupTime() { return cleanupTime; }
}
