package com.bincard.bincard_superadmin.model;

/**
 * Kimlik doğrulama isteği işleme isteği
 */
public class ProcessIdentityRequest {
    private Long requestId;
    private boolean approved;
    private String adminNote;
    
    public ProcessIdentityRequest() {}
    
    public ProcessIdentityRequest(Long requestId, boolean approved, String adminNote) {
        this.requestId = requestId;
        this.approved = approved;
        this.adminNote = adminNote;
    }
    
    // Getters and setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
