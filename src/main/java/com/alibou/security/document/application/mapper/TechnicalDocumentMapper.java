package com.alibou.security.document.application.mapper;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.domain.entity.TechnicalDocument;
import com.alibou.security.document.domain.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for TechnicalDocument entity
 */
@Component
public class TechnicalDocumentMapper {
    
    public TechnicalDocumentDto toDto(TechnicalDocument entity) {
        if (entity == null) {
            return null;
        }
        
        TechnicalDocumentDto.TechnicalDocumentDtoBuilder builder = TechnicalDocumentDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .documentType(entity.getDocumentType())
            .category(entity.getCategory())
            .fileName(entity.getFileName())
            .filePath(entity.getFilePath())
            .mimeType(entity.getFileFormat()) // Map fileFormat to mimeType
            .fileSize(entity.getFileSize())
            .version(entity.getVersion())
            .language(entity.getLanguage())
            .isPublic(entity.getIsPublic())
            .downloadUrl(null) // Will be set by service layer
            .previewUrl(null) // Will be set by service layer  
            .thumbnailUrl(null) // Will be set by service layer
            .checksum(entity.getChecksum())
            .viewCount(entity.getViewCount() != null ? entity.getViewCount().longValue() : 0L)
            .downloadCount(entity.getDownloadCount() != null ? entity.getDownloadCount().longValue() : 0L)
            .rating(entity.getRating() != null ? entity.getRating().doubleValue() : 0.0)
            .sortOrder(entity.getSortOrder())
            .active(entity.getActive())
            .hierarchyPath(entity.getHierarchyPath())
            .createdDate(entity.getCreatedDate())
            .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null)
            .lastModifiedDate(entity.getLastModifiedDate())
            .lastModifiedBy(entity.getLastModifiedBy() != null ? entity.getLastModifiedBy().toString() : null);
        
        // Map product hierarchy
        if (entity.getProduct() != null) {
            Product product = entity.getProduct();
            builder.productId(product.getId())
                   .productName(product.getName());
            
            if (product.getProductSeries() != null) {
                builder.seriesId(product.getProductSeries().getId())
                       .seriesName(product.getProductSeries().getName());
                
                if (product.getProductSeries().getManufacturer() != null) {
                    builder.manufacturerId(product.getProductSeries().getManufacturer().getId())
                           .manufacturerName(product.getProductSeries().getManufacturer().getName());
                    
                    if (product.getProductSeries().getManufacturer().getProductionYear() != null) {
                        builder.yearId(product.getProductSeries().getManufacturer().getProductionYear().getId())
                               .year(product.getProductSeries().getManufacturer().getProductionYear().getYear());
                        
                        if (product.getProductSeries().getManufacturer().getProductionYear().getDocumentField() != null) {
                            builder.fieldId(product.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getId())
                                   .fieldName(product.getProductSeries().getManufacturer().getProductionYear().getDocumentField().getName());
                        }
                    }
                }
            }
        }
        
        return builder.build();
    }
    
    public TechnicalDocument toEntity(TechnicalDocumentDto dto) {
        if (dto == null) {
            return null;
        }
        
        TechnicalDocument entity = new TechnicalDocument();
        // Note: ID is managed by JPA and cannot be set directly
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setDocumentType(dto.getDocumentType());
        entity.setCategory(dto.getCategory());
        entity.setFileName(dto.getFileName());
        entity.setFilePath(dto.getFilePath());
        entity.setFileFormat(dto.getMimeType()); // Map mimeType to fileFormat
        entity.setFileSize(dto.getFileSize());
        entity.setVersion(dto.getVersion());
        entity.setLanguage(dto.getLanguage());
        entity.setIsPublic(dto.getIsPublic());
        entity.setChecksum(dto.getChecksum());
        
        if (dto.getViewCount() != null) {
            entity.setViewCount(dto.getViewCount().intValue());
        }
        if (dto.getDownloadCount() != null) {
            entity.setDownloadCount(dto.getDownloadCount().intValue());
        }
        if (dto.getRating() != null) {
            entity.setRating(java.math.BigDecimal.valueOf(dto.getRating()));
        }
        entity.setSortOrder(dto.getSortOrder());
        
        // Note: Product relationship should be set separately by service layer
        // Note: Audit fields are managed by JPA
        
        return entity;
    }
    
    public List<TechnicalDocumentDto> toDto(List<TechnicalDocument> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public List<TechnicalDocument> toEntity(List<TechnicalDocumentDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}
