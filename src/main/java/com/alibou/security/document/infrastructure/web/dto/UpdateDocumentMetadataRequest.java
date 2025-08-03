package com.alibou.security.document.infrastructure.web.dto;

import com.alibou.security.document.domain.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating document metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentMetadataRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    private DocumentType documentType;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @Size(max = 100, message = "Sub category must not exceed 100 characters")
    private String subCategory;
    
    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String version;
    
    @Size(max = 5, message = "Language must not exceed 5 characters")
    private String language;
    
    private Boolean isPublic;
    
    private Boolean downloadable;
    
    private Integer sortOrder;
}
