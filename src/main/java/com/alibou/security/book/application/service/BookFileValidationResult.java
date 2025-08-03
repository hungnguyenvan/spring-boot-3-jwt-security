package com.alibou.security.book.application.service;

import java.util.List;

/**
 * Book file validation result
 */
public class BookFileValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final BookFileMetadata extractedMetadata;
    
    public BookFileValidationResult(boolean valid, List<String> errors, List<String> warnings, 
                                  BookFileMetadata extractedMetadata) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.extractedMetadata = extractedMetadata;
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public BookFileMetadata getExtractedMetadata() { return extractedMetadata; }
}
