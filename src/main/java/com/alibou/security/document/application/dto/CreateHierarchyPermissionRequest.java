package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating hierarchical permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHierarchyPermissionRequest {
    
    @NotNull(message = "Editor ID is required")
    private Integer editorId;
    
    // Hierarchical scope - null means "all" at that level
    private Integer documentFieldId;
    private Integer productionYearId;
    private Integer manufacturerId;
    private Integer productSeriesId;
    private Integer productId;
    
    // Permission flags
    @Builder.Default
    private Boolean canUpload = true;
    
    @Builder.Default
    private Boolean canEdit = true;
    
    @Builder.Default
    private Boolean canDelete = false;
    
    @Builder.Default
    private Boolean canView = true;
    
    private String scopeDescription;
}
