package com.alibou.security.booktype;

import com.alibou.security.exception.ResourceNotFoundException;
import com.alibou.security.user.Role;
import com.alibou.security.user.User;
import com.alibou.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookTypeService {

    private final BookTypeRepository bookTypeRepository;
    private final EditorBookTypePermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public Page<BookTypeResponse> getAllBookTypes(String search, Pageable pageable) {
        Page<BookType> bookTypes;
        
        if (search == null || search.trim().isEmpty()) {
            bookTypes = bookTypeRepository.findAll(pageable);
        } else {
            bookTypes = bookTypeRepository.findAllBySearchTerm(search.trim(), pageable);
        }
        
        return bookTypes.map(BookTypeResponse::fromEntity);
    }

    public Page<BookTypeResponse> getActiveBookTypes(String search, Pageable pageable) {
        Page<BookType> bookTypes;
        
        if (search == null || search.trim().isEmpty()) {
            bookTypes = bookTypeRepository.findAll(pageable);
        } else {
            bookTypes = bookTypeRepository.findBySearchTerm(search.trim(), pageable);
        }
        
        return bookTypes.map(BookTypeResponse::fromEntity);
    }

    public BookTypeResponse getBookTypeById(Integer id) {
        BookType bookType = bookTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + id));
        return BookTypeResponse.fromEntity(bookType);
    }

    public BookTypeResponse createBookType(BookTypeRequest request) {
        // Only ADMIN can create new book types
        checkAdminPermission();
        
        if (bookTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên loại sách đã tồn tại: " + request.getName());
        }

        BookType bookType = BookType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .category(request.getCategory())
                .colorCode(request.getColorCode())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        BookType savedBookType = bookTypeRepository.save(bookType);
        log.info("Created new book type: {} by user: {}", savedBookType.getName(), getCurrentUsername());
        
        return BookTypeResponse.fromEntity(savedBookType);
    }

    public BookTypeResponse updateBookType(Integer id, BookTypeRequest request) {
        BookType bookType = bookTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + id));

        // Check permission based on user role
        checkUpdatePermission(id);

        // Check if name is unique (excluding current record)
        if (!bookType.getName().equals(request.getName()) && 
            bookTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên loại sách đã tồn tại: " + request.getName());
        }

        bookType.setName(request.getName());
        bookType.setDescription(request.getDescription());
        bookType.setActive(request.getActive() != null ? request.getActive() : bookType.getActive());
        bookType.setCategory(request.getCategory());
        bookType.setColorCode(request.getColorCode());
        bookType.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : bookType.getSortOrder());

        BookType updatedBookType = bookTypeRepository.save(bookType);
        log.info("Updated book type: {} by user: {}", updatedBookType.getName(), getCurrentUsername());
        
        return BookTypeResponse.fromEntity(updatedBookType);
    }

    public void deleteBookType(Integer id) {
        BookType bookType = bookTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + id));

        // Check permission based on user role
        checkDeletePermission(id);

        bookTypeRepository.delete(bookType);
        log.info("Deleted book type: {} by user: {}", bookType.getName(), getCurrentUsername());
    }

    public List<BookTypeResponse> getBookTypesByCategory(String category) {
        List<BookType> bookTypes = bookTypeRepository.findByCategory(category);
        return bookTypes.stream()
                .map(BookTypeResponse::fromEntity)
                .toList();
    }

    public List<String> getAllCategories() {
        return bookTypeRepository.findAllDistinctCategories();
    }

    // Permission management methods
    public void grantEditorPermission(Integer editorId, Integer bookTypeId, boolean canEdit, boolean canDelete) {
        checkAdminPermission();
        
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy editor với ID: " + editorId));
        
        if (editor.getRole() != Role.EDITOR) {
            throw new IllegalArgumentException("User không phải là EDITOR");
        }
        
        BookType bookType = bookTypeRepository.findById(bookTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sách với ID: " + bookTypeId));

        Optional<EditorBookTypePermission> existingPermission = 
                permissionRepository.findByUserIdAndBookTypeId(editorId, bookTypeId);

        if (existingPermission.isPresent()) {
            EditorBookTypePermission permission = existingPermission.get();
            permission.setCanEdit(canEdit);
            permission.setCanDelete(canDelete);
            permission.setActive(true);
            permissionRepository.save(permission);
        } else {
            EditorBookTypePermission permission = EditorBookTypePermission.builder()
                    .user(editor)
                    .bookType(bookType)
                    .canEdit(canEdit)
                    .canDelete(canDelete)
                    .active(true)
                    .build();
            permissionRepository.save(permission);
        }

        log.info("Granted permission to editor {} for book type {}: canEdit={}, canDelete={}", 
                 editor.getUsername(), bookType.getName(), canEdit, canDelete);
    }

    public void revokeEditorPermission(Integer editorId, Integer bookTypeId) {
        checkAdminPermission();
        
        Optional<EditorBookTypePermission> permission = 
                permissionRepository.findByUserIdAndBookTypeId(editorId, bookTypeId);
        
        if (permission.isPresent()) {
            permission.get().setActive(false);
            permissionRepository.save(permission.get());
            log.info("Revoked permission for editor {} on book type {}", editorId, bookTypeId);
        }
    }

    public List<BookTypeResponse> getEditableBookTypesForCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return bookTypeRepository.findByActiveTrue().stream()
                    .map(BookTypeResponse::fromEntity)
                    .toList();
        } else if (currentUser.getRole() == Role.EDITOR) {
            List<BookType> editableBookTypes = permissionRepository.findEditableBookTypesByUserId(currentUser.getId());
            return editableBookTypes.stream()
                    .map(BookTypeResponse::fromEntity)
                    .toList();
        }
        return List.of();
    }

    // Helper methods for permission checking
    private void checkAdminPermission() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ ADMIN mới có quyền thực hiện thao tác này");
        }
    }

    private void checkUpdatePermission(Integer bookTypeId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admin can edit everything
        }
        
        if (currentUser.getRole() == Role.EDITOR) {
            boolean canEdit = permissionRepository.canUserEditBookType(currentUser.getId(), bookTypeId);
            if (!canEdit) {
                throw new AccessDeniedException("Bạn không có quyền chỉnh sửa loại sách này");
            }
            return;
        }
        
        throw new AccessDeniedException("Bạn không có quyền chỉnh sửa loại sách");
    }

    private void checkDeletePermission(Integer bookTypeId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admin can delete everything
        }
        
        if (currentUser.getRole() == Role.EDITOR) {
            boolean canDelete = permissionRepository.canUserDeleteBookType(currentUser.getId(), bookTypeId);
            if (!canDelete) {
                throw new AccessDeniedException("Bạn không có quyền xóa loại sách này");
            }
            return;
        }
        
        throw new AccessDeniedException("Bạn không có quyền xóa loại sách");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user hiện tại"));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
