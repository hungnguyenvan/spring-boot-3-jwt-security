package com.alibou.security.booktype;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_type")
@EntityListeners(AuditingEntityListener.class)
public class BookType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_type_seq")
    @SequenceGenerator(name = "book_type_seq", sequenceName = "book_type_id_seq", allocationSize = 1)
    private Integer id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 50)
    private String category; // FICTION, NON_FICTION, ACADEMIC, TECHNICAL, etc.

    @Column(length = 7)
    private String colorCode; // Hex color code for UI display

    @Builder.Default
    @Column
    private Integer sortOrder = 0; // For ordering display

    @CreatedDate
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(
            nullable = false,
            updatable = false
    )
    private Integer createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private Integer lastModifiedBy;
}
