package com.alibou.security.document.application.service;

import com.alibou.security.document.application.dto.*;
import com.alibou.security.document.application.mapper.HierarchyPermissionMapper;
import com.alibou.security.document.domain.entity.*;
import com.alibou.security.document.domain.repository.*;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import com.alibou.security.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing hierarchical permissions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HierarchyPermissionService {
    
    private final EditorHierarchyPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final DocumentFieldRepository documentFieldRepository;
    private final ProductionYearRepository productionYearRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ProductSeriesRepository productSeriesRepository;
    private final ProductRepository productRepository;
    private final HierarchyPermissionMapper permissionMapper;
    
    /**
     * Create new hierarchical permission
     */
    public HierarchyPermissionDto createPermission(CreateHierarchyPermissionRequest request) {
        log.info("Creating hierarchical permission for editor: {}", request.getEditorId());
        
        // Verify admin access
        checkAdminAccess();
        
        // Validate editor
        User editor = userRepository.findById(request.getEditorId())
            .orElseThrow(() -> new IllegalArgumentException("Editor not found: " + request.getEditorId()));
        
        if (editor.getRole() != Role.EDITOR) {
            throw new IllegalArgumentException("User is not an EDITOR: " + editor.getRole());
        }
        
        // Validate hierarchy scope
        validateHierarchyScope(request);
        
        // Check for conflicting permissions
        checkForConflicts(request);
        
        // Build permission entity
        EditorHierarchyPermission permission = buildPermissionEntity(request, editor);
        
        // Save permission
        EditorHierarchyPermission saved = permissionRepository.save(permission);
        
        log.info("Created permission: {} for editor: {}", saved.getPermissionSummary(), editor.getUsername());
        
        return permissionMapper.toDto(saved);
    }
    
    /**
     * Update existing permission
     */
    public HierarchyPermissionDto updatePermission(Integer permissionId, UpdateHierarchyPermissionRequest request) {
        log.info("Updating hierarchical permission: {}", permissionId);
        
        checkAdminAccess();
        
        EditorHierarchyPermission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
        
        // Update flags
        permission.setCanUpload(request.getCanUpload());
        permission.setCanEdit(request.getCanEdit());
        permission.setCanDelete(request.getCanDelete());
        permission.setCanView(request.getCanView());
        permission.setScopeDescription(request.getScopeDescription());
        
        EditorHierarchyPermission updated = permissionRepository.save(permission);
        
        log.info("Updated permission: {} for editor: {}", 
                updated.getPermissionSummary(), updated.getEditor().getUsername());
        
        return permissionMapper.toDto(updated);
    }
    
    /**
     * Delete permission
     */
    public void deletePermission(Integer permissionId) {
        log.info("Deleting hierarchical permission: {}", permissionId);
        
        checkAdminAccess();
        
        EditorHierarchyPermission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
        
        permissionRepository.delete(permission);
        
        log.info("Deleted permission for editor: {}", permission.getEditor().getUsername());
    }
    
    /**
     * Get all permissions for an editor
     */
    @Transactional(readOnly = true)
    public List<HierarchyPermissionDto> getEditorPermissions(Integer editorId) {
        List<EditorHierarchyPermission> permissions = permissionRepository.findByEditorId(editorId);
        return permissionMapper.toDto(permissions);
    }
    
    /**
     * Get all permissions overview (admin)
     */
    @Transactional(readOnly = true)
    public List<HierarchyPermissionDto> getAllPermissions() {
        checkAdminAccess();
        
        List<EditorHierarchyPermission> permissions = permissionRepository.findAllPermissionsWithDetails();
        return permissionMapper.toDto(permissions);
    }
    
    /**
     * Check if editor can upload to product
     */
    @Transactional(readOnly = true)
    public boolean canUploadToProduct(String username, Integer productId) {
        User editor = getUserByUsername(username);
        if (editor.getRole() == Role.ADMIN) {
            return true; // Admin can upload anywhere
        }
        
        if (editor.getRole() != Role.EDITOR) {
            return false; // Only editors and admins can upload
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        return permissionRepository.hasUploadPermissionForProduct(
            editor.getId(),
            product.getId(),
            product.getProductSeries().getId(),
            product.getProductSeries().getManufacturer().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getId()
        );
    }
    
    /**
     * Check if editor can edit documents in product
     */
    @Transactional(readOnly = true)
    public boolean canEditProduct(String username, Integer productId) {
        User editor = getUserByUsername(username);
        if (editor.getRole() == Role.ADMIN) {
            return true;
        }
        
        if (editor.getRole() != Role.EDITOR) {
            return false;
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        return permissionRepository.hasEditPermissionForProduct(
            editor.getId(),
            product.getId(),
            product.getProductSeries().getId(),
            product.getProductSeries().getManufacturer().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getId()
        );
    }
    
    /**
     * Check if editor can delete documents from product
     */
    @Transactional(readOnly = true)
    public boolean canDeleteFromProduct(String username, Integer productId) {
        User editor = getUserByUsername(username);
        if (editor.getRole() == Role.ADMIN) {
            return true;
        }
        
        if (editor.getRole() != Role.EDITOR) {
            return false;
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        return permissionRepository.hasDeletePermissionForProduct(
            editor.getId(),
            product.getId(),
            product.getProductSeries().getId(),
            product.getProductSeries().getManufacturer().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getId(),
            product.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getId()
        );
    }
    
    /**
     * Get products editor can upload to
     */
    @Transactional(readOnly = true)
    public List<Product> getUploadableProducts(String username) {
        User editor = getUserByUsername(username);
        if (editor.getRole() == Role.ADMIN) {
            return productRepository.findAll();
        }
        
        if (editor.getRole() != Role.EDITOR) {
            return List.of();
        }
        
        return permissionRepository.findUploadableProductsByEditor(editor.getId());
    }
    
    /**
     * Get permission tree for editor
     */
    @Transactional(readOnly = true)
    public List<HierarchyPermissionDto> getEditorPermissionTree(String username) {
        User editor = getUserByUsername(username);
        List<EditorHierarchyPermission> permissions = permissionRepository.findByEditorId(editor.getId());
        return permissionMapper.toSummaryDto(permissions);
    }
    
    // ==========================================================
    // PRIVATE HELPER METHODS
    // ==========================================================
    
    private void checkAdminAccess() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can manage hierarchical permissions");
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return getUserByUsername(username);
    }
    
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }
    
    private void validateHierarchyScope(CreateHierarchyPermissionRequest request) {
        // Validate document field
        if (request.getDocumentFieldId() != null) {
            documentFieldRepository.findById(request.getDocumentFieldId())
                .orElseThrow(() -> new IllegalArgumentException("Document field not found: " + request.getDocumentFieldId()));
        }
        
        // Validate production year
        if (request.getProductionYearId() != null) {
            productionYearRepository.findById(request.getProductionYearId())
                .orElseThrow(() -> new IllegalArgumentException("Production year not found: " + request.getProductionYearId()));
        }
        
        // Validate manufacturer
        if (request.getManufacturerId() != null) {
            manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found: " + request.getManufacturerId()));
        }
        
        // Validate product series
        if (request.getProductSeriesId() != null) {
            productSeriesRepository.findById(request.getProductSeriesId())
                .orElseThrow(() -> new IllegalArgumentException("Product series not found: " + request.getProductSeriesId()));
        }
        
        // Validate product
        if (request.getProductId() != null) {
            productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));
        }
    }
    
    private void checkForConflicts(CreateHierarchyPermissionRequest request) {
        List<EditorHierarchyPermission> conflicts = permissionRepository.findConflictingPermissions(
            request.getEditorId(),
            -1, // Exclude ID (for new permissions)
            request.getDocumentFieldId(),
            request.getProductionYearId(),
            request.getManufacturerId(),
            request.getProductSeriesId(),
            request.getProductId()
        );
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Conflicting permission already exists for this scope");
        }
    }
    
    private EditorHierarchyPermission buildPermissionEntity(CreateHierarchyPermissionRequest request, User editor) {
        EditorHierarchyPermission.EditorHierarchyPermissionBuilder builder = EditorHierarchyPermission.builder()
            .editor(editor)
            .canUpload(request.getCanUpload())
            .canEdit(request.getCanEdit())
            .canDelete(request.getCanDelete())
            .canView(request.getCanView())
            .scopeDescription(request.getScopeDescription());
        
        // Set hierarchy scope
        if (request.getDocumentFieldId() != null) {
            DocumentField field = documentFieldRepository.findById(request.getDocumentFieldId()).orElse(null);
            builder.documentField(field);
        }
        
        if (request.getProductionYearId() != null) {
            ProductionYear year = productionYearRepository.findById(request.getProductionYearId()).orElse(null);
            builder.productionYear(year);
        }
        
        if (request.getManufacturerId() != null) {
            Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId()).orElse(null);
            builder.manufacturer(manufacturer);
        }
        
        if (request.getProductSeriesId() != null) {
            ProductSeries series = productSeriesRepository.findById(request.getProductSeriesId()).orElse(null);
            builder.productSeries(series);
        }
        
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId()).orElse(null);
            builder.product(product);
        }
        
        return builder.build();
    }
}
