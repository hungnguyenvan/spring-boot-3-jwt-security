package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for hierarchical document search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentHierarchySearchDto {
    
    private String fieldName;
    
    private Integer year;
    
    private String manufacturerName;
    
    private String seriesName;
    
    private String productName;
    
    private String documentType;
    
    private String category;
    
    private String language;
    
    private Boolean isPublic;
    
    private String query; // For full text search
}
