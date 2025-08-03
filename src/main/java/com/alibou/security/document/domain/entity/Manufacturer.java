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
 * Manufacturer entity (e.g., Toyota, Kia, Honda)
 * Third level in the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "manufacturer")
public class Manufacturer extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name; // Toyota, Kia, Honda

    @Column(length = 100)
    private String code; // TOYOTA, KIA, HONDA

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String logoPath; // Path to manufacturer logo

    @Column(length = 100)
    private String country; // Japan, Korea, etc.

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_year_id", nullable = false)
    private ProductionYear productionYear;

    // Child relationship
    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductSeries> productSeries = new ArrayList<>();

    // Business methods
    public void addProductSeries(ProductSeries series) {
        series.setManufacturer(this);
        this.productSeries.add(series);
    }

    public void removeProductSeries(ProductSeries series) {
        series.setManufacturer(null);
        this.productSeries.remove(series);
    }

    public long getTotalDocuments() {
        return productSeries.stream()
                .mapToLong(ProductSeries::getTotalDocuments)
                .sum();
    }

    public boolean hasProductSeries(String seriesName) {
        return productSeries.stream()
                .anyMatch(ps -> ps.getName().equalsIgnoreCase(seriesName));
    }

    // Helper method to get full path in hierarchy
    public String getHierarchyPath() {
        return productionYear.getDocumentField().getName() + " / " + 
               productionYear.getYear() + " / " + 
               this.name;
    }
}
