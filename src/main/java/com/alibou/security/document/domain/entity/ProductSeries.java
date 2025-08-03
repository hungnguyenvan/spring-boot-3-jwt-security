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
 * Product Series entity (e.g., Mazda, Morning)
 * Fourth level in the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_series")
public class ProductSeries extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name; // Mazda, Morning, Camry

    @Column(length = 100)
    private String code; // MAZDA, MORNING, CAMRY

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String imagePath; // Path to series image

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    // Child relationship
    @OneToMany(mappedBy = "productSeries", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // Business methods
    public void addProduct(Product product) {
        product.setProductSeries(this);
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        product.setProductSeries(null);
        this.products.remove(product);
    }

    public long getTotalDocuments() {
        return products.stream()
                .mapToLong(Product::getTotalDocuments)
                .sum();
    }

    public boolean hasProduct(String productName) {
        return products.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(productName));
    }

    // Helper method to get full path in hierarchy
    public String getHierarchyPath() {
        return manufacturer.getHierarchyPath() + " / " + this.name;
    }
}
