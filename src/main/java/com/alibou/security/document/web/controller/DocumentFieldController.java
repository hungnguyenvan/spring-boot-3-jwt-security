package com.alibou.security.document.web.controller;

import com.alibou.security.document.application.dto.DocumentFieldDto;
import com.alibou.security.document.application.service.DocumentFieldApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for Document Field management
 */
@RestController
@RequestMapping("/api/v1/document-fields")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Fields", description = "APIs for managing document fields/categories")
public class DocumentFieldController {
    
    private final DocumentFieldApplicationService fieldService;
    
    @GetMapping
    @Operation(summary = "Get all active document fields", description = "Retrieve all active document fields ordered by sort order")
    public ResponseEntity<List<DocumentFieldDto>> getAllFields() {
        
        log.info("Getting all active document fields");
        List<DocumentFieldDto> fields = fieldService.findAllActive();
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search document fields", description = "Search document fields by name or description")
    public ResponseEntity<Page<DocumentFieldDto>> searchFields(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20, sort = "sortOrder") Pageable pageable) {
        
        log.info("Searching document fields with query: {}", query);
        Page<DocumentFieldDto> fields = fieldService.searchByNameOrDescription(query, pageable);
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/by-name/{name}")
    @Operation(summary = "Get field by name", description = "Retrieve document field by name")
    public ResponseEntity<DocumentFieldDto> getFieldByName(
            @Parameter(description = "Field name") @PathVariable String name) {
        
        log.info("Getting document field by name: {}", name);
        return fieldService.findByName(name)
            .map(field -> ResponseEntity.ok(field))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-code/{code}")
    @Operation(summary = "Get field by code", description = "Retrieve document field by code")
    public ResponseEntity<DocumentFieldDto> getFieldByCode(
            @Parameter(description = "Field code") @PathVariable String code) {
        
        log.info("Getting document field by code: {}", code);
        return fieldService.findByCode(code)
            .map(field -> ResponseEntity.ok(field))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-year/{year}")
    @Operation(summary = "Get fields by year", description = "Get document fields that have products for a specific year")
    public ResponseEntity<List<DocumentFieldDto>> getFieldsByYear(
            @Parameter(description = "Production year") @PathVariable Integer year) {
        
        log.info("Getting document fields by year: {}", year);
        List<DocumentFieldDto> fields = fieldService.findByYear(year);
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/by-manufacturer")
    @Operation(summary = "Get fields by manufacturer", description = "Get document fields by manufacturer name")
    public ResponseEntity<List<DocumentFieldDto>> getFieldsByManufacturer(
            @Parameter(description = "Manufacturer name") @RequestParam String manufacturerName) {
        
        log.info("Getting document fields by manufacturer: {}", manufacturerName);
        List<DocumentFieldDto> fields = fieldService.findByManufacturerName(manufacturerName);
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/with-years")
    @Operation(summary = "Get fields with production years", description = "Get document fields with their production years loaded")
    public ResponseEntity<List<DocumentFieldDto>> getFieldsWithYears() {
        
        log.info("Getting document fields with production years");
        List<DocumentFieldDto> fields = fieldService.findAllWithProductionYears();
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get field statistics", description = "Get document statistics for all fields")
    public ResponseEntity<List<DocumentFieldApplicationService.FieldStatistics>> getFieldStatistics() {
        
        log.info("Getting document statistics for all fields");
        List<DocumentFieldApplicationService.FieldStatistics> statistics = fieldService.getDocumentStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping
    @Operation(summary = "Create new document field", description = "Create a new document field")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DocumentFieldDto> createField(
            @Valid @RequestBody DocumentFieldDto fieldDto) {
        
        log.info("Creating new document field: {}", fieldDto.getName());
        DocumentFieldDto created = fieldService.create(fieldDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update document field", description = "Update an existing document field")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DocumentFieldDto> updateField(
            @Parameter(description = "Field ID") @PathVariable Integer id,
            @Valid @RequestBody DocumentFieldDto fieldDto) {
        
        log.info("Updating document field ID: {}", id);
        DocumentFieldDto updated = fieldService.update(id, fieldDto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document field", description = "Soft delete a document field")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteField(
            @Parameter(description = "Field ID") @PathVariable Integer id) {
        
        log.info("Deleting document field ID: {}", id);
        fieldService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
