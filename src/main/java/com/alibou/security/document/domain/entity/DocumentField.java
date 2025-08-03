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
 * Document Category/Field entity (e.g., Auto, Electrical Bike)
 * Top level in the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_field")
public class DocumentField extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name; // Auto, Electrical Bike, etc.

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String code; // AUTO, EBIKE

    @Column(length = 7)
    private String colorCode; // #FF5733 for UI display

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Hierarchical relationship
    @OneToMany(mappedBy = "documentField", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionYear> productionYears = new ArrayList<>();

    // Business methods
    public void addProductionYear(ProductionYear year) {
        year.setDocumentField(this);
        this.productionYears.add(year);
    }

    public void removeProductionYear(ProductionYear year) {
        year.setDocumentField(null);
        this.productionYears.remove(year);
    }

    // Helper methods
    public boolean hasYear(Integer year) {
        return productionYears.stream()
                .anyMatch(py -> py.getYear().equals(year));
    }

    public long getTotalDocuments() {
        return productionYears.stream()
                .mapToLong(ProductionYear::getTotalDocuments)
                .sum();
    }
}
