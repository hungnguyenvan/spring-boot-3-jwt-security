package com.alibou.security.book;

import com.alibou.security.booktype.BookType;
import com.alibou.security.booktype.BookTypeRepository;
import com.alibou.security.exception.ResourceNotFoundException;
import com.alibou.security.user.Role;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookTypeRepository bookTypeRepository;
    private final UserRepository userRepository;

    public Page<BookResponse> getAllBooks(String search, Pageable pageable, boolean activeOnly) {
        Page<Book> books;
        
        if (search == null || search.trim().isEmpty()) {
            books = activeOnly ? 
                bookRepository.findAll(pageable) : 
                bookRepository.findAll(pageable);
        } else {
            books = activeOnly ? 
                bookRepository.findActiveBooksBySearchTerm(search.trim(), pageable) :
                bookRepository.findAllBooksBySearchTerm(search.trim(), pageable);
        }
        
        // Return public version for non-admin users
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return books.map(BookResponse::fromEntityPublic);
        }
        
        return books.map(BookResponse::fromEntity);
    }

    public BookResponse getBookById(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        
        // Increment view count
        book.setViewCount(book.getViewCount() + 1);
        bookRepository.save(book);
        
        // Return appropriate version based on user role
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return BookResponse.fromEntityPublic(book);
        }
        
        return BookResponse.fromEntity(book);
    }

    public BookResponse createBook(BookRequest request) {
        checkAdminOrEditorPermission();
        
        // Validate unique constraints
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("ISBN đã tồn tại: " + request.getIsbn());
        }
        
        if (bookRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Tiêu đề sách đã tồn tại: " + request.getTitle());
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .fileName(request.getFileName())
                .fileFormat(request.getFileFormat())
                .fileSize(request.getFileSize())
                .isFree(request.getIsFree() != null ? request.getIsFree() : true)
                .price(request.getPrice())
                .downloadable(request.getDownloadable() != null ? request.getDownloadable() : true)
                .active(request.getActive() != null ? request.getActive() : true)
                .publisher(request.getPublisher())
                .publicationYear(request.getPublicationYear())
                .language(request.getLanguage())
                .pageCount(request.getPageCount())
                .rating(BigDecimal.ZERO)
                .downloadCount(0)
                .viewCount(0)
                .build();

        // Set book type if provided
        if (request.getBookTypeId() != null) {
            BookType bookType = bookTypeRepository.findById(request.getBookTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + request.getBookTypeId()));
            book.setBookType(bookType);
        }

        Book savedBook = bookRepository.save(book);
        log.info("Created new book: {} by user: {}", savedBook.getTitle(), getCurrentUsername());
        
        return BookResponse.fromEntity(savedBook);
    }

    public BookResponse updateBook(Integer id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));

        checkUpdatePermission(book);

        // Check unique constraints (excluding current record)
        if (request.getIsbn() != null && !book.getIsbn().equals(request.getIsbn()) && 
            bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("ISBN đã tồn tại: " + request.getIsbn());
        }
        
        if (!book.getTitle().equals(request.getTitle()) && 
            bookRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Tiêu đề sách đã tồn tại: " + request.getTitle());
        }

        // Update fields
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setFilePath(request.getFilePath());
        book.setFileName(request.getFileName());
        book.setFileFormat(request.getFileFormat());
        book.setFileSize(request.getFileSize());
        book.setIsFree(request.getIsFree() != null ? request.getIsFree() : book.getIsFree());
        book.setPrice(request.getPrice());
        book.setDownloadable(request.getDownloadable() != null ? request.getDownloadable() : book.getDownloadable());
        book.setActive(request.getActive() != null ? request.getActive() : book.getActive());
        book.setPublisher(request.getPublisher());
        book.setPublicationYear(request.getPublicationYear());
        book.setLanguage(request.getLanguage());
        book.setPageCount(request.getPageCount());

        // Update book type if provided
        if (request.getBookTypeId() != null) {
            BookType bookType = bookTypeRepository.findById(request.getBookTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + request.getBookTypeId()));
            book.setBookType(bookType);
        }

        Book updatedBook = bookRepository.save(book);
        log.info("Updated book: {} by user: {}", updatedBook.getTitle(), getCurrentUsername());
        
        return BookResponse.fromEntity(updatedBook);
    }

    public void deleteBook(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));

        checkDeletePermission(book);

        bookRepository.delete(book);
        log.info("Deleted book: {} by user: {}", book.getTitle(), getCurrentUsername());
    }

    public Page<BookResponse> getBooksByType(Integer bookTypeId, Pageable pageable) {
        Page<Book> books = bookRepository.findByBookTypeIdAndActiveTrue(bookTypeId, pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getFreeBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findByIsFreeAndActiveTrue(true, pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getPaidBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findByIsFreeAndActiveTrue(false, pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getDownloadableBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findByDownloadableAndActiveTrue(true, pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getMostDownloadedBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findMostDownloadedBooks(pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getHighlyRatedBooks(double minRating, Pageable pageable) {
        Page<Book> books = bookRepository.findHighlyRatedBooks(minRating, pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public Page<BookResponse> getRecentBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findRecentBooks(pageable);
        return books.map(BookResponse::fromEntityPublic);
    }

    public String downloadBook(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));

        if (!book.getDownloadable()) {
            throw new AccessDeniedException("Sách này không cho phép tải về");
        }

        if (!book.getIsFree()) {
            // TODO: Check if user has purchased the book
            throw new AccessDeniedException("Bạn cần mua sách này trước khi tải về");
        }

        // Increment download count
        book.setDownloadCount(book.getDownloadCount() + 1);
        bookRepository.save(book);

        log.info("Book downloaded: {} by user: {}", book.getTitle(), getCurrentUsername());
        
        return book.getFilePath();
    }

    // Permission checking methods
    private void checkAdminOrEditorPermission() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.EDITOR) {
            throw new AccessDeniedException("Chỉ ADMIN hoặc EDITOR mới có quyền thực hiện thao tác này");
        }
    }

    private void checkUpdatePermission(Book book) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admin can edit everything
        }
        
        if (currentUser.getRole() == Role.EDITOR) {
            // TODO: Check if editor has permission for this book's type
            return;
        }
        
        throw new AccessDeniedException("Bạn không có quyền chỉnh sửa sách này");
    }

    private void checkDeletePermission(Book book) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admin can delete everything
        }
        
        if (currentUser.getRole() == Role.EDITOR) {
            // TODO: Check if editor has delete permission for this book's type
            return;
        }
        
        throw new AccessDeniedException("Bạn không có quyền xóa sách này");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user hiện tại"));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
}
