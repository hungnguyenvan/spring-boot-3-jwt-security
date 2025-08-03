package com.alibou.security.document.application.service;

import com.alibou.security.document.application.dto.DocumentFieldDto;
import com.alibou.security.document.application.mapper.DocumentFieldMapper;
import com.alibou.security.document.domain.entity.DocumentField;
import com.alibou.security.document.domain.repository.DocumentFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for DocumentField management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentFieldApplicationService {
    
    private final DocumentFieldRepository documentFieldRepository;
    private final DocumentFieldMapper documentFieldMapper;
    
    /**
     * Find all active fields
     */
    @Transactional(readOnly = true)
    public List<DocumentFieldDto> findAllActive() {
        log.debug("Finding all active document fields");
        List<DocumentField> fields = documentFieldRepository.findAllByActiveTrueOrderBySortOrder();
        return documentFieldMapper.toDto(fields);
    }
    
    /**
     * Find field by name
     */
    @Transactional(readOnly = true)
    public Optional<DocumentFieldDto> findByName(String name) {
        log.debug("Finding document field by name: {}", name);
        return documentFieldRepository.findByNameIgnoreCase(name)
            .map(documentFieldMapper::toDto);
    }
    
    /**
     * Find field by code
     */
    @Transactional(readOnly = true)
    public Optional<DocumentFieldDto> findByCode(String code) {
        log.debug("Finding document field by code: {}", code);
        return documentFieldRepository.findByCodeIgnoreCase(code)
            .map(documentFieldMapper::toDto);
    }
    
    /**
     * Search fields by name or description
     */
    @Transactional(readOnly = true)
    public Page<DocumentFieldDto> searchByNameOrDescription(String query, Pageable pageable) {
        log.debug("Searching document fields by name or description: {}", query);
        Page<DocumentField> fields = documentFieldRepository.searchByNameOrDescription(query, pageable);
        return fields.map(documentFieldMapper::toDto);
    }
    
    /**
     * Find fields by year
     */
    @Transactional(readOnly = true)
    public List<DocumentFieldDto> findByYear(Integer year) {
        log.debug("Finding document fields by year: {}", year);
        List<DocumentField> fields = documentFieldRepository.findByYear(year);
        return documentFieldMapper.toDto(fields);
    }
    
    /**
     * Find fields by manufacturer name
     */
    @Transactional(readOnly = true)
    public List<DocumentFieldDto> findByManufacturerName(String manufacturerName) {
        log.debug("Finding document fields by manufacturer name: {}", manufacturerName);
        List<DocumentField> fields = documentFieldRepository.findByManufacturerName(manufacturerName);
        return documentFieldMapper.toDto(fields);
    }
    
    /**
     * Get fields with production years loaded
     */
    @Transactional(readOnly = true)
    public List<DocumentFieldDto> findAllWithProductionYears() {
        log.debug("Finding all document fields with production years");
        List<DocumentField> fields = documentFieldRepository.findAllWithProductionYears();
        return documentFieldMapper.toDto(fields);
    }
    
    /**
     * Get document statistics for all fields
     */
    @Transactional(readOnly = true)
    public List<FieldStatistics> getDocumentStatistics() {
        log.debug("Getting document statistics for all fields");
        List<Object[]> counts = documentFieldRepository.getDocumentCounts();
        List<DocumentField> fields = documentFieldRepository.findAllByActiveTrueOrderBySortOrder();
        
        return fields.stream()
            .map(field -> {
                Long documentCount = findDocumentCountForField(counts, field.getId());
                return FieldStatistics.builder()
                    .fieldId(field.getId())
                    .fieldName(field.getName())
                    .fieldCode(field.getCode())
                    .documentCount(documentCount)
                    .build();
            })
            .toList();
    }
    
    /**
     * Create new field
     */
    public DocumentFieldDto create(DocumentFieldDto dto) {
        log.debug("Creating new document field: {}", dto.getName());
        DocumentField entity = documentFieldMapper.toEntity(dto);
        DocumentField saved = documentFieldRepository.save(entity);
        return documentFieldMapper.toDto(saved);
    }
    
    /**
     * Update existing field
     */
    public DocumentFieldDto update(Integer id, DocumentFieldDto dto) {
        log.debug("Updating document field ID: {}", id);
        Optional<DocumentField> existingOpt = documentFieldRepository.findById(id);
        if (existingOpt.isPresent()) {
            DocumentField existing = existingOpt.get();
            existing.setName(dto.getName());
            existing.setCode(dto.getCode());
            existing.setDescription(dto.getDescription());
            existing.setColorCode(dto.getColor());
            existing.setSortOrder(dto.getSortOrder());
            
            DocumentField updated = documentFieldRepository.save(existing);
            return documentFieldMapper.toDto(updated);
        }
        return null;
    }
    
    /**
     * Delete field (soft delete)
     */
    public void delete(Integer id) {
        log.debug("Deleting document field ID: {}", id);
        Optional<DocumentField> fieldOpt = documentFieldRepository.findById(id);
        if (fieldOpt.isPresent()) {
            DocumentField field = fieldOpt.get();
            field.deactivate();
            documentFieldRepository.save(field);
        }
    }
    
    private Long findDocumentCountForField(List<Object[]> counts, Integer fieldId) {
        return counts.stream()
            .filter(row -> fieldId.equals(row[0]))
            .map(row -> (Long) row[1])
            .findFirst()
            .orElse(0L);
    }
    
    /**
     * Field statistics data class
     */
    @lombok.Builder
    @lombok.Data
    public static class FieldStatistics {
        private Integer fieldId;
        private String fieldName;
        private String fieldCode;
        private Long documentCount;
    }
}
