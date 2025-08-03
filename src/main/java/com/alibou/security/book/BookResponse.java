package com.alibou.security.book;

import com.alibou.security.booktype.BookTypeResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookResponse {

    private Integer id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String synopsis;
    private String category;
    private BookTypeResponse bookType;
    private String bookTypeName;
    private boolean archived;
    private Boolean shareable;
    private boolean available;
    
    // File information
    private String filePath;
    private String fileName;
    private String fileFormat;
    private Long fileSize;
    
    // Pricing and access
    private Boolean isFree;
    private BigDecimal price;
    private Boolean downloadable;
    private Boolean active;
    
    // Publication info
    private String publisher;
    private Integer publicationYear;
    private String language;
    private Integer pageCount;
    
    // Statistics
    private BigDecimal rating;
    private Integer downloadCount;
    private Integer viewCount;
    
    // Audit fields
    private LocalDateTime createDate;
    private LocalDateTime lastModified;
    private Integer createdBy;
    private Integer lastModifiedBy;

    public static BookResponse fromEntity(Book book) {
        BookResponseBuilder builder = BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .filePath(book.getFilePath())
                .fileName(book.getFileName())
                .fileFormat(book.getFileFormat())
                .fileSize(book.getFileSize())
                .isFree(book.getIsFree())
                .price(book.getPrice())
                .downloadable(book.getDownloadable())
                .active(book.getActive())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .language(book.getLanguage())
                .pageCount(book.getPageCount())
                .rating(book.getRating())
                .downloadCount(book.getDownloadCount())
                .viewCount(book.getViewCount())
                .createDate(book.getCreateDate())
                .lastModified(book.getLastModified())
                .createdBy(book.getCreatedBy())
                .lastModifiedBy(book.getLastModifiedBy());

        if (book.getBookType() != null) {
            builder.bookType(BookTypeResponse.fromEntity(book.getBookType()));
        }

        return builder.build();
    }

    // Public version without sensitive information
    public static BookResponse fromEntityPublic(Book book) {
        BookResponseBuilder builder = BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .fileName(book.getFileName()) // Show filename but not full path
                .fileFormat(book.getFileFormat())
                .fileSize(book.getFileSize())
                .isFree(book.getIsFree())
                .price(book.getPrice())
                .downloadable(book.getDownloadable())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .language(book.getLanguage())
                .pageCount(book.getPageCount())
                .rating(book.getRating())
                .downloadCount(book.getDownloadCount())
                .viewCount(book.getViewCount());

        if (book.getBookType() != null) {
            builder.bookType(BookTypeResponse.fromEntity(book.getBookType()));
        }

        return builder.build();
    }
}
