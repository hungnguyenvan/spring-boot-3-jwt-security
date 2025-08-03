package com.alibou.security.document.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TechnicalDocument entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalDocumentDto {
    
    private Integer id;
    
    private String title;
    
    private String description;
    
    private String documentType;
    
    private String category;
    
    private String fileName;
    
    private String filePath;
    
    private String mimeType;
    
    private Long fileSize;
    
    private String version;
    
    private String language;
    
    private Boolean isPublic;
    
    private String downloadUrl;
    
    private String previewUrl;
    
    private String thumbnailUrl;
    
    private String checksum;
    
    private Long viewCount;
    
    private Long downloadCount;
    
    private Double rating;
    
    private Integer sortOrder;
    
    private Boolean active;
    
    // Product information
    private Integer productId;
    private String productName;
    
    // Series information
    private Integer seriesId;
    private String seriesName;
    
    // Manufacturer information
    private Integer manufacturerId;
    private String manufacturerName;
    
    // Year information
    private Integer yearId;
    private Integer year;
    
    // Field information
    private Integer fieldId;
    private String fieldName;
    
    // Hierarchy path
    private String hierarchyPath;
    
    // Audit fields
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
