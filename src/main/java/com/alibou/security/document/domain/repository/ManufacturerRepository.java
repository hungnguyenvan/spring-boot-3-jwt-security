package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.Manufacturer;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Manufacturer entity
 */
public interface ManufacturerRepository extends BaseRepository<Manufacturer, Integer> {
    
    // Find by production year
    List<Manufacturer> findByProductionYearIdAndActiveTrueOrderBySortOrder(Integer yearId);
    Page<Manufacturer> findByProductionYearIdAndActiveTrue(Integer yearId, Pageable pageable);
    
    // Find by name
    Optional<Manufacturer> findByNameAndActiveTrueAndProductionYearId(String name, Integer yearId);
    
    // Search by name pattern
    @Query("SELECT m FROM Manufacturer m " +
           "WHERE m.active = true " +
           "AND (:yearId IS NULL OR m.productionYear.id = :yearId) " +
           "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "ORDER BY m.sortOrder")
    List<Manufacturer> findByNamePattern(@Param("namePattern") String namePattern, 
                                       @Param("yearId") Integer yearId);
    
    // Complex hierarchy search
    @Query("SELECT m FROM Manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE m.active = true " +
           "AND (:fieldName IS NULL OR LOWER(df.name) = LOWER(:fieldName)) " +
           "AND (:year IS NULL OR py.year = :year) " +
           "AND (:manufacturerName IS NULL OR LOWER(m.name) = LOWER(:manufacturerName)) " +
           "ORDER BY df.sortOrder, py.year DESC, m.sortOrder")
    Page<Manufacturer> findByHierarchy(
        @Param("fieldName") String fieldName,
        @Param("year") Integer year,
        @Param("manufacturerName") String manufacturerName,
        Pageable pageable
    );
    
    // Find manufacturers with product series
    @Query("SELECT DISTINCT m FROM Manufacturer m " +
           "JOIN m.productSeries ps " +
           "WHERE m.active = true AND ps.active = true " +
           "AND (:yearId IS NULL OR m.productionYear.id = :yearId) " +
           "ORDER BY m.sortOrder")
    List<Manufacturer> findManufacturersWithSeries(@Param("yearId") Integer yearId);
    
    // Find manufacturers with products
    @Query("SELECT DISTINCT m FROM Manufacturer m " +
           "JOIN m.productSeries ps " +
           "JOIN ps.products p " +
           "WHERE m.active = true AND ps.active = true AND p.active = true " +
           "ORDER BY m.sortOrder")
    List<Manufacturer> findManufacturersWithProducts();
    
    // Find manufacturers with documents
    @Query("SELECT DISTINCT m FROM Manufacturer m " +
           "JOIN m.productSeries ps " +
           "JOIN ps.products p " +
           "JOIN p.technicalDocuments td " +
           "WHERE m.active = true AND ps.active = true AND p.active = true AND td.active = true " +
           "ORDER BY m.sortOrder")
    List<Manufacturer> findManufacturersWithDocuments();
    
    // Count series by manufacturer
    @Query("SELECT m.id, COUNT(ps) FROM Manufacturer m " +
           "LEFT JOIN m.productSeries ps " +
           "WHERE m.active = true " +
           "AND (ps IS NULL OR ps.active = true) " +
           "GROUP BY m.id")
    List<Object[]> countSeriesByManufacturer();
    
    // Statistics queries
    @Query("SELECT COUNT(m) FROM Manufacturer m " +
           "JOIN m.productionYear py " +
           "WHERE m.active = true AND py.id = :yearId")
    long countByYear(@Param("yearId") Integer yearId);
    
    @Query("SELECT COUNT(m) FROM Manufacturer m " +
           "JOIN m.productionYear py " +
           "JOIN py.documentField df " +
           "WHERE m.active = true AND df.id = :fieldId")
    long countByField(@Param("fieldId") Integer fieldId);
    
    // Get manufacturers with most series
    @Query("SELECT m FROM Manufacturer m " +
           "LEFT JOIN m.productSeries ps " +
           "WHERE m.active = true " +
           "GROUP BY m " +
           "ORDER BY COUNT(ps) DESC")
    List<Manufacturer> findManufacturersWithMostSeries(Pageable pageable);
    
    // Get total product count for manufacturer
    @Query("SELECT m.id, COUNT(p) FROM Manufacturer m " +
           "LEFT JOIN m.productSeries ps " +
           "LEFT JOIN ps.products p " +
           "WHERE m.active = true " +
           "AND (ps IS NULL OR ps.active = true) " +
           "AND (p IS NULL OR p.active = true) " +
           "GROUP BY m.id")
    List<Object[]> countProductsByManufacturer();
    
    // Get total document count for manufacturer
    @Query("SELECT m.id, COUNT(td) FROM Manufacturer m " +
           "LEFT JOIN m.productSeries ps " +
           "LEFT JOIN ps.products p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE m.active = true " +
           "AND (ps IS NULL OR ps.active = true) " +
           "AND (p IS NULL OR p.active = true) " +
           "AND (td IS NULL OR td.active = true) " +
           "GROUP BY m.id")
    List<Object[]> countDocumentsByManufacturer();
}
