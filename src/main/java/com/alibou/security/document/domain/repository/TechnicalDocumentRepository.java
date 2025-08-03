package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.TechnicalDocument;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for TechnicalDocument entity
 */
public interface TechnicalDocumentRepository extends BaseRepository<TechnicalDocument, Integer> {
    
    // Find by product
    List<TechnicalDocument> findByProductIdAndActiveTrueOrderBySortOrder(Integer productId);
    Page<TechnicalDocument> findByProductIdAndActiveTrue(Integer productId, Pageable pageable);
    
    // Find by document type
    List<TechnicalDocument> findByDocumentTypeAndActiveTrueOrderBySortOrder(String documentType);
    Page<TechnicalDocument> findByDocumentTypeAndActiveTrue(String documentType, Pageable pageable);
    
    // Find by category
    List<TechnicalDocument> findByCategoryAndActiveTrueOrderBySortOrder(String category);
    Page<TechnicalDocument> findByCategoryAndActiveTrue(String category, Pageable pageable);
    
    // Complex hierarchy search
    @Query("SELECT td FROM TechnicalDocument td " +
           "JOIN td.product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE td.active = true " +
           "AND (:fieldName IS NULL OR LOWER(df.name) = LOWER(:fieldName)) " +
           "AND (:year IS NULL OR py.year = :year) " +
           "AND (:manufacturerName IS NULL OR LOWER(m.name) = LOWER(:manufacturerName)) " +
           "AND (:seriesName IS NULL OR LOWER(ps.name) = LOWER(:seriesName)) " +
           "AND (:productName IS NULL OR LOWER(p.name) = LOWER(:productName)) " +
           "AND (:documentType IS NULL OR LOWER(td.documentType) = LOWER(:documentType)) " +
           "ORDER BY df.sortOrder, py.year DESC, m.sortOrder, ps.sortOrder, p.sortOrder, td.sortOrder")
    Page<TechnicalDocument> findByHierarchy(
        @Param("fieldName") String fieldName,
        @Param("year") Integer year,
        @Param("manufacturerName") String manufacturerName,
        @Param("seriesName") String seriesName,
        @Param("productName") String productName,
        @Param("documentType") String documentType,
        Pageable pageable
    );
    
    // Full text search across documents
    @Query("SELECT td FROM TechnicalDocument td " +
           "JOIN td.product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE td.active = true " +
           "AND td.isPublic = true " +
           "AND (LOWER(td.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(td.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(td.documentType) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(df.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(ps.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY td.downloadCount DESC, td.rating DESC")
    Page<TechnicalDocument> searchDocuments(@Param("query") String query, Pageable pageable);
    
    // Get popular documents
    @Query("SELECT td FROM TechnicalDocument td " +
           "WHERE td.active = true AND td.isPublic = true " +
           "ORDER BY td.downloadCount DESC, td.viewCount DESC")
    List<TechnicalDocument> findMostPopular(Pageable pageable);
    
    // Get highly rated documents
    @Query("SELECT td FROM TechnicalDocument td " +
           "WHERE td.active = true AND td.isPublic = true " +
           "AND td.rating > 0 " +
           "ORDER BY td.rating DESC, td.downloadCount DESC")
    List<TechnicalDocument> findHighestRated(Pageable pageable);
    
    // Get recent documents
    @Query("SELECT td FROM TechnicalDocument td " +
           "WHERE td.active = true AND td.isPublic = true " +
           "ORDER BY td.createdDate DESC")
    List<TechnicalDocument> findRecent(Pageable pageable);
    
    // Get document types for a specific product
    @Query("SELECT DISTINCT td.documentType FROM TechnicalDocument td " +
           "WHERE td.product.id = :productId AND td.active = true " +
           "ORDER BY td.documentType")
    List<String> findDocumentTypesByProduct(@Param("productId") Integer productId);
    
    // Get categories for a specific field
    @Query("SELECT DISTINCT td.category FROM TechnicalDocument td " +
           "JOIN td.product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE df.id = :fieldId AND td.active = true " +
           "ORDER BY td.category")
    List<String> findCategoriesByField(@Param("fieldId") Integer fieldId);
    
    // Statistics queries
    @Query("SELECT COUNT(td) FROM TechnicalDocument td WHERE td.active = true")
    long countAllActive();
    
    @Query("SELECT COUNT(td) FROM TechnicalDocument td WHERE td.active = true AND td.isPublic = true")
    long countPublic();
    
    @Query("SELECT SUM(td.downloadCount) FROM TechnicalDocument td WHERE td.active = true")
    Long getTotalDownloads();
    
    @Query("SELECT SUM(td.viewCount) FROM TechnicalDocument td WHERE td.active = true")
    Long getTotalViews();
    
    @Query("SELECT AVG(td.rating) FROM TechnicalDocument td WHERE td.active = true AND td.rating > 0")
    Double getAverageRating();
    
    @Query("SELECT SUM(td.fileSize) FROM TechnicalDocument td WHERE td.active = true")
    Long getTotalFileSize();
}
