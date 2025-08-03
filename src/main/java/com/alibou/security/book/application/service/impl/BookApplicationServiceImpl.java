package com.alibou.security.book.application.service.impl;

import com.alibou.security.book.*;
import com.alibou.security.book.application.service.BookApplicationService;
import com.alibou.security.book.dto.BookStatistics;
import com.alibou.security.core.application.service.impl.BaseApplicationServiceImpl;
import com.alibou.security.core.domain.service.PermissionService;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clean Architecture implementation of Book Application Service
 * Orchestrates business use cases and delegates to domain services
 */
@Service
@Slf4j
public class BookApplicationServiceImpl 
    extends BaseApplicationServiceImpl<BookV2, BookRequest, BookResponse, Integer>
    implements BookApplicationService {

    private final BookRepository bookRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    public BookApplicationServiceImpl(
            BookRepository bookRepository,
            PermissionService permissionService,
            UserRepository userRepository) {
        super(bookRepository);
        this.bookRepository = bookRepository;
        this.permissionService = permissionService;
        this.userRepository = userRepository;
    }

    @Override
    public Page<BookResponse> findByBookType(Integer bookTypeId, Pageable pageable) {
        log.debug("Finding books by book type: {}", bookTypeId);
        return bookRepository.findByBookTypeIdAndActiveTrue(bookTypeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<BookResponse> findFreeBooks(Pageable pageable) {
        log.debug("Finding free books");
        return bookRepository.findByIsFreeAndActiveTrue(true, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<BookResponse> findPaidBooks(Pageable pageable) {
        log.debug("Finding paid books");
        return bookRepository.findByIsFreeAndActiveTrue(false, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<BookResponse> findDownloadableBooks(Pageable pageable) {
        log.debug("Finding downloadable books");
        return bookRepository.findByDownloadableAndActiveTrue(true, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<BookResponse> searchBooks(String title, String author, String isbn, 
                                        Integer bookTypeId, Boolean isFree, Pageable pageable) {
        log.debug("Searching books with criteria - title: {}, author: {}, isbn: {}, bookTypeId: {}, isFree: {}", 
                 title, author, isbn, bookTypeId, isFree);
        
        return bookRepository.searchBooks(title, author, isbn, bookTypeId, isFree, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public BookStatistics getBookStatistics() {
        log.debug("Getting book statistics");
        
        long totalBooks = bookRepository.countByActiveTrue();
        long freeBooks = bookRepository.countByIsFreeAndActiveTrue(true);
        long paidBooks = bookRepository.countByIsFreeAndActiveTrue(false);
        long downloadableBooks = bookRepository.countByDownloadableAndActiveTrue(true);
        double averageRating = bookRepository.getAverageRating();
        long totalDownloads = bookRepository.getTotalDownloads();
        long totalViews = bookRepository.getTotalViews();
        
        return new BookStatistics(totalBooks, freeBooks, paidBooks, downloadableBooks, 
                                averageRating, totalDownloads, totalViews);
    }

    @Override
    public List<BookResponse> getMostDownloadedBooks(int limit) {
        log.debug("Getting most downloaded books, limit: {}", limit);
        return bookRepository.findTopByDownloadCountDesc(limit)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getHighestRatedBooks(int limit) {
        log.debug("Getting highest rated books, limit: {}", limit);
        return bookRepository.findTopByRatingDesc(limit)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateRating(Integer bookId, Double newRating) {
        log.debug("Updating rating for book {} to {}", bookId, newRating);
        
        BookV2 book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        book.updateRating(BigDecimal.valueOf(newRating));
        bookRepository.save(book);
        
        log.info("Updated rating for book {} to {}", bookId, newRating);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Integer bookId) {
        log.debug("Incrementing download count for book {}", bookId);
        
        BookV2 book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        book.incrementDownloadCount();
        bookRepository.save(book);
        
        log.info("Incremented download count for book {}", bookId);
    }

    @Override
    @Transactional
    public void incrementViewCount(Integer bookId) {
        log.debug("Incrementing view count for book {}", bookId);
        
        BookV2 book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        book.incrementViewCount();
        bookRepository.save(book);
        
        log.info("Incremented view count for book {}", bookId);
    }

    @Override
    public boolean canUserDownloadBook(Integer userId, Integer bookId) {
        log.debug("Checking if user {} can download book {}", userId, bookId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return permissionService.canDownloadBook(user, bookId);
    }

    @Override
    public String getDownloadUrl(Integer userId, Integer bookId) {
        log.debug("Getting download URL for user {} and book {}", userId, bookId);
        
        if (!canUserDownloadBook(userId, bookId)) {
            throw new RuntimeException("User does not have permission to download this book");
        }
        
        BookV2 book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        if (!book.isDownloadAllowed()) {
            throw new RuntimeException("Book is not available for download");
        }
        
        // Increment download count
        incrementDownloadCount(bookId);
        
        // Generate or return download URL
        return generateDownloadUrl(book);
    }

    private String generateDownloadUrl(BookV2 book) {
        // Implementation would generate secure download URL
        // This could involve creating temporary signed URLs, etc.
        return "/api/books/" + book.getId() + "/download?token=" + System.currentTimeMillis();
    }

    @Override
    protected BookV2 mapToEntity(BookRequest request) {
        return BookV2.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .fileFormat(request.getFileFormat())
                .fileSize(request.getFileSize())
                .isFree(request.getIsFree())
                .price(request.getPrice())
                .downloadable(request.getDownloadable())
                .publisher(request.getPublisher())
                .publicationYear(request.getPublicationYear())
                .language(request.getLanguage())
                .pageCount(request.getPageCount())
                .build();
    }

    @Override
    protected BookResponse mapToResponse(BookV2 entity) {
        return BookResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .isbn(entity.getIsbn())
                .description(entity.getDescription())
                .bookTypeName(entity.getBookType() != null ? entity.getBookType().getName() : null)
                .filePath(entity.getFilePath())
                .fileName(entity.getFileName())
                .fileFormat(entity.getFileFormat())
                .fileSize(entity.getFileSize())
                .isFree(entity.getIsFree())
                .price(entity.getPrice())
                .downloadable(entity.getDownloadable())
                .publisher(entity.getPublisher())
                .publicationYear(entity.getPublicationYear())
                .language(entity.getLanguage())
                .pageCount(entity.getPageCount())
                .rating(entity.getRating())
                .downloadCount(entity.getDownloadCount())
                .viewCount(entity.getViewCount())
                .active(entity.getActive())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    @Override
    protected void updateEntityFromRequest(BookV2 entity, BookRequest request) {
        entity.setTitle(request.getTitle());
        entity.setAuthor(request.getAuthor());
        entity.setIsbn(request.getIsbn());
        entity.setDescription(request.getDescription());
        entity.setFilePath(request.getFilePath());
        entity.setFileName(request.getFileName());
        entity.setFileFormat(request.getFileFormat());
        entity.setFileSize(request.getFileSize());
        entity.setIsFree(request.getIsFree());
        entity.setPrice(request.getPrice());
        entity.setDownloadable(request.getDownloadable());
        entity.setPublisher(request.getPublisher());
        entity.setPublicationYear(request.getPublicationYear());
        entity.setLanguage(request.getLanguage());
        entity.setPageCount(request.getPageCount());
    }

    // Implementation for BaseApplicationService interface but using BookV2
    @Override
    public BookResponse create(BookRequest request) {
        return super.create(request);
    }

    @Override
    public BookResponse update(Integer id, BookRequest request) {
        return super.update(id, request);
    }

    // Other interface methods are inherited from BaseApplicationServiceImpl
}
