package com.bincard.bincard_superadmin;

import java.util.List;
import java.util.stream.Collectors;
import com.bincard.bincard_superadmin.model.PaymentPoint;
import com.bincard.bincard_superadmin.model.PaymentMethod;

/**
 * Ödeme noktası güncelleme ve ekleme işlemleri için DTO sınıfı
 */
public class PaymentPointUpdateDTO {
    private String name;
    private LocationDTO location;
    private AddressDTO address;
    private String contactNumber;
    private String workingHours;
    private List<String> paymentMethods;
    private String description;
    private boolean active;

    // Constructors
    public PaymentPointUpdateDTO() {}

    public PaymentPointUpdateDTO(String name, LocationDTO location, AddressDTO address, 
                                String contactNumber, String workingHours, List<String> paymentMethods, 
                                String description, boolean active) {
        this.name = name;
        this.location = location;
        this.address = address;
        this.contactNumber = contactNumber;
        this.workingHours = workingHours;
        this.paymentMethods = paymentMethods;
        this.description = description;
        this.active = active;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocationDTO getLocation() { return location; }
    public void setLocation(LocationDTO location) { this.location = location; }

    public AddressDTO getAddress() { return address; }
    public void setAddress(AddressDTO address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public List<String> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<String> paymentMethods) { this.paymentMethods = paymentMethods; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /**
     * PaymentPoint'ten PaymentPointUpdateDTO oluşturur
     */
    public static PaymentPointUpdateDTO fromPaymentPoint(PaymentPoint paymentPoint) {
        PaymentPointUpdateDTO dto = new PaymentPointUpdateDTO();
        dto.setName(paymentPoint.getName());
        dto.setContactNumber(paymentPoint.getContactNumber());
        dto.setWorkingHours(paymentPoint.getWorkingHours());
        
        // PaymentMethod'ları string listesi olarak çevir
        if (paymentPoint.getPaymentMethods() != null) {
            List<String> paymentMethodStrings = paymentPoint.getPaymentMethods().stream()
                .map(PaymentMethod::name)
                .collect(Collectors.toList());
            dto.setPaymentMethods(paymentMethodStrings);
        }
        
        dto.setDescription(paymentPoint.getDescription());
        dto.setActive(paymentPoint.isActive());
        
        // Location
        if (paymentPoint.getLocation() != null && paymentPoint.getLocation().getLatitude() != null && 
            paymentPoint.getLocation().getLongitude() != null) {
            LocationDTO location = new LocationDTO();
            location.setLatitude(paymentPoint.getLocation().getLatitude());
            location.setLongitude(paymentPoint.getLocation().getLongitude());
            dto.setLocation(location);
        }
        
        // Address
        if (paymentPoint.getAddress() != null) {
            AddressDTO address = new AddressDTO();
            address.setStreet(paymentPoint.getAddress().getStreet());
            address.setDistrict(paymentPoint.getAddress().getDistrict());
            address.setCity(paymentPoint.getAddress().getCity());
            address.setPostalCode(paymentPoint.getAddress().getPostalCode());
            dto.setAddress(address);
        }
        
        return dto;
    }

    /**
     * LocationDTO iç sınıfı
     */
    public static class LocationDTO {
        private Double latitude;
        private Double longitude;

        public LocationDTO() {}

        public LocationDTO(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    /**
     * AddressDTO iç sınıfı
     */
    public static class AddressDTO {
        private String street;
        private String district;
        private String city;
        private String postalCode;

        public AddressDTO() {}

        public AddressDTO(String street, String district, String city, String postalCode) {
            this.street = street;
            this.district = district;
            this.city = city;
            this.postalCode = postalCode;
        }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    }
}
