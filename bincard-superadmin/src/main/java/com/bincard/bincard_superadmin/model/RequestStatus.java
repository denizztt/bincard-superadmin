package com.bincard.bincard_superadmin.model;

/**
 * Kimlik doğrulama isteği durumları
 */
public enum RequestStatus {
    PENDING("Beklemede"),
    APPROVED("Onaylandı"),
    REJECTED("Reddedildi");
    
    private final String displayName;
    
    RequestStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
