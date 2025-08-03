package com.alibou.security.core.application.service.impl;

import com.alibou.security.core.application.service.BaseApplicationService;
import com.alibou.security.core.domain.entity.BaseEntity;
import com.alibou.security.core.domain.repository.BaseRepository;
import com.alibou.security.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract base implementation for application services
 * Provides common CRUD operations and business logic patterns
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class BaseApplicationServiceImpl<
    Entity extends BaseEntity, 
    Request, 
    Response, 
    ID
> implements BaseApplicationService<Entity, Request, Response, ID> {

    protected final BaseRepository<Entity, ID> repository;

    @Override
    @Transactional
    public Response create(Request request) {
        log.debug("Creating new entity with request: {}", request);
        
        Entity entity = mapToEntity(request);
        entity.activate(); // Ensure new entities are active
        
        Entity savedEntity = repository.save(entity);
        log.info("Created entity with ID: {}", savedEntity.getId());
        
        return mapToResponse(savedEntity);
    }

    @Override
    @Transactional
    public Response update(ID id, Request request) {
        log.debug("Updating entity with ID: {} and request: {}", id, request);
        
        Entity existingEntity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));
        
        updateEntityFromRequest(existingEntity, request);
        Entity savedEntity = repository.save(existingEntity);
        
        log.info("Updated entity with ID: {}", savedEntity.getId());
        return mapToResponse(savedEntity);
    }

    @Override
    public Optional<Response> findById(ID id) {
        log.debug("Finding entity by ID: {}", id);
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Page<Response> findAll(Pageable pageable) {
        log.debug("Finding all entities with pagination: {}", pageable);
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<Response> findAllActive(Pageable pageable) {
        log.debug("Finding all active entities with pagination: {}", pageable);
        return repository.findAllActive(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<Response> search(String query, Pageable pageable) {
        log.debug("Searching entities with query: '{}' and pagination: {}", query, pageable);
        return repository.searchActive(query, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void activate(ID id) {
        log.debug("Activating entity with ID: {}", id);
        
        Entity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));
        
        entity.activate();
        repository.save(entity);
        
        log.info("Activated entity with ID: {}", id);
    }

    @Override
    @Transactional
    public void deactivate(ID id) {
        log.debug("Deactivating entity with ID: {}", id);
        
        Entity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));
        
        entity.deactivate();
        repository.save(entity);
        
        log.info("Deactivated entity with ID: {}", id);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        log.debug("Soft deleting entity with ID: {}", id);
        deactivate(id); // Soft delete by deactivating
    }

    @Override
    @Transactional
    public void permanentDelete(ID id) {
        log.debug("Permanently deleting entity with ID: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Entity not found with ID: " + id);
        }
        
        repository.deleteById(id);
        log.warn("Permanently deleted entity with ID: {}", id);
    }

    @Override
    public boolean exists(ID id) {
        return repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countActive() {
        return repository.countActive();
    }

    @Override
    @Transactional
    public List<Response> createBatch(List<Request> requests) {
        log.debug("Creating batch of {} entities", requests.size());
        
        List<Entity> entities = requests.stream()
            .map(this::mapToEntity)
            .peek(Entity::activate)
            .collect(Collectors.toList());
        
        List<Entity> savedEntities = repository.saveAll(entities);
        log.info("Created batch of {} entities", savedEntities.size());
        
        return savedEntities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Response> updateBatch(List<ID> ids, List<Request> requests) {
        if (ids.size() != requests.size()) {
            throw new IllegalArgumentException("IDs and requests lists must have the same size");
        }
        
        log.debug("Updating batch of {} entities", ids.size());
        
        List<Entity> updatedEntities = ids.stream()
            .map(id -> {
                Entity entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id));
                
                Request request = requests.get(ids.indexOf(id));
                updateEntityFromRequest(entity, request);
                return entity;
            })
            .collect(Collectors.toList());
        
        List<Entity> savedEntities = repository.saveAll(updatedEntities);
        log.info("Updated batch of {} entities", savedEntities.size());
        
        return savedEntities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBatch(List<ID> ids) {
        log.debug("Soft deleting batch of {} entities", ids.size());
        deactivateBatch(ids);
    }

    @Override
    @Transactional
    public void activateBatch(List<ID> ids) {
        log.debug("Activating batch of {} entities", ids.size());
        
        List<Entity> entities = ids.stream()
            .map(id -> repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id)))
            .peek(Entity::activate)
            .collect(Collectors.toList());
        
        repository.saveAll(entities);
        log.info("Activated batch of {} entities", entities.size());
    }

    @Override
    @Transactional
    public void deactivateBatch(List<ID> ids) {
        log.debug("Deactivating batch of {} entities", ids.size());
        
        List<Entity> entities = ids.stream()
            .map(id -> repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with ID: " + id)))
            .peek(Entity::deactivate)
            .collect(Collectors.toList());
        
        repository.saveAll(entities);
        log.info("Deactivated batch of {} entities", entities.size());
    }

    // Abstract methods to be implemented by concrete services
    protected abstract Entity mapToEntity(Request request);
    protected abstract Response mapToResponse(Entity entity);
    protected abstract void updateEntityFromRequest(Entity entity, Request request);
}
