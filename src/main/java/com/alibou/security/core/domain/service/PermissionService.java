package com.alibou.security.core.domain.service;

import com.alibou.security.user.User;

/**
 * Domain service for permission checking across the library system
 * Encapsulates complex business rules for authorization
 */
public interface PermissionService {
    
    /**
     * Check if user can edit books of specific type
     */
    boolean canEditBookType(User user, Integer bookTypeId);
    
    /**
     * Check if user can delete books of specific type
     */
    boolean canDeleteBookType(User user, Integer bookTypeId);
    
    /**
     * Check if user can view book content (for paid content)
     */
    boolean canViewBookContent(User user, Integer bookId);
    
    /**
     * Check if user can download book
     */
    boolean canDownloadBook(User user, Integer bookId);
    
    /**
     * Check if user can manage user profiles
     */
    boolean canManageUserProfile(User currentUser, Integer targetUserId);
    
    /**
     * Check if user can access admin features
     */
    boolean hasAdminAccess(User user);
    
    /**
     * Check if user can access editor features
     */
    boolean hasEditorAccess(User user);
    
    /**
     * Get user's effective permissions for a book type
     */
    BookTypePermission getBookTypePermission(User user, Integer bookTypeId);
    
    /**
     * Grant editor permission for specific book type
     */
    void grantEditorPermission(Integer userId, Integer bookTypeId, boolean canEdit, boolean canDelete);
    
    /**
     * Revoke editor permission for specific book type
     */
    void revokeEditorPermission(Integer userId, Integer bookTypeId);
}
