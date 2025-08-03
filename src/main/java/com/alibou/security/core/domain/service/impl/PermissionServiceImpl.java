package com.alibou.security.core.domain.service.impl;

import com.alibou.security.core.domain.entity.BookTypePermission;
import com.alibou.security.core.domain.service.PermissionService;
import com.alibou.security.user.Role;
import com.alibou.security.user.User;
import com.alibou.security.book.BookRepository;
import com.alibou.security.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of permission service with business logic
 * for library management authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final BookRepository bookRepository;
    // Note: We'll need to inject EditorBookTypePermissionRepository when available

    @Override
    public boolean canEditBookType(User user, Integer bookTypeId) {
        log.debug("Checking edit permission for user {} on book type {}", user.getId(), bookTypeId);
        
        // Admin can edit everything
        if (hasAdminAccess(user)) {
            return true;
        }
        
        // Editor needs specific permission
        if (hasEditorAccess(user)) {
            // TODO: Check EditorBookTypePermission table
            // For now, return true for editors
            return true;
        }
        
        return false;
    }

    @Override
    public boolean canDeleteBookType(User user, Integer bookTypeId) {
        log.debug("Checking delete permission for user {} on book type {}", user.getId(), bookTypeId);
        
        // Admin can delete everything
        if (hasAdminAccess(user)) {
            return true;
        }
        
        // Editor needs specific delete permission
        if (hasEditorAccess(user)) {
            // TODO: Check EditorBookTypePermission table for delete permission
            // For now, return false for editors (safer default)
            return false;
        }
        
        return false;
    }

    @Override
    public boolean canViewBookContent(User user, Integer bookId) {
        log.debug("Checking view content permission for user {} on book {}", user.getId(), bookId);
        
        // Admin can view everything
        if (hasAdminAccess(user)) {
            return true;
        }
        
        // Check if book exists and is free
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return false;
        }
        
        // Free books can be viewed by anyone
        if (book.getIsFree()) {
            return true;
        }
        
        // TODO: Check if user has purchased the book
        // For now, editors can view paid content
        return hasEditorAccess(user);
    }

    @Override
    public boolean canDownloadBook(User user, Integer bookId) {
        log.debug("Checking download permission for user {} on book {}", user.getId(), bookId);
        
        // Admin can download everything
        if (hasAdminAccess(user)) {
            return true;
        }
        
        // Check if book exists and is downloadable
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || !book.getDownloadable()) {
            return false;
        }
        
        // Check if user can view content first
        if (!canViewBookContent(user, bookId)) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean canManageUserProfile(User currentUser, Integer targetUserId) {
        log.debug("Checking user management permission for user {} on target user {}", 
                 currentUser.getId(), targetUserId);
        
        // Admin can manage anyone
        if (hasAdminAccess(currentUser)) {
            return true;
        }
        
        // Users can manage their own profile
        return currentUser.getId().equals(targetUserId);
    }

    @Override
    public boolean hasAdminAccess(User user) {
        return user.getRole() == Role.ADMIN;
    }

    @Override
    public boolean hasEditorAccess(User user) {
        return user.getRole() == Role.EDITOR || user.getRole() == Role.ADMIN;
    }

    @Override
    public BookTypePermission getBookTypePermission(User user, Integer bookTypeId) {
        log.debug("Getting book type permissions for user {} on book type {}", 
                 user.getId(), bookTypeId);
        
        boolean canEdit = canEditBookType(user, bookTypeId);
        boolean canDelete = canDeleteBookType(user, bookTypeId);
        boolean canView = hasEditorAccess(user); // Editors and admins can view book types
        
        return new BookTypePermission(canEdit, canDelete, canView);
    }

    @Override
    public void grantEditorPermission(Integer userId, Integer bookTypeId, boolean canEdit, boolean canDelete) {
        log.info("Granting editor permission for user {} on book type {} (edit: {}, delete: {})", 
                userId, bookTypeId, canEdit, canDelete);
        
        // TODO: Implement with EditorBookTypePermissionRepository
        // Create or update permission record
    }

    @Override
    public void revokeEditorPermission(Integer userId, Integer bookTypeId) {
        log.info("Revoking editor permission for user {} on book type {}", userId, bookTypeId);
        
        // TODO: Implement with EditorBookTypePermissionRepository
        // Delete or deactivate permission record
    }
}
