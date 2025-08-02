package com.alibou.security.booktype;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-types")
@RequiredArgsConstructor
@Slf4j
public class BookTypeController {

    private final BookTypeService bookTypeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('USER')")
    public ResponseEntity<Page<BookTypeResponse>> getAllBookTypes(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<BookTypeResponse> bookTypes = activeOnly 
                ? bookTypeService.getActiveBookTypes(search, pageRequest)
                : bookTypeService.getAllBookTypes(search, pageRequest);
        
        return ResponseEntity.ok(bookTypes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('USER')")
    public ResponseEntity<BookTypeResponse> getBookTypeById(@PathVariable Integer id) {
        BookTypeResponse bookType = bookTypeService.getBookTypeById(id);
        return ResponseEntity.ok(bookType);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookTypeResponse> createBookType(@Valid @RequestBody BookTypeRequest request) {
        BookTypeResponse bookType = bookTypeService.createBookType(request);
        log.info("Created book type: {}", bookType.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookType);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<BookTypeResponse> updateBookType(
            @PathVariable Integer id,
            @Valid @RequestBody BookTypeRequest request
    ) {
        BookTypeResponse bookType = bookTypeService.updateBookType(id, request);
        log.info("Updated book type: {}", bookType.getName());
        return ResponseEntity.ok(bookType);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<Void> deleteBookType(@PathVariable Integer id) {
        bookTypeService.deleteBookType(id);
        log.info("Deleted book type with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('USER')")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = bookTypeService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('USER')")
    public ResponseEntity<List<BookTypeResponse>> getBookTypesByCategory(@PathVariable String category) {
        List<BookTypeResponse> bookTypes = bookTypeService.getBookTypesByCategory(category);
        return ResponseEntity.ok(bookTypes);
    }

    @GetMapping("/my-editable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<List<BookTypeResponse>> getMyEditableBookTypes() {
        List<BookTypeResponse> bookTypes = bookTypeService.getEditableBookTypesForCurrentUser();
        return ResponseEntity.ok(bookTypes);
    }

    // Admin-only endpoints for permission management
    @PostMapping("/permissions/{editorId}/{bookTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> grantEditorPermission(
            @PathVariable Integer editorId,
            @PathVariable Integer bookTypeId,
            @RequestParam(defaultValue = "true") boolean canEdit,
            @RequestParam(defaultValue = "false") boolean canDelete
    ) {
        bookTypeService.grantEditorPermission(editorId, bookTypeId, canEdit, canDelete);
        return ResponseEntity.ok("Đã cấp quyền thành công");
    }

    @DeleteMapping("/permissions/{editorId}/{bookTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> revokeEditorPermission(
            @PathVariable Integer editorId,
            @PathVariable Integer bookTypeId
    ) {
        bookTypeService.revokeEditorPermission(editorId, bookTypeId);
        return ResponseEntity.ok("Đã thu hồi quyền thành công");
    }
}
