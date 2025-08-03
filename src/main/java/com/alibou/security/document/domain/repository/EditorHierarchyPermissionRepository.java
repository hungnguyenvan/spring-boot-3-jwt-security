package com.alibou.security.document.domain.repository;

import com.alibou.security.core.domain.repository.BaseRepository;
import com.alibou.security.document.domain.entity.EditorHierarchyPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for EditorHierarchyPermission entity
 */
public interface EditorHierarchyPermissionRepository extends BaseRepository<EditorHierarchyPermission, Integer> {
    
    /**
     * Find all permissions for a specific editor
     */
    @Query("SELECT ehp FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true " +
           "ORDER BY ehp.createdDate DESC")
    List<EditorHierarchyPermission> findByEditorId(@Param("editorId") Integer editorId);
    
    /**
     * Find permissions that cover a specific product for an editor
     */
    @Query("SELECT ehp FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true " +
           "AND (ehp.product.id = :productId " +
           "OR ehp.productSeries.id = :seriesId " +
           "OR ehp.manufacturer.id = :manufacturerId " +
           "OR ehp.productionYear.id = :yearId " +
           "OR ehp.documentField.id = :fieldId " +
           "OR (ehp.product IS NULL AND ehp.productSeries IS NULL AND ehp.manufacturer IS NULL AND ehp.productionYear IS NULL AND ehp.documentField IS NULL)) " +
           "ORDER BY CASE " +
           "WHEN ehp.product IS NOT NULL THEN 1 " +
           "WHEN ehp.productSeries IS NOT NULL THEN 2 " +
           "WHEN ehp.manufacturer IS NOT NULL THEN 3 " +
           "WHEN ehp.productionYear IS NOT NULL THEN 4 " +
           "WHEN ehp.documentField IS NOT NULL THEN 5 " +
           "ELSE 6 END")
    List<EditorHierarchyPermission> findPermissionsForProduct(
        @Param("editorId") Integer editorId,
        @Param("productId") Integer productId,
        @Param("seriesId") Integer seriesId,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("yearId") Integer yearId,
        @Param("fieldId") Integer fieldId
    );
    
    /**
     * Check if editor has upload permission for a specific product
     */
    @Query("SELECT COUNT(ehp) > 0 FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true AND ehp.canUpload = true " +
           "AND (ehp.product.id = :productId " +
           "OR ehp.productSeries.id = :seriesId " +
           "OR ehp.manufacturer.id = :manufacturerId " +
           "OR ehp.productionYear.id = :yearId " +
           "OR ehp.documentField.id = :fieldId " +
           "OR (ehp.product IS NULL AND ehp.productSeries IS NULL AND ehp.manufacturer IS NULL AND ehp.productionYear IS NULL AND ehp.documentField IS NULL))")
    boolean hasUploadPermissionForProduct(
        @Param("editorId") Integer editorId,
        @Param("productId") Integer productId,
        @Param("seriesId") Integer seriesId,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("yearId") Integer yearId,
        @Param("fieldId") Integer fieldId
    );
    
    /**
     * Check if editor has edit permission for a specific product
     */
    @Query("SELECT COUNT(ehp) > 0 FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true AND ehp.canEdit = true " +
           "AND (ehp.product.id = :productId " +
           "OR ehp.productSeries.id = :seriesId " +
           "OR ehp.manufacturer.id = :manufacturerId " +
           "OR ehp.productionYear.id = :yearId " +
           "OR ehp.documentField.id = :fieldId " +
           "OR (ehp.product IS NULL AND ehp.productSeries IS NULL AND ehp.manufacturer IS NULL AND ehp.productionYear IS NULL AND ehp.documentField IS NULL))")
    boolean hasEditPermissionForProduct(
        @Param("editorId") Integer editorId,
        @Param("productId") Integer productId,
        @Param("seriesId") Integer seriesId,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("yearId") Integer yearId,
        @Param("fieldId") Integer fieldId
    );
    
    /**
     * Check if editor has delete permission for a specific product
     */
    @Query("SELECT COUNT(ehp) > 0 FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true AND ehp.canDelete = true " +
           "AND (ehp.product.id = :productId " +
           "OR ehp.productSeries.id = :seriesId " +
           "OR ehp.manufacturer.id = :manufacturerId " +
           "OR ehp.productionYear.id = :yearId " +
           "OR ehp.documentField.id = :fieldId " +
           "OR (ehp.product IS NULL AND ehp.productSeries IS NULL AND ehp.manufacturer IS NULL AND ehp.productionYear IS NULL AND ehp.documentField IS NULL))")
    boolean hasDeletePermissionForProduct(
        @Param("editorId") Integer editorId,
        @Param("productId") Integer productId,
        @Param("seriesId") Integer seriesId,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("yearId") Integer yearId,
        @Param("fieldId") Integer fieldId
    );
    
