package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.Product;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity
 */
public interface ProductRepository extends BaseRepository<Product, Integer> {
    
    // Find by product series
    List<Product> findByProductSeriesIdAndActiveTrueOrderBySortOrder(Integer seriesId);
    Page<Product> findByProductSeriesIdAndActiveTrue(Integer seriesId, Pageable pageable);
    
    // Find by name
    Optional<Product> findByNameAndActiveTrueAndProductSeriesId(String name, Integer seriesId);
    
    // Search by name pattern
    @Query("SELECT p FROM Product p " +
           "WHERE p.active = true " +
           "AND (:seriesId IS NULL OR p.productSeries.id = :seriesId) " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "ORDER BY p.sortOrder")
    List<Product> findByNamePattern(@Param("namePattern") String namePattern, 
                                   @Param("seriesId") Integer seriesId);
    
    // Complex hierarchy search
    @Query("SELECT p FROM Product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE p.active = true " +
           "AND (:fieldName IS NULL OR LOWER(df.name) = LOWER(:fieldName)) " +
           "AND (:year IS NULL OR py.year = :year) " +
           "AND (:manufacturerName IS NULL OR LOWER(m.name) = LOWER(:manufacturerName)) " +
           "AND (:seriesName IS NULL OR LOWER(ps.name) = LOWER(:seriesName)) " +
           "AND (:productName IS NULL OR LOWER(p.name) = LOWER(:productName)) " +
           "ORDER BY df.sortOrder, py.year DESC, m.sortOrder, ps.sortOrder, p.sortOrder")
    Page<Product> findByHierarchy(
        @Param("fieldName") String fieldName,
        @Param("year") Integer year,
        @Param("manufacturerName") String manufacturerName,
        @Param("seriesName") String seriesName,
        @Param("productName") String productName,
        Pageable pageable
    );
    
    // Find products with documents
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.technicalDocuments td " +
           "WHERE p.active = true AND td.active = true " +
           "AND (:seriesId IS NULL OR p.productSeries.id = :seriesId) " +
           "ORDER BY p.sortOrder")
    List<Product> findProductsWithDocuments(@Param("seriesId") Integer seriesId);
    
    // Find products by document type
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.technicalDocuments td " +
           "WHERE p.active = true AND td.active = true " +
           "AND LOWER(td.documentType) = LOWER(:documentType) " +
           "ORDER BY p.sortOrder")
    List<Product> findByDocumentType(@Param("documentType") String documentType);
    
    // Count documents by product
    @Query("SELECT p.id, COUNT(td) FROM Product p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE p.active = true " +
           "AND (td IS NULL OR td.active = true) " +
           "GROUP BY p.id")
    List<Object[]> countDocumentsByProduct();
    
    // Statistics queries
    @Query("SELECT COUNT(p) FROM Product p " +
           "JOIN p.productSeries ps " +
           "WHERE p.active = true AND ps.id = :seriesId")
    long countByProductSeries(@Param("seriesId") Integer seriesId);
    
    @Query("SELECT COUNT(p) FROM Product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "WHERE p.active = true AND m.id = :manufacturerId")
    long countByManufacturer(@Param("manufacturerId") Integer manufacturerId);
    
    @Query("SELECT COUNT(p) FROM Product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "WHERE p.active = true AND py.id = :yearId")
    long countByYear(@Param("yearId") Integer yearId);
    
    @Query("SELECT COUNT(p) FROM Product p " +
           "JOIN p.productSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE p.active = true AND df.id = :fieldId")
    long countByField(@Param("fieldId") Integer fieldId);
    
    // Get products with most documents
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE p.active = true " +
           "GROUP BY p " +
           "ORDER BY COUNT(td) DESC")
    List<Product> findProductsWithMostDocuments(Pageable pageable);
}
