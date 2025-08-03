package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.ProductionYear;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductionYear entity
 */
public interface ProductionYearRepository extends BaseRepository<ProductionYear, Integer> {
    
    // Find by document field
    List<ProductionYear> findByDocumentFieldIdAndActiveTrueOrderByYearDesc(Integer fieldId);
    Page<ProductionYear> findByDocumentFieldIdAndActiveTrue(Integer fieldId, Pageable pageable);
    
    // Find by year
    Optional<ProductionYear> findByYearAndActiveTrueAndDocumentFieldId(Integer year, Integer fieldId);
    
    // Find years in range
    @Query("SELECT py FROM ProductionYear py " +
           "WHERE py.active = true " +
           "AND (:fieldId IS NULL OR py.documentField.id = :fieldId) " +
           "AND py.year BETWEEN :startYear AND :endYear " +
           "ORDER BY py.year DESC")
    List<ProductionYear> findByYearRange(@Param("startYear") Integer startYear, 
                                        @Param("endYear") Integer endYear,
                                        @Param("fieldId") Integer fieldId);
    
    // Complex hierarchy search
    @Query("SELECT py FROM ProductionYear py " +
           "JOIN py.documentField df " +
           "WHERE py.active = true " +
           "AND (:fieldName IS NULL OR LOWER(df.name) = LOWER(:fieldName)) " +
           "AND (:year IS NULL OR py.year = :year) " +
           "ORDER BY df.sortOrder, py.year DESC")
    Page<ProductionYear> findByHierarchy(
        @Param("fieldName") String fieldName,
        @Param("year") Integer year,
        Pageable pageable
    );
    
    // Find years with manufacturers
    @Query("SELECT DISTINCT py FROM ProductionYear py " +
           "JOIN py.manufacturers m " +
           "WHERE py.active = true AND m.active = true " +
           "AND (:fieldId IS NULL OR py.documentField.id = :fieldId) " +
           "ORDER BY py.year DESC")
    List<ProductionYear> findYearsWithManufacturers(@Param("fieldId") Integer fieldId);
    
    // Find years with products
    @Query("SELECT DISTINCT py FROM ProductionYear py " +
           "JOIN py.manufacturers m " +
           "JOIN m.productSeries ps " +
           "JOIN ps.products p " +
           "WHERE py.active = true AND m.active = true AND ps.active = true AND p.active = true " +
           "ORDER BY py.year DESC")
    List<ProductionYear> findYearsWithProducts();
    
    // Find years with documents
    @Query("SELECT DISTINCT py FROM ProductionYear py " +
           "JOIN py.manufacturers m " +
           "JOIN m.productSeries ps " +
           "JOIN ps.products p " +
           "JOIN p.technicalDocuments td " +
           "WHERE py.active = true AND m.active = true AND ps.active = true " +
           "AND p.active = true AND td.active = true " +
           "ORDER BY py.year DESC")
    List<ProductionYear> findYearsWithDocuments();
    
    // Count manufacturers by year
    @Query("SELECT py.id, COUNT(m) FROM ProductionYear py " +
           "LEFT JOIN py.manufacturers m " +
           "WHERE py.active = true " +
           "AND (m IS NULL OR m.active = true) " +
           "GROUP BY py.id")
    List<Object[]> countManufacturersByYear();
    
    // Statistics queries
    @Query("SELECT COUNT(py) FROM ProductionYear py " +
           "JOIN py.documentField df " +
           "WHERE py.active = true AND df.id = :fieldId")
    long countByField(@Param("fieldId") Integer fieldId);
    
    @Query("SELECT MIN(py.year) FROM ProductionYear py " +
           "WHERE py.active = true " +
           "AND (:fieldId IS NULL OR py.documentField.id = :fieldId)")
    Integer findMinYear(@Param("fieldId") Integer fieldId);
    
    @Query("SELECT MAX(py.year) FROM ProductionYear py " +
           "WHERE py.active = true " +
           "AND (:fieldId IS NULL OR py.documentField.id = :fieldId)")
    Integer findMaxYear(@Param("fieldId") Integer fieldId);
    
    // Get years with most manufacturers
    @Query("SELECT py FROM ProductionYear py " +
           "LEFT JOIN py.manufacturers m " +
           "WHERE py.active = true " +
           "GROUP BY py " +
           "ORDER BY COUNT(m) DESC")
    List<ProductionYear> findYearsWithMostManufacturers(Pageable pageable);
    
    // Get total product count for year
    @Query("SELECT py.id, COUNT(p) FROM ProductionYear py " +
           "LEFT JOIN py.manufacturers m " +
           "LEFT JOIN m.productSeries ps " +
           "LEFT JOIN ps.products p " +
           "WHERE py.active = true " +
           "AND (m IS NULL OR m.active = true) " +
           "AND (ps IS NULL OR ps.active = true) " +
           "AND (p IS NULL OR p.active = true) " +
           "GROUP BY py.id")
    List<Object[]> countProductsByYear();
    
    // Get total document count for year
    @Query("SELECT py.id, COUNT(td) FROM ProductionYear py " +
           "LEFT JOIN py.manufacturers m " +
           "LEFT JOIN m.productSeries ps " +
           "LEFT JOIN ps.products p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE py.active = true " +
           "AND (m IS NULL OR m.active = true) " +
           "AND (ps IS NULL OR ps.active = true) " +
           "AND (p IS NULL OR p.active = true) " +
           "AND (td IS NULL OR td.active = true) " +
           "GROUP BY py.id")
    List<Object[]> countDocumentsByYear();
}
