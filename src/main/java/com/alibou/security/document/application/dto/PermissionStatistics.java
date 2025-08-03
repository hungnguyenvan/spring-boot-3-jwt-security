package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics for hierarchical permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStatistics {
    
    private Integer totalPermissions;
    private Integer uploadPermissions;
    private Integer editPermissions;
    private Integer deletePermissions;
    private Integer viewPermissions;
    private Integer editorsWithPermissions;
    private Integer documentFieldPermissions;
    private Integer productionYearPermissions;
    private Integer manufacturerPermissions;
    private Integer productSeriesPermissions;
    private Integer productPermissions;
}
