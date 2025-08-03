package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for hierarchical permission response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyPermissionDto {
    
    private Integer id;
    
    // Editor information
    private Integer editorId;
    private String editorUsername;
    private String editorEmail;
    
    // Hierarchy scope
    private Integer documentFieldId;
    private String documentFieldName;
    
    private Integer productionYearId;
    private Integer productionYear;
    
    private Integer manufacturerId;
    private String manufacturerName;
    
    private Integer productSeriesId;
    private String productSeriesName;
    
    private Integer productId;
    private String productName;
    
    // Permission flags
    private Boolean canUpload;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canView;
    
    // Meta information
    private String permissionLevel;
    private String hierarchyPath;
    private String scopeDescription;
    private String permissionSummary;
    
    // Audit fields
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Integer createdBy;
    private Integer lastModifiedBy;
    private Boolean active;
}
