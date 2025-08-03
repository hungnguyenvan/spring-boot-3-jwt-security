package com.alibou.security.document.application.mapper;

import com.alibou.security.document.application.dto.DocumentFieldDto;
import com.alibou.security.document.domain.entity.DocumentField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for DocumentField entity
 */
@Component
public class DocumentFieldMapper {
    
    public DocumentFieldDto toDto(DocumentField entity) {
        if (entity == null) {
            return null;
        }
        
        return DocumentFieldDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .code(entity.getCode())
            .description(entity.getDescription())
            .color(entity.getColorCode()) // Map colorCode to color
            .sortOrder(entity.getSortOrder())
            .active(entity.getActive())
            .totalDocumentCount(entity.getTotalDocuments())
            .createdDate(entity.getCreatedDate())
            .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null)
            .lastModifiedDate(entity.getLastModifiedDate())
            .lastModifiedBy(entity.getLastModifiedBy() != null ? entity.getLastModifiedBy().toString() : null)
            .build();
    }
    
    public DocumentField toEntity(DocumentFieldDto dto) {
        if (dto == null) {
            return null;
        }
        
        DocumentField entity = new DocumentField();
        // Note: ID is managed by JPA
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setColorCode(dto.getColor()); // Map color to colorCode
        entity.setSortOrder(dto.getSortOrder());
        
        // Note: Audit fields are managed by JPA
        // Note: Statistics are calculated fields, not set directly
        
        return entity;
    }
    
    public List<DocumentFieldDto> toDto(List<DocumentField> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public List<DocumentField> toEntity(List<DocumentFieldDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}
