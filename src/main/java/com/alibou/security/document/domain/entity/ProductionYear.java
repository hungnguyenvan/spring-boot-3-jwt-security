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
 * Production Year entity (e.g., 2008, 2009)
 * Second level in the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "production_year")
public class ProductionYear extends BaseEntity {

    @Column(nullable = false)
    private Integer year; // 2008, 2009, etc.

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_field_id", nullable = false)
    private DocumentField documentField;

    // Child relationship
    @OneToMany(mappedBy = "productionYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Manufacturer> manufacturers = new ArrayList<>();

    // Business methods
    public void addManufacturer(Manufacturer manufacturer) {
        manufacturer.setProductionYear(this);
        this.manufacturers.add(manufacturer);
    }

    public void removeManufacturer(Manufacturer manufacturer) {
        manufacturer.setProductionYear(null);
        this.manufacturers.remove(manufacturer);
    }

    public long getTotalDocuments() {
        return manufacturers.stream()
                .mapToLong(Manufacturer::getTotalDocuments)
                .sum();
    }

    public boolean hasManufacturer(String manufacturerName) {
        return manufacturers.stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(manufacturerName));
    }
}
