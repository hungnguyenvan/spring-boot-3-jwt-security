package com.alibou.security.document.presentation.controller;

import com.alibou.security.document.application.dto.HierarchyPermissionDto;
import com.alibou.security.document.application.service.HierarchyPermissionService;
import com.alibou.security.document.domain.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Editor controller for viewing own permissions
 */
@RestController
@RequestMapping("/api/v1/editor/my-permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Editor Hierarchy Permissions", description = "Editor APIs for viewing own hierarchical permissions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('EDITOR')")
public class EditorHierarchyPermissionController {
    
    private final HierarchyPermissionService permissionService;
    
    /**
     * Get my permission tree
     */
    @GetMapping
    @Operation(
        summary = "Get my permissions",
        description = "Editor views their own hierarchical permissions"
    )
    public ResponseEntity<List<HierarchyPermissionDto>> getMyPermissions(Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Editor {} retrieving own permissions", username);
        
        List<HierarchyPermissionDto> permissions = permissionService.getEditorPermissionTree(username);
        
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get products I can upload to
     */
    @GetMapping("/uploadable-products")
    @Operation(
        summary = "Get uploadable products",
        description = "Editor views products they can upload documents to"
    )
    public ResponseEntity<List<Product>> getUploadableProducts(Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Editor {} retrieving uploadable products", username);
        
        List<Product> products = permissionService.getUploadableProducts(username);
        
        return ResponseEntity.ok(products);
    }
    
    /**
     * Check if I can upload to specific product
     */
    @GetMapping("/check/upload/{productId}")
    @Operation(
        summary = "Check upload permission",
        description = "Editor checks if they can upload to specific product"
    )
    public ResponseEntity<Boolean> canUploadToProduct(
            @PathVariable Integer productId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Editor {} checking upload permission for product: {}", username, productId);
        
        boolean canUpload = permissionService.canUploadToProduct(username, productId);
        
        return ResponseEntity.ok(canUpload);
    }
    
    /**
     * Check if I can edit documents in specific product
     */
    @GetMapping("/check/edit/{productId}")
    @Operation(
        summary = "Check edit permission",
        description = "Editor checks if they can edit documents in specific product"
    )
    public ResponseEntity<Boolean> canEditProduct(
            @PathVariable Integer productId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Editor {} checking edit permission for product: {}", username, productId);
        
        boolean canEdit = permissionService.canEditProduct(username, productId);
        
        return ResponseEntity.ok(canEdit);
    }
    
    /**
     * Check if I can delete documents from specific product
     */
    @GetMapping("/check/delete/{productId}")
    @Operation(
        summary = "Check delete permission",
        description = "Editor checks if they can delete documents from specific product"
    )
    public ResponseEntity<Boolean> canDeleteFromProduct(
            @PathVariable Integer productId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Editor {} checking delete permission for product: {}", username, productId);
        
        boolean canDelete = permissionService.canDeleteFromProduct(username, productId);
        
        return ResponseEntity.ok(canDelete);
    }
}
