package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating hierarchical permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHierarchyPermissionRequest {
    
    // Permission flags
    @NotNull(message = "Upload permission flag is required")
    private Boolean canUpload;
    
    @NotNull(message = "Edit permission flag is required")
    private Boolean canEdit;
    
    @NotNull(message = "Delete permission flag is required")
    private Boolean canDelete;
    
    @NotNull(message = "View permission flag is required")
    private Boolean canView;
    
    private String scopeDescription;
}
