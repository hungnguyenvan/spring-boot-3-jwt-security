package com.alibou.security.book.application.service;

import com.alibou.security.book.Book;
import com.alibou.security.book.BookRequest;
import com.alibou.security.book.BookResponse;
import com.alibou.security.core.application.service.BaseApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Application service interface for Book management
 * Extends base service with book-specific operations
 */
public interface BookApplicationService extends BaseApplicationService<Book, BookRequest, BookResponse, Integer> {
    
    /**
     * Find books by book type
     */
    Page<BookResponse> findByBookType(Integer bookTypeId, Pageable pageable);
    
    /**
     * Find free books only
     */
    Page<BookResponse> findFreeBooks(Pageable pageable);
    
    /**
     * Find paid books only
     */
    Page<BookResponse> findPaidBooks(Pageable pageable);
    
    /**
     * Find downloadable books
     */
    Page<BookResponse> findDownloadableBooks(Pageable pageable);
    
    /**
     * Search books by multiple criteria
     */
    Page<BookResponse> searchBooks(String title, String author, String isbn, 
                                  Integer bookTypeId, Boolean isFree, Pageable pageable);
    
    /**
     * Get books statistics
     */
    BookStatistics getBookStatistics();
    
    /**
     * Get most downloaded books
     */
    List<BookResponse> getMostDownloadedBooks(int limit);
    
    /**
     * Get highest rated books
     */
    List<BookResponse> getHighestRatedBooks(int limit);
    
    /**
     * Update book rating
     */
    void updateRating(Integer bookId, Double newRating);
    
    /**
     * Increment download count
     */
    void incrementDownloadCount(Integer bookId);
    
    /**
     * Increment view count
     */
    void incrementViewCount(Integer bookId);
    
    /**
     * Check if user can download book
     */
    boolean canUserDownloadBook(Integer userId, Integer bookId);
    
    /**
     * Get download URL for book (if user has permission)
     */
    String getDownloadUrl(Integer userId, Integer bookId);
}

/**
 * Book statistics DTO
 */
class BookStatistics {
    private final long totalBooks;
    private final long freeBooks;
    private final long paidBooks;
    private final long downloadableBooks;
    private final double averageRating;
    private final long totalDownloads;
    private final long totalViews;
    
    public BookStatistics(long totalBooks, long freeBooks, long paidBooks, 
                         long downloadableBooks, double averageRating, 
                         long totalDownloads, long totalViews) {
        this.totalBooks = totalBooks;
        this.freeBooks = freeBooks;
        this.paidBooks = paidBooks;
        this.downloadableBooks = downloadableBooks;
        this.averageRating = averageRating;
        this.totalDownloads = totalDownloads;
        this.totalViews = totalViews;
    }
    
    // Getters
    public long getTotalBooks() { return totalBooks; }
    public long getFreeBooks() { return freeBooks; }
    public long getPaidBooks() { return paidBooks; }
    public long getDownloadableBooks() { return downloadableBooks; }
    public double getAverageRating() { return averageRating; }
    public long getTotalDownloads() { return totalDownloads; }
    public long getTotalViews() { return totalViews; }
}
