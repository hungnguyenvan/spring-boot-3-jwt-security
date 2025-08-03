package com.alibou.security.document.domain.repository;

import com.alibou.security.document.domain.entity.DocumentField;
import com.alibou.security.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DocumentField entity
 */
public interface DocumentFieldRepository extends BaseRepository<DocumentField, Integer> {
    
    // Find by name or code
    Optional<DocumentField> findByNameIgnoreCase(String name);
    Optional<DocumentField> findByCodeIgnoreCase(String code);
    
    // Find all ordered by sort order
    List<DocumentField> findAllByActiveTrueOrderBySortOrder();
    
    // Find with statistics
    @Query("SELECT df FROM DocumentField df " +
           "LEFT JOIN FETCH df.productionYears py " +
           "WHERE df.active = true " +
           "ORDER BY df.sortOrder")
    List<DocumentField> findAllWithProductionYears();
    
    // Search by name or description
    @Query("SELECT df FROM DocumentField df " +
           "WHERE df.active = true " +
           "AND (LOWER(df.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(df.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY df.sortOrder")
    Page<DocumentField> searchByNameOrDescription(@Param("query") String query, Pageable pageable);
    
    // Get document counts
    @Query("SELECT df.id, COUNT(td) FROM DocumentField df " +
           "LEFT JOIN df.productionYears py " +
           "LEFT JOIN py.manufacturers m " +
           "LEFT JOIN m.productSeries ps " +
           "LEFT JOIN ps.products p " +
           "LEFT JOIN p.technicalDocuments td " +
           "WHERE df.active = true " +
           "GROUP BY df.id")
    List<Object[]> getDocumentCounts();
    
    // Find fields with specific year
    @Query("SELECT DISTINCT df FROM DocumentField df " +
           "JOIN df.productionYears py " +
           "WHERE df.active = true AND py.year = :year " +
           "ORDER BY df.sortOrder")
    List<DocumentField> findByYear(@Param("year") Integer year);
    
    // Find fields with manufacturer
    @Query("SELECT DISTINCT df FROM DocumentField df " +
           "JOIN df.productionYears py " +
           "JOIN py.manufacturers m " +
           "WHERE df.active = true " +
           "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :manufacturerName, '%')) " +
           "ORDER BY df.sortOrder")
    List<DocumentField> findByManufacturerName(@Param("manufacturerName") String manufacturerName);
}
