package com.bincard.bincard_superadmin.model;

import java.time.LocalDateTime;

/**
 * Kimlik doğrulama isteği DTO
 */
public class IdentityVerificationRequestDTO {
    private Long id;
    private UserIdentityInfoDTO identityInfo;
    private String requestedByPhone;
    private LocalDateTime requestedAt;
    private RequestStatus status;
    private String adminNote;
    private String reviewedByPhone;
    private LocalDateTime reviewedAt;
    
    public IdentityVerificationRequestDTO() {}
    
    public IdentityVerificationRequestDTO(Long id, UserIdentityInfoDTO identityInfo, 
                                          String requestedByPhone, LocalDateTime requestedAt,
                                          RequestStatus status, String adminNote, 
                                          String reviewedByPhone, LocalDateTime reviewedAt) {
        this.id = id;
        this.identityInfo = identityInfo;
        this.requestedByPhone = requestedByPhone;
        this.requestedAt = requestedAt;
        this.status = status;
        this.adminNote = adminNote;
        this.reviewedByPhone = reviewedByPhone;
        this.reviewedAt = reviewedAt;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserIdentityInfoDTO getIdentityInfo() { return identityInfo; }
    public void setIdentityInfo(UserIdentityInfoDTO identityInfo) { this.identityInfo = identityInfo; }
    
    public String getRequestedByPhone() { return requestedByPhone; }
    public void setRequestedByPhone(String requestedByPhone) { this.requestedByPhone = requestedByPhone; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    
    public String getReviewedByPhone() { return reviewedByPhone; }
    public void setReviewedByPhone(String reviewedByPhone) { this.reviewedByPhone = reviewedByPhone; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
