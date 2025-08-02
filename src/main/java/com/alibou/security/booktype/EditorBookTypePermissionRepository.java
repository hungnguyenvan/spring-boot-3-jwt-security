package com.alibou.security.booktype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EditorBookTypePermissionRepository extends JpaRepository<EditorBookTypePermission, Integer> {

    @Query("SELECT ebtp FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.bookType.id = :bookTypeId AND ebtp.active = true")
    Optional<EditorBookTypePermission> findByUserIdAndBookTypeId(@Param("userId") Integer userId, 
                                                                  @Param("bookTypeId") Integer bookTypeId);

    @Query("SELECT ebtp FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.active = true")
    List<EditorBookTypePermission> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT ebtp FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.bookType.id = :bookTypeId AND ebtp.active = true")
    List<EditorBookTypePermission> findByBookTypeId(@Param("bookTypeId") Integer bookTypeId);

    @Query("SELECT DISTINCT ebtp.bookType FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.canEdit = true AND ebtp.active = true")
    List<BookType> findEditableBookTypesByUserId(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT ebtp.bookType FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.canDelete = true AND ebtp.active = true")
    List<BookType> findDeletableBookTypesByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(ebtp) > 0 FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.bookType.id = :bookTypeId " +
           "AND ebtp.canEdit = true AND ebtp.active = true")
    boolean canUserEditBookType(@Param("userId") Integer userId, @Param("bookTypeId") Integer bookTypeId);

    @Query("SELECT COUNT(ebtp) > 0 FROM EditorBookTypePermission ebtp " +
           "WHERE ebtp.user.id = :userId AND ebtp.bookType.id = :bookTypeId " +
           "AND ebtp.canDelete = true AND ebtp.active = true")
    boolean canUserDeleteBookType(@Param("userId") Integer userId, @Param("bookTypeId") Integer bookTypeId);

    boolean existsByUserIdAndBookTypeIdAndActiveTrue(Integer userId, Integer bookTypeId);
}
