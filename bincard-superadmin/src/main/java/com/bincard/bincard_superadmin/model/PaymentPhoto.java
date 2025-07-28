package com.bincard.bincard_superadmin.model;

public class PaymentPhoto {
    private Long id;
    private String imageUrl;

    // Constructors
    public PaymentPhoto() {}

    public PaymentPhoto(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "PaymentPhoto{" +
                "id=" + id +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
