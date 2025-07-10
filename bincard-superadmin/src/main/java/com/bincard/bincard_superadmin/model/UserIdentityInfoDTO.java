package com.bincard.bincard_superadmin.model;

/**
 * Kimlik bilgilerini temsil eden DTO
 */
public class UserIdentityInfoDTO {
    private Long id;
    private String frontCardPhoto;
    private String backCardPhoto;
    private String nationalId;
    private String serialNumber;
    private String birthDate;
    private String gender;
    private String motherName;
    private String fatherName;
    private String approvedByPhone;
    private boolean approved;
    private String approvedAt;
    private String userPhone;
    
    public UserIdentityInfoDTO() {}
    
    public UserIdentityInfoDTO(Long id, String frontCardPhoto, String backCardPhoto, String nationalId,
                               String serialNumber, String birthDate, String gender, 
                               String motherName, String fatherName, String approvedByPhone,
                               boolean approved, String approvedAt, String userPhone) {
        this.id = id;
        this.frontCardPhoto = frontCardPhoto;
        this.backCardPhoto = backCardPhoto;
        this.nationalId = nationalId;
        this.serialNumber = serialNumber;
        this.birthDate = birthDate;
        this.gender = gender;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.approvedByPhone = approvedByPhone;
        this.approved = approved;
        this.approvedAt = approvedAt;
        this.userPhone = userPhone;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFrontCardPhoto() { return frontCardPhoto; }
    public void setFrontCardPhoto(String frontCardPhoto) { this.frontCardPhoto = frontCardPhoto; }
    
    public String getBackCardPhoto() { return backCardPhoto; }
    public void setBackCardPhoto(String backCardPhoto) { this.backCardPhoto = backCardPhoto; }
    
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    
    // Eski API uyumluluğu için
    public String getTcNo() { return nationalId; }
    public void setTcNo(String tcNo) { this.nationalId = tcNo; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    
    public String getApprovedByPhone() { return approvedByPhone; }
    public void setApprovedByPhone(String approvedByPhone) { this.approvedByPhone = approvedByPhone; }
    
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    
    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    
    // Eski API uyumluluğu için
    public String getFirstName() { return ""; } // API'de yok
    public void setFirstName(String firstName) { /* API'de yok */ }
    
    public String getLastName() { return ""; } // API'de yok  
    public void setLastName(String lastName) { /* API'de yok */ }
    
    public String getAddress() { return ""; } // API'de yok
    public void setAddress(String address) { /* API'de yok */ }
    
    public String getDocumentImageUrl() { return frontCardPhoto; } // Uyumluluk için
    public void setDocumentImageUrl(String documentImageUrl) { this.frontCardPhoto = documentImageUrl; }
    
    public String getSelfieImageUrl() { return backCardPhoto; } // Uyumluluk için
    public void setSelfieImageUrl(String selfieImageUrl) { this.backCardPhoto = selfieImageUrl; }
}
