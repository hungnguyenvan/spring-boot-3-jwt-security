package com.alibou.security.document.domain.entity;

import com.alibou.security.core.domain.entity.BaseEntity;
import com.alibou.security.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity for managing hierarchical permissions for editors
 * Allows fine-grained permission control across the document hierarchy
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "editor_hierarchy_permission")
public class EditorHierarchyPermission extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private User editor;
    
    // Hierarchical scope - null means "all" at that level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_field_id")
    private DocumentField documentField;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_year_id")
    private ProductionYear productionYear;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_series_id")
    private ProductSeries productSeries;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    // Permission flags
    @Builder.Default
    @Column(nullable = false)
    private Boolean canUpload = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean canEdit = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean canDelete = false;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean canView = true;
    
    // Permission scope description for admin UI
    @Column(length = 500)
    private String scopeDescription;
    
    /**
     * Get the most specific level of this permission
     */
    public String getPermissionLevel() {
        if (product != null) return "PRODUCT";
        if (productSeries != null) return "PRODUCT_SERIES";
        if (manufacturer != null) return "MANUFACTURER";
        if (productionYear != null) return "PRODUCTION_YEAR";
        if (documentField != null) return "DOCUMENT_FIELD";
        return "GLOBAL";
    }
    
    /**
     * Get hierarchy path for display
     */
    public String getHierarchyPath() {
        StringBuilder path = new StringBuilder();
        
        if (documentField != null) {
            path.append(documentField.getName());
            
            if (productionYear != null) {
                path.append(" / ").append(productionYear.getYear());
                
                if (manufacturer != null) {
                    path.append(" / ").append(manufacturer.getName());
                    
                    if (productSeries != null) {
                        path.append(" / ").append(productSeries.getName());
                        
                        if (product != null) {
                            path.append(" / ").append(product.getName());
                        }
                    }
                }
            }
        }
        
        return path.length() > 0 ? path.toString() : "All Documents";
    }
    
    /**
     * Check if this permission covers a specific product
     */
    public boolean coversProduct(Product targetProduct) {
        if (targetProduct == null) return false;
        
        // If product is specified, must match exactly
        if (product != null) {
            return product.getId().equals(targetProduct.getId());
        }
        
        // If series is specified, target product must be in that series
        if (productSeries != null) {
            return productSeries.getId().equals(targetProduct.getProductSeries().getId());
        }
        
        // If manufacturer is specified, target product's manufacturer must match
        if (manufacturer != null) {
            return manufacturer.getId().equals(targetProduct.getProductSeries().getManufacturer().getId());
        }
        
        // If year is specified, target product's year must match
        if (productionYear != null) {
            return productionYear.getId().equals(targetProduct.getProductSeries().getManufacturer().getProductionYear().getId());
        }
        
        // If field is specified, target product's field must match
        if (documentField != null) {
            return documentField.getId().equals(targetProduct.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getId());
        }
        
        // Global permission covers everything
        return true;
    }
    
    /**
     * Get permission summary for audit logs
     */
    public String getPermissionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Level: ").append(getPermissionLevel())
               .append(", Scope: ").append(getHierarchyPath())
               .append(", Permissions: ");
        
        if (canUpload) summary.append("UPLOAD ");
        if (canEdit) summary.append("EDIT ");
        if (canDelete) summary.append("DELETE ");
        if (canView) summary.append("VIEW ");
        
        return summary.toString().trim();
    }
}
