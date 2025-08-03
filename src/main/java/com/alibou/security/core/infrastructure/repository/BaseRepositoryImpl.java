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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base repository implementation providing common functionality
 * for all domain entities with audit and activation support
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaseRepositoryImpl<T extends BaseEntity, ID> 
    extends SimpleJpaRepository<T, ID> 
    implements BaseRepository<T, ID> {

    @PersistenceContext
    private final EntityManager entityManager;
    
    private final JpaEntityInformation<T, ID> entityInformation;

    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    @Override
    public List<T> findAllActive() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityInformation.getJavaType());
        Root<T> root = query.from(entityInformation.getJavaType());
        
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
        Root<T> root = query.from(entityInformation.getJavaType());
        
        query.select(cb.count(root)).where(cb.isTrue(root.get("active")));
        
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public Page<T> search(String searchQuery, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityInformation.getJavaType());
        Root<T> root = query.from(entityInformation.getJavaType());

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
        CriteriaQuery<T> query = cb.createQuery(entityInformation.getJavaType());
        Root<T> root = query.from(entityInformation.getJavaType());

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
        Root<T> root = query.from(entityInformation.getJavaType());

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
        Root<T> root = query.from(entityInformation.getJavaType());

        List<Predicate> predicates = buildSearchPredicates(cb, root, searchQuery);
        predicates.add(cb.isTrue(root.get("active")));
        
        query.select(cb.count(root)).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getSingleResult();
    }
}
