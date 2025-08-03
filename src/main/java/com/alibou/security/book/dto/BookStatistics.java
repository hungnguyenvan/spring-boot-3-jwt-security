package com.alibou.security.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for book statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatistics {
    
    private Long totalBooks;
    private Long activeBooks;
    private Long inactiveBooks;
    private Long booksByType;
    private Long recentBooks; // Books created in last 30 days
    private Double averageRating;
    private Long totalReviews;
    private String mostPopularBookType;
    private String recentlyAddedBook;
}
