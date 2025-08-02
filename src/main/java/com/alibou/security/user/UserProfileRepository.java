package com.alibou.security.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Optional<UserProfile> findByUserId(Integer userId);

    Optional<UserProfile> findByUserEmail(String email);

    @Query("SELECT up FROM UserProfile up WHERE " +
           "LOWER(up.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(up.country) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<UserProfile> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT up FROM UserProfile up WHERE up.activityStatus = :status")
    Page<UserProfile> findByActivityStatus(@Param("status") UserProfile.ActivityStatus status, Pageable pageable);

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.activityStatus = :status")
    Long countByActivityStatus(@Param("status") UserProfile.ActivityStatus status);
}
