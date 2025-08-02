package com.alibou.security.booktype;

import com.alibou.security.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "editor_book_type_permission", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_type_id"}))
@EntityListeners(AuditingEntityListener.class)
public class EditorBookTypePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "editor_permission_seq")
    @SequenceGenerator(name = "editor_permission_seq", sequenceName = "editor_permission_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_type_id", nullable = false)
    private BookType bookType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canEdit = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canDelete = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private Integer createdBy;
}
