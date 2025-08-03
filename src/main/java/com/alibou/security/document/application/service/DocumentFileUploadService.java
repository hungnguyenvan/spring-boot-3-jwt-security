package com.alibou.security.document.application.service;

import com.alibou.security.document.application.dto.TechnicalDocumentDto;
import com.alibou.security.document.application.mapper.TechnicalDocumentMapper;
import com.alibou.security.document.domain.entity.TechnicalDocument;
import com.alibou.security.document.domain.entity.Product;
import com.alibou.security.document.domain.repository.TechnicalDocumentRepository;
import com.alibou.security.document.domain.repository.ProductRepository;
import com.alibou.security.document.infrastructure.web.dto.FileUploadResponse;
import com.alibou.security.document.infrastructure.web.dto.CreateDocumentRequest;
import com.alibou.security.document.infrastructure.web.dto.UpdateDocumentMetadataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling document file uploads and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentFileUploadService {
    
    private final TechnicalDocumentRepository documentRepository;
    private final ProductRepository productRepository;
    private final TechnicalDocumentMapper documentMapper;
    
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.upload.temp-dir:./uploads/temp}")
    private String tempUploadDir;
    
    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${app.upload.allowed-extensions:pdf,doc,docx,xls,xlsx,ppt,pptx,dwg,png,jpg,jpeg}")
    private String allowedExtensions;
    
    // Temporary file storage (in production, use Redis or database)
    private final Map<String, TempFileInfo> tempFiles = new ConcurrentHashMap<>();
    
    /**
     * Step 1: Upload file to temporary location
     */
    public FileUploadResponse uploadTemporaryFile(MultipartFile file) throws IOException {
        log.info("Uploading temporary file: {}", file.getOriginalFilename());
        
        // Validate file
        validateFile(file);
        
        // Generate temporary file ID
        String tempFileId = UUID.randomUUID().toString();
        
        // Create temp directory if not exists
        Path tempDir = Paths.get(tempUploadDir);
        Files.createDirectories(tempDir);
        
        // Save file to temporary location
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String tempFileName = tempFileId + "." + fileExtension;
        Path tempFilePath = tempDir.resolve(tempFileName);
        
        Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Calculate checksum
        String checksum = calculateChecksum(tempFilePath);
        
        // Store temporary file info
        TempFileInfo tempFileInfo = TempFileInfo.builder()
            .tempFileId(tempFileId)
            .originalFileName(file.getOriginalFilename())
            .tempFilePath(tempFilePath.toString())
            .fileSize(file.getSize())
            .fileFormat(fileExtension.toUpperCase())
            .checksum(checksum)
            .uploadedAt(LocalDateTime.now())
            .build();
            
        tempFiles.put(tempFileId, tempFileInfo);
        
        // Schedule cleanup (remove after 1 hour if not used)
        scheduleCleanup(tempFileId);
        
        return FileUploadResponse.success(
            tempFileId,
            file.getOriginalFilename(),
            fileExtension.toUpperCase(),
            file.getSize(),
            checksum
        );
    }
    
    /**
     * Step 2: Create document with metadata and move file to final location
     */
    public TechnicalDocumentDto createDocumentWithFile(CreateDocumentRequest request) throws IOException {
        log.info("Creating document with file: {}", request.getTemporaryFileId());
        
        // Get temporary file info
        TempFileInfo tempFileInfo = tempFiles.get(request.getTemporaryFileId());
        if (tempFileInfo == null) {
            throw new IllegalArgumentException("Temporary file not found: " + request.getTemporaryFileId());
        }
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));
        
        // Create final file path based on hierarchy
        String finalFilePath = generateFinalFilePath(product, request.getTitle(), tempFileInfo.getFileFormat());
        
        // Move file from temp to final location
        Path finalPath = Paths.get(uploadDir, finalFilePath);
        Files.createDirectories(finalPath.getParent());
        Files.move(Paths.get(tempFileInfo.getTempFilePath()), finalPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create technical document entity
        TechnicalDocument document = TechnicalDocument.builder()
            .title(request.getTitle())
            .documentType(request.getDocumentType().name())
            .description(request.getDescription())
            .category(request.getCategory())
            .subCategory(request.getSubCategory())
            .product(product)
            .fileName(tempFileInfo.getOriginalFileName())
            .filePath(finalFilePath)
            .fileFormat(tempFileInfo.getFileFormat())
            .fileSize(tempFileInfo.getFileSize())
            .checksum(tempFileInfo.getChecksum())
            .version(request.getVersion() != null ? request.getVersion() : "v1.0")
            .language(request.getLanguage() != null ? request.getLanguage() : "EN")
            .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
            .downloadable(request.getDownloadable() != null ? request.getDownloadable() : true)
            .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
            .build();
        
        // Save document
        TechnicalDocument saved = documentRepository.save(document);
        
        // Remove from temporary storage
        tempFiles.remove(request.getTemporaryFileId());
        
        return documentMapper.toDto(saved);
    }
    
    /**
     * Alternative: Upload and create document in one step
     */
    public TechnicalDocumentDto uploadAndCreateDocument(MultipartFile file, CreateDocumentRequest request) throws IOException {
        log.info("Uploading and creating document: {}", request.getTitle());
        
        // Upload to temporary location first
        FileUploadResponse uploadResponse = uploadTemporaryFile(file);
        
        // Set temporary file ID in request
        request.setTemporaryFileId(uploadResponse.getTemporaryFileId());
        
        // Create document with file
        return createDocumentWithFile(request);
    }
    
    /**
     * Get temporary file information
     */
    @Transactional(readOnly = true)
    public FileUploadResponse getTemporaryFileInfo(String tempFileId) {
        TempFileInfo tempFileInfo = tempFiles.get(tempFileId);
        if (tempFileInfo == null) {
            throw new IllegalArgumentException("Temporary file not found: " + tempFileId);
        }
        
        return FileUploadResponse.success(
            tempFileInfo.getTempFileId(),
            tempFileInfo.getOriginalFileName(),
            tempFileInfo.getFileFormat(),
            tempFileInfo.getFileSize(),
            tempFileInfo.getChecksum()
        );
    }
    
    /**
     * Delete temporary file
     */
    public void deleteTemporaryFile(String tempFileId) throws IOException {
        TempFileInfo tempFileInfo = tempFiles.remove(tempFileId);
        if (tempFileInfo != null) {
            Files.deleteIfExists(Paths.get(tempFileInfo.getTempFilePath()));
            log.info("Deleted temporary file: {}", tempFileId);
        }
    }
    
    /**
     * Update document metadata without changing file
     */
    public TechnicalDocumentDto updateDocumentMetadata(Integer documentId, UpdateDocumentMetadataRequest request) {
        log.info("Updating metadata for document: {}", documentId);
        
        TechnicalDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        
        // Update fields
        if (request.getTitle() != null) document.setTitle(request.getTitle());
        if (request.getDocumentType() != null) document.setDocumentType(request.getDocumentType().name());
        if (request.getDescription() != null) document.setDescription(request.getDescription());
        if (request.getCategory() != null) document.setCategory(request.getCategory());
        if (request.getSubCategory() != null) document.setSubCategory(request.getSubCategory());
        if (request.getVersion() != null) document.setVersion(request.getVersion());
        if (request.getLanguage() != null) document.setLanguage(request.getLanguage());
        if (request.getIsPublic() != null) document.setIsPublic(request.getIsPublic());
        if (request.getDownloadable() != null) document.setDownloadable(request.getDownloadable());
        if (request.getSortOrder() != null) document.setSortOrder(request.getSortOrder());
        
        TechnicalDocument updated = documentRepository.save(document);
        return documentMapper.toDto(updated);
    }
    
    /**
     * Replace document file while keeping metadata
     */
    public TechnicalDocumentDto replaceDocumentFile(Integer documentId, MultipartFile newFile) throws IOException {
        log.info("Replacing file for document: {}", documentId);
        
        TechnicalDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        
        // Validate new file
        validateFile(newFile);
        
        // Delete old file
        Path oldFilePath = Paths.get(uploadDir, document.getFilePath());
        Files.deleteIfExists(oldFilePath);
        
        // Generate new file path
        String fileExtension = getFileExtension(newFile.getOriginalFilename());
        String newFilePath = generateFinalFilePath(document.getProduct(), document.getTitle(), fileExtension);
        
        // Save new file
        Path finalPath = Paths.get(uploadDir, newFilePath);
        Files.createDirectories(finalPath.getParent());
        Files.copy(newFile.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Calculate new checksum
        String newChecksum = calculateChecksum(finalPath);
        
        // Update document
        document.setFileName(newFile.getOriginalFilename());
        document.setFilePath(newFilePath);
        document.setFileFormat(fileExtension.toUpperCase());
        document.setFileSize(newFile.getSize());
        document.setChecksum(newChecksum);
        
        TechnicalDocument updated = documentRepository.save(document);
        return documentMapper.toDto(updated);
    }
    
    /**
     * Get upload statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUploadStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Document statistics
        long totalDocuments = documentRepository.countAllActive();
        Long totalFileSize = documentRepository.getTotalFileSize();
        
        // Temporary file statistics
        int tempFileCount = tempFiles.size();
        long tempTotalSize = tempFiles.values().stream()
            .mapToLong(TempFileInfo::getFileSize)
            .sum();
        
        stats.put("totalDocuments", totalDocuments);
        stats.put("totalFileSize", totalFileSize != null ? totalFileSize : 0);
        stats.put("tempFileCount", tempFileCount);
        stats.put("tempTotalSize", tempTotalSize);
        stats.put("allowedExtensions", allowedExtensions.split(","));
        stats.put("maxFileSize", maxFileSize);
        
        return stats;
    }
    
    // ==========================================================
    // PRIVATE HELPER METHODS
    // ==========================================================
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedExtension(fileExtension)) {
            throw new IllegalArgumentException("File extension not allowed: " + fileExtension);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private boolean isAllowedExtension(String extension) {
        String[] allowed = allowedExtensions.toLowerCase().split(",");
        for (String ext : allowed) {
            if (ext.trim().equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    private String generateFinalFilePath(Product product, String documentTitle, String fileExtension) {
        // Get hierarchy path
        String hierarchyPath = product.getHierarchyPath();
        
        // Clean document title for filename
        String cleanTitle = documentTitle.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
        
        // Generate path: field/year/manufacturer/series/product/documentTitle.ext
        return hierarchyPath.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9/_]", "") 
            + "/" + cleanTitle + "." + fileExtension.toLowerCase();
    }
    
    private String calculateChecksum(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return "sha256:" + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private void scheduleCleanup(String tempFileId) {
        // In production, use @Scheduled method or job queue
        // For now, simple cleanup after 1 hour
        new Thread(() -> {
            try {
                Thread.sleep(3600000); // 1 hour
                deleteTemporaryFile(tempFileId);
            } catch (Exception e) {
                log.warn("Error during scheduled cleanup of temp file: {}", tempFileId, e);
            }
        }).start();
    }
}

/**
 * Temporary file information
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class TempFileInfo {
    private String tempFileId;
    private String originalFileName;
    private String tempFilePath;
    private Long fileSize;
    private String fileFormat;
    private String checksum;
    private LocalDateTime uploadedAt;
}
