package com.alibou.security.core.domain.service;

import java.util.List;

/**
 * File validation result
 */
public class FileValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    public FileValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
}
