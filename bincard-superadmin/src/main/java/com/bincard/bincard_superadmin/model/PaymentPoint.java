package com.bincard.bincard_superadmin.model;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentPoint {
    private Long id;
    private String name;
    private Location location;
    private Address address;
    private String contactNumber;
    private String workingHours;
    private List<PaymentMethod> paymentMethods;
    private String description;
    private boolean active;
    private List<PaymentPhoto> photos;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private Double distance;

    // Constructors
    public PaymentPoint() {}

    public PaymentPoint(Long id, String name, Location location, Address address, 
                       String contactNumber, String workingHours, List<PaymentMethod> paymentMethods,
                       String description, boolean active, List<PaymentPhoto> photos,
                       LocalDateTime createdAt, LocalDateTime lastUpdated, Double distance) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.address = address;
        this.contactNumber = contactNumber;
        this.workingHours = workingHours;
        this.paymentMethods = paymentMethods;
        this.description = description;
        this.active = active;
        this.photos = photos;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.distance = distance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public List<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<PaymentMethod> paymentMethods) { this.paymentMethods = paymentMethods; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<PaymentPhoto> getPhotos() { return photos; }
    public void setPhotos(List<PaymentPhoto> photos) { this.photos = photos; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    @Override
    public String toString() {
        return "PaymentPoint{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", address=" + address +
                ", contactNumber='" + contactNumber + '\'' +
                ", workingHours='" + workingHours + '\'' +
                ", paymentMethods=" + paymentMethods +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", distance=" + distance +
                '}';
    }
}
