package com.alibou.security.core.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for all domain entities
 * Provides common CRUD operations and advanced querying
 */
public interface BaseRepository<T, ID> {
    
    // Basic CRUD operations
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    Page<T> findAll(Pageable pageable);
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
    long count();
    
    // Advanced querying with Specifications
    List<T> findAll(Specification<T> spec);
    Page<T> findAll(Specification<T> spec, Pageable pageable);
    long count(Specification<T> spec);
    Optional<T> findOne(Specification<T> spec);
    
    // Active entity operations
    List<T> findAllActive();
    Page<T> findAllActive(Pageable pageable);
    long countActive();
    
    // Batch operations
    List<T> saveAll(Iterable<T> entities);
    void deleteAll(Iterable<T> entities);
    void deleteAllById(Iterable<ID> ids);
    
    // Search operations
    Page<T> search(String query, Pageable pageable);
    Page<T> searchActive(String query, Pageable pageable);
}
