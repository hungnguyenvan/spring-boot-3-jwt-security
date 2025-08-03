package com.alibou.security.core.infrastructure.repository;

import com.alibou.security.core.domain.entity.BaseEntity;
import com.alibou.security.core.domain.repository.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base repository implementation providing common functionality
 * for all domain entities with audit and activation support
 */
@Transactional(readOnly = true)
public abstract class BaseRepositoryImpl<T extends BaseEntity, ID> 
    implements BaseRepository<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;
    
    protected final Class<T> entityClass;

    public BaseRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    @Transactional
    public T save(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<T> findById(ID id) {
        T entity = entityManager.find(entityClass, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<T> results = typedQuery.getResultList();
        long total = count();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::delete);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        if (entityManager.contains(entity)) {
            entityManager.remove(entity);
        } else {
            entityManager.remove(entityManager.merge(entity));
        }
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    @Transactional
    public void deleteAllById(Iterable<ID> ids) {
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<T> findAll(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<T> results = typedQuery.getResultList();
        long total = count(spec);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public long count(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        
        query.select(cb.count(root));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public Optional<T> findOne(Specification<T> spec) {
        List<T> results = findAll(spec);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<T> findAllActive() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        query.select(root).where(cb.isTrue(root.get("active")));
        
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<T> findAllActive(Pageable pageable) {
        Specification<T> activeSpec = (root, query, cb) -> cb.isTrue(root.get("active"));
        return findAll(activeSpec, pageable);
    }

    @Override
    public long countActive() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        
        query.select(cb.count(root)).where(cb.isTrue(root.get("active")));
        
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public Page<T> search(String searchQuery, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        
        if (!predicates.isEmpty()) {
            query.where(cb.or(predicates.toArray(new Predicate[0])));
        }

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    query.orderBy(cb.asc(root.get(order.getProperty())));
                } else {
                    query.orderBy(cb.desc(root.get(order.getProperty())));
                }
            });
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<T> results = typedQuery.getResultList();
        long total = countSearchResults(searchQuery);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<T> searchActive(String searchQuery, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        predicates.add(cb.isTrue(root.get("active")));
        
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<T> results = typedQuery.getResultList();
        long total = countActiveSearchResults(searchQuery);

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<T> root, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";
            
            // Add searchable fields based on common entity patterns
            try {
                // Check if entity has common searchable fields
                if (hasField(root, "title")) {
                    predicates.add(cb.like(cb.lower(root.get("title")), likePattern));
                }
                if (hasField(root, "name")) {
                    predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
                }
                if (hasField(root, "description")) {
                    predicates.add(cb.like(cb.lower(root.get("description")), likePattern));
                }
                if (hasField(root, "author")) {
                    predicates.add(cb.like(cb.lower(root.get("author")), likePattern));
                }
            } catch (Exception e) {
                // Field doesn't exist, continue
            }
        }
        
        return predicates;
    }

    private boolean hasField(Root<T> root, String fieldName) {
        try {
            root.get(fieldName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private long countSearchResults(String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        
        if (!predicates.isEmpty()) {
            query.select(cb.count(root)).where(cb.or(predicates.toArray(new Predicate[0])));
        } else {
            query.select(cb.count(root));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    private long countActiveSearchResults(String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        predicates.add(cb.isTrue(root.get("active")));
        
        query.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getSingleResult();
    }
}
