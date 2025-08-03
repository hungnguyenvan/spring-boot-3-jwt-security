package com.alibou.security.document.domain.entity;

import com.alibou.security.core.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Technical Document entity (e.g., Engine Schematic, Window Electric Schematic)
 * Final level in the document hierarchy - contains actual documents
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "technical_document")
public class TechnicalDocument extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title; // Engine Schematic, Window Electric Schematic

    @Column(nullable = false, length = 100)
    private String documentType; // ENGINE_SCHEMATIC, WINDOW_ELECTRIC_SCHEMATIC

    @Column(length = 1000)
    private String description;

    // File information
    @Column(length = 500)
    private String filePath; // Path to the actual document file

    @Column(length = 255)
    private String fileName; // Original filename

    @Column(length = 50)
    private String fileFormat; // PDF, DWG, PNG, etc.

    @Column
    private Long fileSize; // File size in bytes

    @Column(length = 100)
    private String checksum; // File integrity check

    // Document metadata
    @Column(length = 100)
    private String version; // Document version (v1.0, v2.1, etc.)

    @Column(length = 100)
    private String language; // EN, VI, JP, etc.

    @Column
    private Integer pageCount; // Number of pages

    // Document categorization
    @Column(length = 100)
    private String category; // ELECTRICAL, MECHANICAL, HYDRAULIC

    @Column(length = 100)
    private String subCategory; // WIRING, ENGINE, TRANSMISSION

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublic = true; // Public or restricted access

    @Builder.Default
    @Column(nullable = false)
    private Boolean downloadable = true; // Can be downloaded

    // Document statistics
    @Builder.Default
    @Column
    private Integer downloadCount = 0;

    @Builder.Default
    @Column
    private Integer viewCount = 0;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Business methods
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void updateRating(BigDecimal newRating) {
        if (newRating != null && 
            newRating.compareTo(BigDecimal.ZERO) >= 0 && 
            newRating.compareTo(BigDecimal.valueOf(5)) <= 0) {
            this.rating = newRating;
        }
    }

    // Helper method to get full path in hierarchy
    public String getHierarchyPath() {
        return product.getHierarchyPath() + " / " + this.title;
    }

    // Get full document path with type
    public String getFullDocumentPath() {
        return getHierarchyPath() + " (" + this.documentType + ")";
    }

    // Check if document is accessible
    public boolean isAccessible() {
        return Boolean.TRUE.equals(this.getActive()) && Boolean.TRUE.equals(this.isPublic);
    }

    // Check if document can be downloaded
    public boolean canBeDownloaded() {
        return isAccessible() && Boolean.TRUE.equals(this.downloadable);
    }

    // Get file extension
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return fileFormat != null ? fileFormat.toLowerCase() : "";
    }

    // Get formatted file size
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "Unknown";
        }
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
