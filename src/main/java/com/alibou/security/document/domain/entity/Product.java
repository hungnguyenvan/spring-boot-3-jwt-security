package com.alibou.security.document.domain.entity;

import com.alibou.security.core.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Product entity (e.g., Mazda2, Kia Morning)
 * Fifth level in the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name; // Mazda2, Kia Morning, Camry 2.5

    @Column(length = 100)
    private String code; // MAZDA2, KIA_MORNING, CAMRY_25

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String imagePath; // Path to product image

    @Column(length = 100)
    private String modelCode; // Internal model code

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_series_id", nullable = false)
    private ProductSeries productSeries;

    // Child relationship - Final level contains documents
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TechnicalDocument> technicalDocuments = new ArrayList<>();

    // Business methods
    public void addTechnicalDocument(TechnicalDocument document) {
        document.setProduct(this);
        this.technicalDocuments.add(document);
    }

    public void removeTechnicalDocument(TechnicalDocument document) {
        document.setProduct(null);
        this.technicalDocuments.remove(document);
    }

    public long getTotalDocuments() {
        return technicalDocuments.size();
    }

    public boolean hasDocumentType(String documentType) {
        return technicalDocuments.stream()
                .anyMatch(doc -> doc.getDocumentType().equalsIgnoreCase(documentType));
    }

    // Helper method to get full path in hierarchy
    public String getHierarchyPath() {
        return productSeries.getHierarchyPath() + " / " + this.name;
    }

    // Get documents by type
    public List<TechnicalDocument> getDocumentsByType(String documentType) {
        return technicalDocuments.stream()
                .filter(doc -> doc.getDocumentType().equalsIgnoreCase(documentType))
                .toList();
    }

    // Get available document types
    public List<String> getAvailableDocumentTypes() {
        return technicalDocuments.stream()
                .map(TechnicalDocument::getDocumentType)
                .distinct()
                .sorted()
                .toList();
    }
}
