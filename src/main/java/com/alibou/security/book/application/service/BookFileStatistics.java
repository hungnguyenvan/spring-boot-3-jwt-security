package com.alibou.security.book.application.service;

/**
 * Book file statistics
 */
public class BookFileStatistics {
    private final Integer bookId;
    private final String fileName;
    private final long fileSizeBytes;
    private final Integer downloadCount;
    private final Integer viewCount;
    private final java.time.LocalDateTime lastDownload;
    private final java.time.LocalDateTime lastView;
    private final java.util.Map<String, Integer> downloadsByCountry;
    private final java.util.Map<String, Integer> downloadsByMonth;
    
    public BookFileStatistics(Integer bookId, String fileName, long fileSizeBytes,
                             Integer downloadCount, Integer viewCount,
                             java.time.LocalDateTime lastDownload, java.time.LocalDateTime lastView,
                             java.util.Map<String, Integer> downloadsByCountry,
                             java.util.Map<String, Integer> downloadsByMonth) {
        this.bookId = bookId;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.downloadCount = downloadCount;
        this.viewCount = viewCount;
        this.lastDownload = lastDownload;
        this.lastView = lastView;
        this.downloadsByCountry = downloadsByCountry;
        this.downloadsByMonth = downloadsByMonth;
    }
    
    // Getters
    public Integer getBookId() { return bookId; }
    public String getFileName() { return fileName; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public Integer getDownloadCount() { return downloadCount; }
    public Integer getViewCount() { return viewCount; }
    public java.time.LocalDateTime getLastDownload() { return lastDownload; }
    public java.time.LocalDateTime getLastView() { return lastView; }
    public java.util.Map<String, Integer> getDownloadsByCountry() { return downloadsByCountry; }
    public java.util.Map<String, Integer> getDownloadsByMonth() { return downloadsByMonth; }
}
