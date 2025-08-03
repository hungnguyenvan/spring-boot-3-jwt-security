package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for permission check operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckResponse {
    
    private Boolean hasPermission;
    private String permissionType;
    private Integer editorId;
    private Integer productId;
    private String message;
    private String editorUsername;
    private String productName;
    private String hierarchyPath;
}
