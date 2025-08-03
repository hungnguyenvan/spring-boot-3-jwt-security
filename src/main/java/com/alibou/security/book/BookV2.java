package com.alibou.security.book;

import com.alibou.security.booktype.BookType;
import com.alibou.security.core.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BookV2 extends BaseEntity {

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

    @Column(length = 255)
    private String fileName; // Tên file gốc

    @Column(length = 50)
    private String fileFormat; // Định dạng file (PDF, EPUB, etc.)

    @Column
    private Long fileSize; // Kích thước file (bytes)

    // Pricing and access control
    @Builder.Default
    @Column(nullable = false)
    private Boolean isFree = true; // Tài liệu free hay trả phí

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO; // Giá tiền nếu trả phí

    @Builder.Default
    @Column(nullable = false)
    private Boolean downloadable = true; // Có cho phép tải hay không

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

    // Helper methods for business logic
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void updateRating(BigDecimal newRating) {
        if (newRating != null && newRating.compareTo(BigDecimal.ZERO) >= 0 && newRating.compareTo(BigDecimal.valueOf(5)) <= 0) {
            this.rating = newRating;
        }
    }

    public boolean isPaid() {
        return !Boolean.TRUE.equals(this.isFree);
    }

    public boolean isDownloadAllowed() {
        return Boolean.TRUE.equals(this.downloadable) && Boolean.TRUE.equals(this.getActive());
    }
}
