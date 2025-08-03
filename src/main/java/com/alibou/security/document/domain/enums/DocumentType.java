package com.alibou.security.document.domain.enums;

/**
 * Enumeration for document types in the library system
 */
public enum DocumentType {
    MANUAL("Manual", "User manuals and instruction guides"),
    SCHEMATIC("Schematic", "Technical schematics and diagrams"),
    SPECIFICATION("Specification", "Product specifications and technical data"),
    REPAIR_GUIDE("Repair Guide", "Repair and maintenance guides"),
    PARTS_CATALOG("Parts Catalog", "Parts lists and catalogs"),
    SERVICE_BULLETIN("Service Bulletin", "Service bulletins and updates"),
    WIRING_DIAGRAM("Wiring Diagram", "Electrical wiring diagrams"),
    TECHNICAL_NOTE("Technical Note", "Technical notes and advisories"),
    SAFETY_SHEET("Safety Sheet", "Safety information and data sheets"),
    INSTALLATION_GUIDE("Installation Guide", "Installation instructions"),
    TROUBLESHOOTING("Troubleshooting", "Troubleshooting guides"),
    FIRMWARE("Firmware", "Firmware and software documentation"),
    CALIBRATION("Calibration", "Calibration procedures"),
    TEST_PROCEDURE("Test Procedure", "Testing procedures and protocols"),
    TRAINING_MATERIAL("Training Material", "Training and educational materials"),
    OTHER("Other", "Other document types");
    
    private final String displayName;
    private final String description;
    
    DocumentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get DocumentType from string value (case insensitive)
     */
    public static DocumentType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (DocumentType type : DocumentType.values()) {
            if (type.name().equalsIgnoreCase(value) || 
                type.getDisplayName().equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Invalid document type: " + value);
    }
    
    /**
     * Check if value is valid document type
     */
    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
