package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.ProductSeries;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductSeries entity
 */
public interface ProductSeriesRepository extends BaseRepository<ProductSeries, Integer> {
    
    // Find by manufacturer
    List<ProductSeries> findByManufacturerIdAndActiveTrueOrderBySortOrder(Integer manufacturerId);
    Page<ProductSeries> findByManufacturerIdAndActiveTrue(Integer manufacturerId, Pageable pageable);
    
    // Find by name
    Optional<ProductSeries> findByNameAndActiveTrueAndManufacturerId(String name, Integer manufacturerId);
    
    // Search by name pattern
    @Query("SELECT ps FROM ProductSeries ps " +
           "WHERE ps.active = true " +
           "AND (:manufacturerId IS NULL OR ps.manufacturer.id = :manufacturerId) " +
           "AND LOWER(ps.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "ORDER BY ps.sortOrder")
    List<ProductSeries> findByNamePattern(@Param("namePattern") String namePattern, 
                                         @Param("manufacturerId") Integer manufacturerId);
    
    // Complex hierarchy search
    @Query("SELECT ps FROM ProductSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE ps.active = true " +
           "AND (:fieldName IS NULL OR LOWER(df.name) = LOWER(:fieldName)) " +
           "AND (:year IS NULL OR py.year = :year) " +
           "AND (:manufacturerName IS NULL OR LOWER(m.name) = LOWER(:manufacturerName)) " +
           "AND (:seriesName IS NULL OR LOWER(ps.name) = LOWER(:seriesName)) " +
           "ORDER BY df.sortOrder, py.year DESC, m.sortOrder, ps.sortOrder")
    Page<ProductSeries> findByHierarchy(
        @Param("fieldName") String fieldName,
        @Param("year") Integer year,
        @Param("manufacturerName") String manufacturerName,
        @Param("seriesName") String seriesName,
        Pageable pageable
    );
    
    // Find series with products
    @Query("SELECT DISTINCT ps FROM ProductSeries ps " +
           "JOIN ps.products p " +
           "WHERE ps.active = true AND p.active = true " +
           "AND (:manufacturerId IS NULL OR ps.manufacturer.id = :manufacturerId) " +
           "ORDER BY ps.sortOrder")
    List<ProductSeries> findSeriesWithProducts(@Param("manufacturerId") Integer manufacturerId);
    
    // Find series with documents
    @Query("SELECT DISTINCT ps FROM ProductSeries ps " +
           "JOIN ps.products p " +
           "JOIN p.technicalDocuments td " +
           "WHERE ps.active = true AND p.active = true AND td.active = true " +
           "ORDER BY ps.sortOrder")
    List<ProductSeries> findSeriesWithDocuments();
    
    // Count products by series
    @Query("SELECT ps.id, COUNT(p) FROM ProductSeries ps " +
           "LEFT JOIN ps.products p " +
           "WHERE ps.active = true " +
           "AND (p IS NULL OR p.active = true) " +
           "GROUP BY ps.id")
    List<Object[]> countProductsBySeries();
    
    // Statistics queries
    @Query("SELECT COUNT(ps) FROM ProductSeries ps " +
           "JOIN ps.manufacturer m " +
           "WHERE ps.active = true AND m.id = :manufacturerId")
    long countByManufacturer(@Param("manufacturerId") Integer manufacturerId);
    
    @Query("SELECT COUNT(ps) FROM ProductSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "WHERE ps.active = true AND py.id = :yearId")
    long countByYear(@Param("yearId") Integer yearId);
    
    @Query("SELECT COUNT(ps) FROM ProductSeries ps " +
           "JOIN ps.manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE ps.active = true AND df.id = :fieldId")
    long countByField(@Param("fieldId") Integer fieldId);
    
    // Get series with most products
    @Query("SELECT ps FROM ProductSeries ps " +
           "LEFT JOIN ps.products p " +
           "WHERE ps.active = true " +
           "GROUP BY ps " +
           "ORDER BY COUNT(p) DESC")
    List<ProductSeries> findSeriesWithMostProducts(Pageable pageable);
    
    // Get total document count for series
    @Query("SELECT ps.id, COUNT(td) FROM ProductSeries ps " +
           "LEFT JOIN ps.products p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE ps.active = true " +
           "AND (p IS NULL OR p.active = true) " +
           "AND (td IS NULL OR td.active = true) " +
           "GROUP BY ps.id")
    List<Object[]> countDocumentsBySeries();
}