    /**
     * Find all products editor can upload to
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE EXISTS (" +
           "  SELECT ehp FROM EditorHierarchyPermission ehp " +
           "  WHERE ehp.editor.id = :editorId AND ehp.active = true AND ehp.canUpload = true " +
           "  AND (ehp.product.id = p.id " +
           "  OR ehp.productSeries.id = ps.id " +
           "  OR ehp.manufacturer.id = m.id " +
           "  OR ehp.productionYear.id = py.id " +
           "  OR ehp.documentField.id = df.id " +
           "  OR (ehp.product IS NULL AND ehp.productSeries IS NULL AND ehp.manufacturer IS NULL AND ehp.productionYear IS NULL AND ehp.documentField IS NULL))" +
           ") AND p.active = true " +
           "ORDER BY df.sortOrder, py.year DESC, m.sortOrder, ps.sortOrder, p.sortOrder")
    List<com.alibou.security.document.domain.entity.Product> findUploadableProductsByEditor(@Param("editorId") Integer editorId);
    
    /**
     * Find conflicting permissions (same scope, same editor)
     */
    @Query("SELECT ehp FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.editor.id = :editorId AND ehp.active = true " +
           "AND ehp.id != :excludeId " +
           "AND ((:documentFieldId IS NULL AND ehp.documentField IS NULL) OR ehp.documentField.id = :documentFieldId) " +
           "AND ((:productionYearId IS NULL AND ehp.productionYear IS NULL) OR ehp.productionYear.id = :productionYearId) " +
           "AND ((:manufacturerId IS NULL AND ehp.manufacturer IS NULL) OR ehp.manufacturer.id = :manufacturerId) " +
           "AND ((:productSeriesId IS NULL AND ehp.productSeries IS NULL) OR ehp.productSeries.id = :productSeriesId) " +
           "AND ((:productId IS NULL AND ehp.product IS NULL) OR ehp.product.id = :productId)")
    List<EditorHierarchyPermission> findConflictingPermissions(
        @Param("editorId") Integer editorId,
        @Param("excludeId") Integer excludeId,
        @Param("documentFieldId") Integer documentFieldId,
        @Param("productionYearId") Integer productionYearId,
        @Param("manufacturerId") Integer manufacturerId,
        @Param("productSeriesId") Integer productSeriesId,
        @Param("productId") Integer productId
    );
    
    /**
     * Find all permissions for admin overview
     */
    @Query("SELECT ehp FROM EditorHierarchyPermission ehp " +
           "JOIN FETCH ehp.editor " +
           "LEFT JOIN FETCH ehp.documentField " +
           "LEFT JOIN FETCH ehp.productionYear " +
           "LEFT JOIN FETCH ehp.manufacturer " +
           "LEFT JOIN FETCH ehp.productSeries " +
           "LEFT JOIN FETCH ehp.product " +
           "WHERE ehp.active = true " +
           "ORDER BY ehp.editor.username, ehp.createdDate DESC")
    List<EditorHierarchyPermission> findAllPermissionsWithDetails();
    
    /**
     * Count permissions by editor
     */
    @Query("SELECT ehp.editor.id, COUNT(ehp) FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.active = true " +
           "GROUP BY ehp.editor.id")
    List<Object[]> countPermissionsByEditor();
    
    /**
     * Find permissions by hierarchy level
     */
    @Query("SELECT ehp FROM EditorHierarchyPermission ehp " +
           "WHERE ehp.active = true " +
           "AND CASE " +
           "WHEN :level = 'PRODUCT' THEN ehp.product IS NOT NULL " +
           "WHEN :level = 'PRODUCT_SERIES' THEN ehp.productSeries IS NOT NULL AND ehp.product IS NULL " +
           "WHEN :level = 'MANUFACTURER' THEN ehp.manufacturer IS NOT NULL AND ehp.productSeries IS NULL " +
           "WHEN :level = 'PRODUCTION_YEAR' THEN ehp.productionYear IS NOT NULL AND ehp.manufacturer IS NULL " +
           "WHEN :level = 'DOCUMENT_FIELD' THEN ehp.documentField IS NOT NULL AND ehp.productionYear IS NULL " +
           "WHEN :level = 'GLOBAL' THEN ehp.documentField IS NULL " +
           "ELSE false END = true " +
           "ORDER BY ehp.createdDate DESC")
    List<EditorHierarchyPermission> findByPermissionLevel(@Param("level") String level);
}
