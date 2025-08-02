package com.alibou.security.booktype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookTypeRepository extends JpaRepository<BookType, Integer> {

    Optional<BookType> findByName(String name);

    List<BookType> findByActiveTrue();

    List<BookType> findByActiveTrueOrderBySortOrder();

    List<BookType> findByCategory(String category);

    @Query("SELECT bt FROM BookType bt WHERE bt.active = true AND " +
           "(LOWER(bt.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(bt.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(bt.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BookType> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT bt FROM BookType bt WHERE " +
           "(LOWER(bt.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(bt.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(bt.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BookType> findAllBySearchTerm(@Param("search") String search, Pageable pageable);

    boolean existsByName(String name);

    long countByActiveTrue();

    @Query("SELECT DISTINCT bt.category FROM BookType bt WHERE bt.category IS NOT NULL ORDER BY bt.category")
    List<String> findAllDistinctCategories();
}
