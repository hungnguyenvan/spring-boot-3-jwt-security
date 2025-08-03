package com.alibou.security.core.domain.service;

/**
 * Permission result for book type operations
 */
public class BookTypePermission {
    private final boolean canEdit;
    private final boolean canDelete;
    private final boolean canView;
    
    public BookTypePermission(boolean canEdit, boolean canDelete, boolean canView) {
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canView = canView;
    }
    
    public boolean canEdit() {
        return canEdit;
    }
    
    public boolean canDelete() {
        return canDelete;
    }
    
    public boolean canView() {
        return canView;
    }
    
    @Override
    public String toString() {
        return String.format("BookTypePermission{canEdit=%s, canDelete=%s, canView=%s}", 
                           canEdit, canDelete, canView);
    }
}
