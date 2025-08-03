package com.alibou.security.core.application.service;

import com.alibou.security.core.domain.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Base application service interface defining common operations
 * for all domain entities in the library management system
 */
public interface BaseApplicationService<Entity extends BaseEntity, Request, Response, ID> {
    
    /**
     * Create a new entity
     */
    Response create(Request request);
    
    /**
     * Update an existing entity
     */
    Response update(ID id, Request request);
    
    /**
     * Find entity by ID
     */
    Optional<Response> findById(ID id);
    
    /**
     * Find all entities with pagination
     */
    Page<Response> findAll(Pageable pageable);
    
    /**
     * Find all active entities with pagination
     */
    Page<Response> findAllActive(Pageable pageable);
    
    /**
     * Search entities with query
     */
    Page<Response> search(String query, Pageable pageable);
    
    /**
     * Activate an entity
     */
    void activate(ID id);
    
    /**
     * Deactivate an entity
     */
    void deactivate(ID id);
    
    /**
     * Delete an entity (soft delete by deactivating)
     */
    void delete(ID id);
    
    /**
     * Permanently delete an entity (hard delete)
     */
    void permanentDelete(ID id);
    
    /**
     * Check if entity exists
     */
    boolean exists(ID id);
    
    /**
     * Get total count
     */
    long count();
    
    /**
     * Get active count
     */
    long countActive();
    
    /**
     * Batch operations
     */
    List<Response> createBatch(List<Request> requests);
    List<Response> updateBatch(List<ID> ids, List<Request> requests);
    void deleteBatch(List<ID> ids);
    void activateBatch(List<ID> ids);
    void deactivateBatch(List<ID> ids);
}
