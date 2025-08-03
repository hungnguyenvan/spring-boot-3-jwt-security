package com.alibou.security.document.application.service;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.application.dto.DocumentHierarchySearchDto;
import com.alibou.security.document.application.mapper.TechnicalDocumentMapper;
import com.alibou.security.document.domain.entity.TechnicalDocument;
import com.alibou.security.document.domain.repository.TechnicalDocumentRepository;
import com.alibou.security.core.application.service.impl.BaseApplicationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for TechnicalDocument management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TechnicalDocumentApplicationService {
    
    private final TechnicalDocumentRepository technicalDocumentRepository;
    private final TechnicalDocumentMapper technicalDocumentMapper;
    
    protected TechnicalDocumentRepository getRepository() {
        return technicalDocumentRepository;
    }
    
    protected TechnicalDocumentMapper getMapper() {
        return technicalDocumentMapper;
    }
    
    /**
     * Find documents by product
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> findByProduct(Integer productId) {
        log.debug("Finding technical documents by product ID: {}", productId);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findByProductIdAndActiveTrueOrderBySortOrder(productId);
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Find documents by product with pagination
     */
    @Transactional(readOnly = true)
    public Page<TechnicalDocumentDto> findByProduct(Integer productId, Pageable pageable) {
        log.debug("Finding technical documents by product ID: {} with pagination", productId);
        Page<TechnicalDocument> documents = technicalDocumentRepository
            .findByProductIdAndActiveTrue(productId, pageable);
        return documents.map(technicalDocumentMapper::toDto);
    }
    
    /**
     * Find documents by document type
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> findByDocumentType(String documentType) {
        log.debug("Finding technical documents by type: {}", documentType);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findByDocumentTypeAndActiveTrueOrderBySortOrder(documentType);
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Find documents by category
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> findByCategory(String category) {
        log.debug("Finding technical documents by category: {}", category);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findByCategoryAndActiveTrueOrderBySortOrder(category);
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Complex hierarchy search
     */
    @Transactional(readOnly = true)
    public Page<TechnicalDocumentDto> searchByHierarchy(DocumentHierarchySearchDto searchDto, Pageable pageable) {
        log.debug("Searching technical documents by hierarchy: {}", searchDto);
        Page<TechnicalDocument> documents = technicalDocumentRepository.findByHierarchy(
            searchDto.getFieldName(),
            searchDto.getYear(),
            searchDto.getManufacturerName(),
            searchDto.getSeriesName(),
            searchDto.getProductName(),
            searchDto.getDocumentType(),
            pageable
        );
        return documents.map(technicalDocumentMapper::toDto);
    }
    
    /**
     * Full text search across documents
     */
    @Transactional(readOnly = true)
    public Page<TechnicalDocumentDto> searchDocuments(String query, Pageable pageable) {
        log.debug("Full text search for documents with query: {}", query);
        Page<TechnicalDocument> documents = technicalDocumentRepository
            .searchDocuments(query, pageable);
        return documents.map(technicalDocumentMapper::toDto);
    }
    
    /**
     * Get most popular documents
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> getMostPopular(int limit) {
        log.debug("Getting {} most popular documents", limit);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findMostPopular(Pageable.ofSize(limit));
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Get highest rated documents
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> getHighestRated(int limit) {
        log.debug("Getting {} highest rated documents", limit);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findHighestRated(Pageable.ofSize(limit));
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Get recent documents
     */
    @Transactional(readOnly = true)
    public List<TechnicalDocumentDto> getRecent(int limit) {
        log.debug("Getting {} most recent documents", limit);
        List<TechnicalDocument> documents = technicalDocumentRepository
            .findRecent(Pageable.ofSize(limit));
        return technicalDocumentMapper.toDto(documents);
    }
    
    /**
     * Get document types for a product
     */
    @Transactional(readOnly = true)
    public List<String> getDocumentTypesByProduct(Integer productId) {
        log.debug("Getting document types for product ID: {}", productId);
        return technicalDocumentRepository.findDocumentTypesByProduct(productId);
    }
    
    /**
     * Get categories for a field
     */
    @Transactional(readOnly = true)
    public List<String> getCategoriesByField(Integer fieldId) {
        log.debug("Getting categories for field ID: {}", fieldId);
        return technicalDocumentRepository.findCategoriesByField(fieldId);
    }
    
    /**
     * Increment view count
     */
    public void incrementViewCount(Integer documentId) {
        log.debug("Incrementing view count for document ID: {}", documentId);
        Optional<TechnicalDocument> documentOpt = technicalDocumentRepository.findById(documentId);
        if (documentOpt.isPresent()) {
            TechnicalDocument document = documentOpt.get();
            document.incrementViewCount();
            technicalDocumentRepository.save(document);
        }
    }
    
    /**
     * Increment download count
     */
    public void incrementDownloadCount(Integer documentId) {
        log.debug("Incrementing download count for document ID: {}", documentId);
        Optional<TechnicalDocument> documentOpt = technicalDocumentRepository.findById(documentId);
        if (documentOpt.isPresent()) {
            TechnicalDocument document = documentOpt.get();
            document.incrementDownloadCount();
            technicalDocumentRepository.save(document);
        }
    }
    
    /**
     * Update rating
     */
    public void updateRating(Integer documentId, Double rating) {
        log.debug("Updating rating for document ID: {} to {}", documentId, rating);
        Optional<TechnicalDocument> documentOpt = technicalDocumentRepository.findById(documentId);
        if (documentOpt.isPresent()) {
            TechnicalDocument document = documentOpt.get();
            document.updateRating(java.math.BigDecimal.valueOf(rating));
            technicalDocumentRepository.save(document);
        }
    }
    
    /**
     * Get document statistics
     */
    @Transactional(readOnly = true)
    public DocumentStatistics getStatistics() {
        log.debug("Getting document statistics");
        return DocumentStatistics.builder()
            .totalActive(technicalDocumentRepository.countAllActive())
            .totalPublic(technicalDocumentRepository.countPublic())
            .totalDownloads(Optional.ofNullable(technicalDocumentRepository.getTotalDownloads()).orElse(0L))
            .totalViews(Optional.ofNullable(technicalDocumentRepository.getTotalViews()).orElse(0L))
            .averageRating(Optional.ofNullable(technicalDocumentRepository.getAverageRating()).orElse(0.0))
            .build();
    }
    
    /**
     * Document statistics data class
     */
    @lombok.Builder
    @lombok.Data
    public static class DocumentStatistics {
        private long totalActive;
        private long totalPublic;
        private long totalDownloads;
        private long totalViews;
        private double averageRating;
    }
}
