package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for DocumentField entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFieldDto {
    
    private Integer id;
    
    private String name;
    
    private String code;
    
    private String description;
    
    private String icon;
    
    private String color;
    
    private Integer sortOrder;
    
    private Boolean active;
    
    // Statistics (read-only)
    private Long yearCount;
    private Long manufacturerCount;
    private Long totalDocumentCount;
    
    // Audit fields
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
