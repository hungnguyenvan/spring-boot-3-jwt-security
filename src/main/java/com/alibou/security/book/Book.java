package com.alibou.security.book;

import com.alibou.security.booktype.BookType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(generator = "book_id_seq")
    @SequenceGenerator(name = "book_id_seq", sequenceName = "book_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 20)
    private String isbn;

    @Column(length = 1000)
    private String description;

    // Relationship with BookType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_type_id")
    private BookType bookType;

    // File management fields
    @Column(length = 500)
    private String filePath; // Đường dẫn lưu trữ file

    @Column(length = 100)
    private String fileName; // Tên file gốc

    @Column(length = 10)
    private String fileFormat; // PDF, EPUB, DOCX, etc.

    @Column
    private Long fileSize; // Kích thước file (bytes)

    // Pricing and access control
    @Builder.Default
    @Column(nullable = false)
    private Boolean isFree = true; // Free hay phải trả phí

    @Column(precision = 10, scale = 2)
    private BigDecimal price; // Giá bán (nếu không free)

    @Builder.Default
    @Column(nullable = false)
    private Boolean downloadable = true; // Có cho phép tải hay không

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true; // Trạng thái hoạt động

    // Publication info
    @Column(length = 255)
    private String publisher;

    @Column
    private Integer publicationYear;

    @Column(length = 50)
    private String language;

    @Column
    private Integer pageCount;

    // Rating and statistics
    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @Column
    private Integer downloadCount = 0;

    @Builder.Default
    @Column
    private Integer viewCount = 0;

    @CreatedDate
    @Column(
            name = "created_date",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(
            name = "last_modified_date",
            insertable = false
    )
    private LocalDateTime lastModified;

    @CreatedBy
    @Column(
            name = "created_by",
            nullable = false,
            updatable = false
    )
    private Integer createdBy;

    @LastModifiedBy
    @Column(
            name = "last_modified_by", 
            insertable = false
    )
    private Integer lastModifiedBy;
}
