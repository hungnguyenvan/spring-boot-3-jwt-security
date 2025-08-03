package com.alibou.security.document.web.controller;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.application.dto.DocumentHierarchySearchDto;
import com.alibou.security.document.application.service.TechnicalDocumentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Technical Document management
 */
@RestController
@RequestMapping("/api/v1/technical-documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Technical Documents", description = "APIs for managing technical documents")
public class TechnicalDocumentController {
    
    private final TechnicalDocumentApplicationService documentService;
    
    @GetMapping
    @Operation(summary = "Get documents by product", description = "Retrieve technical documents for a specific product")
    public ResponseEntity<Page<TechnicalDocumentDto>> getDocumentsByProduct(
            @Parameter(description = "Product ID") @RequestParam Integer productId,
            @PageableDefault(size = 20, sort = "sortOrder") Pageable pageable) {
        
        log.info("Getting documents for product ID: {}", productId);
        Page<TechnicalDocumentDto> documents = documentService.findByProduct(productId, pageable);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Advanced hierarchy search", description = "Search documents across the entire hierarchy")
    public ResponseEntity<Page<TechnicalDocumentDto>> searchDocuments(
            @Parameter(description = "Field name") @RequestParam(required = false) String fieldName,
            @Parameter(description = "Production year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Manufacturer name") @RequestParam(required = false) String manufacturerName,
            @Parameter(description = "Product series name") @RequestParam(required = false) String seriesName,
            @Parameter(description = "Product name") @RequestParam(required = false) String productName,
            @Parameter(description = "Document type") @RequestParam(required = false) String documentType,
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "downloadCount", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("Searching documents with hierarchy: field={}, year={}, manufacturer={}, series={}, product={}, type={}, query={}", 
                fieldName, year, manufacturerName, seriesName, productName, documentType, query);
        
        if (query != null && !query.trim().isEmpty()) {
            // Full text search
            Page<TechnicalDocumentDto> documents = documentService.searchDocuments(query, pageable);
            return ResponseEntity.ok(documents);
        } else {
            // Hierarchy search
            DocumentHierarchySearchDto searchDto = DocumentHierarchySearchDto.builder()
                .fieldName(fieldName)
                .year(year)
                .manufacturerName(manufacturerName)
                .seriesName(seriesName)
                .productName(productName)
                .documentType(documentType)
                .build();
            
            Page<TechnicalDocumentDto> documents = documentService.searchByHierarchy(searchDto, pageable);
            return ResponseEntity.ok(documents);
        }
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular documents", description = "Retrieve most downloaded documents")
    public ResponseEntity<List<TechnicalDocumentDto>> getPopularDocuments(
            @Parameter(description = "Number of documents to return") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting {} most popular documents", limit);
        List<TechnicalDocumentDto> documents = documentService.getMostPopular(limit);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/highly-rated")
    @Operation(summary = "Get highly rated documents", description = "Retrieve highest rated documents")
    public ResponseEntity<List<TechnicalDocumentDto>> getHighlyRatedDocuments(
            @Parameter(description = "Number of documents to return") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting {} highest rated documents", limit);
        List<TechnicalDocumentDto> documents = documentService.getHighestRated(limit);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent documents", description = "Retrieve recently added documents")
    public ResponseEntity<List<TechnicalDocumentDto>> getRecentDocuments(
            @Parameter(description = "Number of documents to return") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting {} most recent documents", limit);
        List<TechnicalDocumentDto> documents = documentService.getRecent(limit);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/by-type")
    @Operation(summary = "Get documents by type", description = "Retrieve documents by document type")
    public ResponseEntity<List<TechnicalDocumentDto>> getDocumentsByType(
            @Parameter(description = "Document type") @RequestParam String documentType) {
        
        log.info("Getting documents by type: {}", documentType);
        List<TechnicalDocumentDto> documents = documentService.findByDocumentType(documentType);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/by-category")
    @Operation(summary = "Get documents by category", description = "Retrieve documents by category")
    public ResponseEntity<List<TechnicalDocumentDto>> getDocumentsByCategory(
            @Parameter(description = "Document category") @RequestParam String category) {
        
        log.info("Getting documents by category: {}", category);
        List<TechnicalDocumentDto> documents = documentService.findByCategory(category);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/product/{productId}/types")
    @Operation(summary = "Get document types for product", description = "Get available document types for a product")
    public ResponseEntity<List<String>> getDocumentTypesForProduct(
            @Parameter(description = "Product ID") @PathVariable Integer productId) {
        
        log.info("Getting document types for product ID: {}", productId);
        List<String> types = documentService.getDocumentTypesByProduct(productId);
        return ResponseEntity.ok(types);
    }
    
    @GetMapping("/field/{fieldId}/categories")
    @Operation(summary = "Get categories for field", description = "Get available categories for a document field")
    public ResponseEntity<List<String>> getCategoriesForField(
            @Parameter(description = "Document field ID") @PathVariable Integer fieldId) {
        
        log.info("Getting categories for field ID: {}", fieldId);
        List<String> categories = documentService.getCategoriesByField(fieldId);
        return ResponseEntity.ok(categories);
    }
    
    @PostMapping("/{id}/view")
    @Operation(summary = "Record document view", description = "Increment view count for a document")
    public ResponseEntity<Void> recordView(
            @Parameter(description = "Document ID") @PathVariable Integer id) {
        
        log.info("Recording view for document ID: {}", id);
        documentService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/download")
    @Operation(summary = "Record document download", description = "Increment download count for a document")
    public ResponseEntity<Void> recordDownload(
            @Parameter(description = "Document ID") @PathVariable Integer id) {
        
        log.info("Recording download for document ID: {}", id);
        documentService.incrementDownloadCount(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate document", description = "Submit rating for a document")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Void> rateDocument(
            @Parameter(description = "Document ID") @PathVariable Integer id,
            @RequestBody Map<String, Double> rating) {
        
        Double ratingValue = rating.get("rating");
        if (ratingValue == null || ratingValue < 0 || ratingValue > 5) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Rating document ID: {} with rating: {}", id, ratingValue);
        documentService.updateRating(id, ratingValue);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get document statistics", description = "Retrieve overall document statistics")
    public ResponseEntity<TechnicalDocumentApplicationService.DocumentStatistics> getStatistics() {
        
        log.info("Getting document statistics");
        TechnicalDocumentApplicationService.DocumentStatistics stats = documentService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
