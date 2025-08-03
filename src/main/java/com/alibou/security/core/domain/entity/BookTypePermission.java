package com.alibou.security.core.domain.entity;

import com.alibou.security.booktype.BookType;
import com.alibou.security.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for book type permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_type_permissions")
public class BookTypePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_type_id", nullable = false)
    private BookType bookType;
    
    @Builder.Default
    @Column(name = "can_read", nullable = false)
    private Boolean canRead = false;
    
    @Builder.Default
    @Column(name = "can_write", nullable = false)
    private Boolean canWrite = false;
    
    @Builder.Default
    @Column(name = "can_delete", nullable = false)
    private Boolean canDelete = false;
    
    @Builder.Default
    @Column(name = "can_manage", nullable = false)
    private Boolean canManage = false;
}
