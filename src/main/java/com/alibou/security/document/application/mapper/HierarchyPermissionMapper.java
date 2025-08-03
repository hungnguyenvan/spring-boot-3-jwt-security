package com.alibou.security.document.application.mapper;

import com.alibou.security.document.application.dto.HierarchyPermissionDto;
import com.alibou.security.document.domain.entity.EditorHierarchyPermission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for EditorHierarchyPermission entity and DTOs
 */
@Component
public class HierarchyPermissionMapper {
    
    /**
     * Convert entity to DTO
     */
    public HierarchyPermissionDto toDto(EditorHierarchyPermission entity) {
        if (entity == null) {
            return null;
        }
        
        HierarchyPermissionDto.HierarchyPermissionDtoBuilder builder = HierarchyPermissionDto.builder()
            .id(entity.getId())
            .canUpload(entity.getCanUpload())
            .canEdit(entity.getCanEdit())
            .canDelete(entity.getCanDelete())
            .canView(entity.getCanView())
            .permissionLevel(entity.getPermissionLevel())
            .hierarchyPath(entity.getHierarchyPath())
            .scopeDescription(entity.getScopeDescription())
            .permissionSummary(entity.getPermissionSummary())
            .createdDate(entity.getCreatedDate())
            .lastModifiedDate(entity.getLastModifiedDate())
            .createdBy(entity.getCreatedBy())
            .lastModifiedBy(entity.getLastModifiedBy())
            .active(entity.getActive());
        
        // Map editor information
        if (entity.getEditor() != null) {
            builder.editorId(entity.getEditor().getId())
                   .editorUsername(entity.getEditor().getUsername())
                   .editorEmail(entity.getEditor().getEmail());
        }
        
        // Map hierarchy scope
        if (entity.getDocumentField() != null) {
            builder.documentFieldId(entity.getDocumentField().getId())
                   .documentFieldName(entity.getDocumentField().getName());
        }
        
        if (entity.getProductionYear() != null) {
            builder.productionYearId(entity.getProductionYear().getId())
                   .productionYear(entity.getProductionYear().getYear());
        }
        
        if (entity.getManufacturer() != null) {
            builder.manufacturerId(entity.getManufacturer().getId())
                   .manufacturerName(entity.getManufacturer().getName());
        }
        
        if (entity.getProductSeries() != null) {
            builder.productSeriesId(entity.getProductSeries().getId())
                   .productSeriesName(entity.getProductSeries().getName());
        }
        
        if (entity.getProduct() != null) {
            builder.productId(entity.getProduct().getId())
                   .productName(entity.getProduct().getName());
        }
        
        return builder.build();
    }
    
    /**
     * Convert list of entities to DTOs
     */
    public List<HierarchyPermissionDto> toDto(List<EditorHierarchyPermission> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert entity to summary DTO (minimal information)
     */
    public HierarchyPermissionDto toSummaryDto(EditorHierarchyPermission entity) {
        if (entity == null) {
            return null;
        }
        
        return HierarchyPermissionDto.builder()
            .id(entity.getId())
            .editorId(entity.getEditor() != null ? entity.getEditor().getId() : null)
            .editorUsername(entity.getEditor() != null ? entity.getEditor().getUsername() : null)
            .permissionLevel(entity.getPermissionLevel())
            .hierarchyPath(entity.getHierarchyPath())
            .canUpload(entity.getCanUpload())
            .canEdit(entity.getCanEdit())
            .canDelete(entity.getCanDelete())
            .canView(entity.getCanView())
            .active(entity.getActive())
            .build();
    }
    
    /**
     * Convert list of entities to summary DTOs
     */
    public List<HierarchyPermissionDto> toSummaryDto(List<EditorHierarchyPermission> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
    }
}
