package com.alibou.security.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    // Search functionality
    @Query("SELECT b FROM Book b WHERE b.active = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Book> findActiveBooksBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Book> findAllBooksBySearchTerm(@Param("search") String search, Pageable pageable);

    // Filter by book type
    List<Book> findByBookTypeIdAndActiveTrue(Integer bookTypeId);

    Page<Book> findByBookTypeIdAndActiveTrue(Integer bookTypeId, Pageable pageable);

    // Filter by free/paid status
    Page<Book> findByIsFreeAndActiveTrue(Boolean isFree, Pageable pageable);

    // Filter by downloadable
    Page<Book> findByDownloadableAndActiveTrue(Boolean downloadable, Pageable pageable);

    // Find by author
    Page<Book> findByAuthorContainingIgnoreCaseAndActiveTrue(String author, Pageable pageable);

    // Find by ISBN
    Optional<Book> findByIsbn(String isbn);

    // Find by file format
    List<Book> findByFileFormatAndActiveTrue(String fileFormat);

    // Statistics queries
    long countByActiveTrue();

    long countByIsFreeAndActiveTrue(Boolean isFree);

    long countByDownloadableAndActiveTrue(Boolean downloadable);

    long countByBookTypeIdAndActiveTrue(Integer bookTypeId);

    // Popular books (most downloaded)
    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY b.downloadCount DESC")
    Page<Book> findMostDownloadedBooks(Pageable pageable);

    // Highly rated books
    @Query("SELECT b FROM Book b WHERE b.active = true AND b.rating >= :minRating ORDER BY b.rating DESC")
    Page<Book> findHighlyRatedBooks(@Param("minRating") double minRating, Pageable pageable);

    // Recent books
    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY b.createDate DESC")
    Page<Book> findRecentBooks(Pageable pageable);

    // Books by publisher
    Page<Book> findByPublisherContainingIgnoreCaseAndActiveTrue(String publisher, Pageable pageable);

    // Books by language
    List<Book> findByLanguageAndActiveTrue(String language);

    // Books by publication year
    List<Book> findByPublicationYearAndActiveTrue(Integer year);

    boolean existsByIsbn(String isbn);

    boolean existsByTitle(String title);
}
