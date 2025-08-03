package com.alibou.security.core.domain.repository;

import com.alibou.security.core.domain.entity.BookTypePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BookTypePermission entity
 */
public interface BookTypePermissionRepository extends JpaRepository<BookTypePermission, Integer> {
    
    /**
     * Find permission by user and book type
     */
    @Query("SELECT p FROM BookTypePermission p WHERE p.user.id = :userId AND p.bookType.id = :bookTypeId")
    Optional<BookTypePermission> findByUserIdAndBookTypeId(@Param("userId") Integer userId, @Param("bookTypeId") Integer bookTypeId);
    
    /**
     * Find all permissions for a user
     */
    @Query("SELECT p FROM BookTypePermission p WHERE p.user.id = :userId")
    List<BookTypePermission> findByUserId(@Param("userId") Integer userId);
    
    /**
     * Find all permissions for a book type
     */
    @Query("SELECT p FROM BookTypePermission p WHERE p.bookType.id = :bookTypeId")
    List<BookTypePermission> findByBookTypeId(@Param("bookTypeId") Integer bookTypeId);
}
