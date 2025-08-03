package com.alibou.security.document.presentation.controller;

import com.alibou.security.document.application.dto.*;
import com.alibou.security.document.application.service.HierarchyPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing hierarchical permissions
 */
@RestController
@RequestMapping("/api/v1/admin/hierarchy-permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Hierarchy Permissions", description = "Admin APIs for managing editor permissions in document hierarchy")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHierarchyPermissionController {
    
    private final HierarchyPermissionService permissionService;
    
    /**
     * Create new hierarchical permission for editor
     */
    @PostMapping
    @Operation(
        summary = "Create hierarchical permission",
        description = "Admin creates permission for editor at specific hierarchy level. " +
                     "Null values mean permission applies to ALL items at that level."
    )
    public ResponseEntity<HierarchyPermissionDto> createPermission(
            @Valid @RequestBody CreateHierarchyPermissionRequest request) {
        
        log.info("Admin creating permission for editor: {}", request.getEditorId());
        
        HierarchyPermissionDto permission = permissionService.createPermission(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }
    
    /**
     * Update existing permission
     */
    @PutMapping("/{permissionId}")
    @Operation(
        summary = "Update hierarchical permission",
        description = "Admin updates existing permission settings"
    )
    public ResponseEntity<HierarchyPermissionDto> updatePermission(
            @PathVariable Integer permissionId,
            @Valid @RequestBody UpdateHierarchyPermissionRequest request) {
        
        log.info("Admin updating permission: {}", permissionId);
        
        HierarchyPermissionDto permission = permissionService.updatePermission(permissionId, request);
        
        return ResponseEntity.ok(permission);
    }
    
    /**
     * Delete permission
     */
    @DeleteMapping("/{permissionId}")
    @Operation(
        summary = "Delete hierarchical permission",
        description = "Admin removes permission from editor"
    )
    public ResponseEntity<Void> deletePermission(@PathVariable Integer permissionId) {
        
        log.info("Admin deleting permission: {}", permissionId);
        
        permissionService.deletePermission(permissionId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all permissions overview
     */
    @GetMapping
    @Operation(
        summary = "Get all permissions",
        description = "Admin views all hierarchical permissions in system"
    )
    public ResponseEntity<List<HierarchyPermissionDto>> getAllPermissions() {
        
        log.info("Admin retrieving all permissions");
        
        List<HierarchyPermissionDto> permissions = permissionService.getAllPermissions();
        
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permissions for specific editor
     */
    @GetMapping("/editor/{editorId}")
    @Operation(
        summary = "Get editor permissions",
        description = "Admin views all permissions assigned to specific editor"
    )
    public ResponseEntity<List<HierarchyPermissionDto>> getEditorPermissions(
            @PathVariable Integer editorId) {
        
        log.info("Admin retrieving permissions for editor: {}", editorId);
        
        List<HierarchyPermissionDto> permissions = permissionService.getEditorPermissions(editorId);
        
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Check upload permission for product
     */
    @GetMapping("/check/upload/{editorId}/{productId}")
    @Operation(
        summary = "Check upload permission",
        description = "Admin checks if editor can upload to specific product"
    )
    public ResponseEntity<PermissionCheckResponse> checkUploadPermission(
            @PathVariable Integer editorId,
            @PathVariable Integer productId) {
        
        log.info("Admin checking upload permission - Editor: {}, Product: {}", editorId, productId);
        
        // Get editor username for permission check
        // Note: This could be optimized by adding a method that takes editorId directly
        boolean canUpload = false; // Would need to implement getEditorUsername method
        
        PermissionCheckResponse response = PermissionCheckResponse.builder()
            .hasPermission(canUpload)
            .permissionType("UPLOAD")
            .editorId(editorId)
            .productId(productId)
            .message(canUpload ? "Editor has upload permission" : "Editor lacks upload permission")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get permission statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get permission statistics",
        description = "Admin views system-wide permission statistics"
    )
    public ResponseEntity<PermissionStatistics> getPermissionStatistics() {
        
        log.info("Admin retrieving permission statistics");
        
        List<HierarchyPermissionDto> allPermissions = permissionService.getAllPermissions();
        
        PermissionStatistics stats = PermissionStatistics.builder()
            .totalPermissions(allPermissions.size())
            .uploadPermissions((int) allPermissions.stream().filter(p -> p.getCanUpload()).count())
            .editPermissions((int) allPermissions.stream().filter(p -> p.getCanEdit()).count())
            .deletePermissions((int) allPermissions.stream().filter(p -> p.getCanDelete()).count())
            .viewPermissions((int) allPermissions.stream().filter(p -> p.getCanView()).count())
            .editorsWithPermissions((int) allPermissions.stream()
                .map(p -> p.getEditorId())
                .distinct()
                .count())
            .build();
        
        return ResponseEntity.ok(stats);
    }
}
