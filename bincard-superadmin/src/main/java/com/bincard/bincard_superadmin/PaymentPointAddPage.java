package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import netscape.javascript.JSObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Yeni ödeme noktası ekleme sayfası - Harita ile konum seçimi
 */
public class PaymentPointAddPage extends SuperadminPageBase {

    // Form alanları
    private TextField nameField;
    private TextField latitudeField;
    private TextField longitudeField;
    private TextField streetField;
    private TextField districtField;
    private TextField cityField;
    private TextField postalCodeField;
    private TextField contactNumberField;
    private TextArea descriptionArea;
    private CheckBox activeCheckBox;
    
    // Çalışma saatleri için slider'lar
    private Slider startHourSlider;
    private Slider startMinuteSlider;
    private Slider endHourSlider;
    private Slider endMinuteSlider;
    private Label workingHoursLabel;
    
    // Ödeme yöntemleri için checkbox'lar
    private CheckBox cashCheckBox;
    private CheckBox creditCardCheckBox;
    private CheckBox debitCardCheckBox;
    private CheckBox mobileAppCheckBox;
    private CheckBox qrCodeCheckBox;
    
    // Harita ve konum
    private WebView mapWebView;
    private WebEngine webEngine;
    private Button selectLocationButton;
    private Label locationStatusLabel;
    
    // Butonlar
    private Button saveButton;
    private Button cancelButton;
    private Button clearButton;

    public PaymentPointAddPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktası Ekle");
    }

    @Override
    protected Node createContent() {
        // Ana container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        Label titleLabel = new Label("Yeni Ödeme Noktası Ekle");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // İkon ile başlık container'ı
        HBox titleContainer = new HBox(15);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        FontIcon titleIcon = new FontIcon(FontAwesomeSolid.PLUS_CIRCLE);
        titleIcon.setIconSize(28);
        titleIcon.setIconColor(Color.web("#2c3e50"));
        titleContainer.getChildren().addAll(titleIcon, titleLabel);

        // Ana layout - Sol harita, sağ form
        HBox mainLayout = new HBox(20);
        
        // Sol panel - Harita
        VBox mapPanel = createMapPanel();
        mapPanel.setPrefWidth(500);
        
        // Sağ panel - Form
        ScrollPane formScrollPane = new ScrollPane(createFormPanel());
        formScrollPane.setFitToWidth(true);
        formScrollPane.setPrefWidth(500);
        formScrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.getChildren().addAll(mapPanel, formScrollPane);
        
        // Buton container
        HBox buttonContainer = createButtonContainer();

        mainContainer.getChildren().addAll(titleContainer, mainLayout, buttonContainer);
        
        return mainContainer;
    }
    
    /**
     * Harita panelini oluşturur
     */
    private VBox createMapPanel() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        panel.setPadding(new Insets(20));
        
        // Harita başlığı
        Label mapTitle = new Label("Konum Seçimi");
        mapTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        mapTitle.setTextFill(Color.web("#34495e"));
        
        // Lokasyon durumu
        locationStatusLabel = new Label("Haritadan bir konum seçin");
        locationStatusLabel.setTextFill(Color.web("#7f8c8d"));
        locationStatusLabel.setStyle("-fx-font-size: 14px;");
        
        // Harita WebView
        mapWebView = new WebView();
        mapWebView.setPrefHeight(400);
        webEngine = mapWebView.getEngine();
        
        // Harita HTML'ini yükle
        loadMapHTML();
        
        // Konum seç butonu
        selectLocationButton = new Button("Seçilen Konumu Onayla");
        selectLocationButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                                     "-fx-font-weight: bold; -fx-padding: 10 20;");
        selectLocationButton.setOnAction(e -> confirmSelectedLocation());
        
        panel.getChildren().addAll(mapTitle, locationStatusLabel, mapWebView, selectLocationButton);
        return panel;
    }
    
    /**
     * Google Maps API kullanarak harita HTML'ini yükler
     */
    private void loadMapHTML() {
        String mapHTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ödeme Noktası Konum Seçimi</title>
                <style>
                    body, html {
                        height: 100%;
                        margin: 0;
                        padding: 0;
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: #f8f9fa;
                    }
                    
                    #map {
                        height: 100%;
                        width: 100%;
                        border-radius: 8px;
                    }
                    
                    .info-panel {
                        position: absolute;
                        top: 10px;
                        left: 10px;
                        right: 10px;
                        background: white;
                        padding: 15px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        z-index: 1000;
                        display: none;
                    }
                    
                    .coordinate-info {
                        font-size: 14px;
                        color: #2c3e50;
                        margin-bottom: 5px;
                    }
                    
                    .address-info {
                        font-size: 12px;
                        color: #7f8c8d;
                    }
                    
                    .search-container {
                        position: absolute;
                        top: 10px;
                        left: 50%;
                        transform: translateX(-50%);
                        z-index: 1000;
                    }
                    
                    #search-input {
                        width: 300px;
                        padding: 10px;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        font-size: 14px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    
                    .controls {
                        position: absolute;
                        bottom: 10px;
                        right: 10px;
                        z-index: 1000;
                    }
                    
                    .control-btn {
                        background: #3498db;
                        color: white;
                        border: none;
                        padding: 8px 12px;
                        margin: 2px;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 12px;
                    }
                    
                    .control-btn:hover {
                        background: #2980b9;
                    }
                </style>
            </head>
            <body>
                <div class="search-container">
                    <input type="text" id="search-input" placeholder="Adres veya yer ara (ör: İstanbul Üniversitesi)">
                </div>
                
                <div id="coordinates" class="info-panel">
                    <div id="lat-lng" class="coordinate-info"></div>
                    <div id="address" class="address-info"></div>
                </div>
                
                <div id="map"></div>
                
                <div class="controls">
                    <button class="control-btn" onclick="getCurrentLocation()">Konumum</button>
                    <button class="control-btn" onclick="clearSelection()">Temizle</button>
                </div>
                
                <script>
                    let map;
                    let selectedMarker;
                    let selectedLocation = null;
                    let geocoder;
                    let autocomplete;
                    
                    // Google Maps API'sını başlat
                    function initMap() {
                        // İstanbul merkezli harita
                        const istanbul = { lat: 41.0082, lng: 28.9784 };
                        
                        map = new google.maps.Map(document.getElementById("map"), {
                            zoom: 11,
                            center: istanbul,
                            mapTypeControl: true,
                            mapTypeControlOptions: {
                                style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
                                mapTypeIds: ["roadmap", "terrain", "satellite", "hybrid"]
                            },
                            streetViewControl: true,
                            fullscreenControl: true,
                            zoomControl: true
                        });
                        
                        geocoder = new google.maps.Geocoder();
                        
                        // Arama kutusu için autocomplete
                        const searchInput = document.getElementById('search-input');
                        autocomplete = new google.maps.places.Autocomplete(searchInput, {
                            types: ['establishment', 'geocode'],
                            componentRestrictions: { country: 'TR' }
                        });
                        
                        // Arama sonucu seçildiğinde
                        autocomplete.addListener('place_changed', function() {
                            const place = autocomplete.getPlace();
                            if (place.geometry) {
                                map.setCenter(place.geometry.location);
                                map.setZoom(16);
                                
                                // Marker ekle
                                selectLocation(place.geometry.location.lat(), place.geometry.location.lng());
                            }
                        });
                        
                        // Harita tıklama eventi
                        map.addListener("click", (event) => {
                            selectLocation(event.latLng.lat(), event.latLng.lng());
                        });
                    }
                    
                    // Konum seçimi fonksiyonu
                    function selectLocation(lat, lng) {
                        // Önceki marker'ı temizle
                        if (selectedMarker) {
                            selectedMarker.setMap(null);
                        }
                        
                        // Yeni marker ekle
                        selectedMarker = new google.maps.Marker({
                            position: { lat: lat, lng: lng },
                            map: map,
                            title: "Seçilen Konum",
                            icon: {
                                url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="#e74c3c">
                                        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                                    </svg>
                                `),
                                scaledSize: new google.maps.Size(40, 40),
                                anchor: new google.maps.Point(20, 40)
                            },
                            animation: google.maps.Animation.DROP
                        });
                        
                        selectedLocation = { lat: lat, lng: lng };
                        
                        // Koordinat bilgilerini göster
                        document.getElementById('lat-lng').textContent = 
                            `Enlem: ${lat.toFixed(6)}, Boylam: ${lng.toFixed(6)}`;
                        document.getElementById('coordinates').style.display = 'block';
                        
                        // Reverse geocoding ile adres bilgisi al
                        geocoder.geocode({ location: { lat: lat, lng: lng } }, (results, status) => {
                            if (status === "OK" && results[0]) {
                                const addressComponents = results[0].address_components;
                                const formattedAddress = results[0].formatted_address;
                                
                                // Adres bileşenlerini parse et
                                let street = "";
                                let district = "";
                                let city = "";
                                let postalCode = "";
                                
                                addressComponents.forEach(component => {
                                    const types = component.types;
                                    if (types.includes('route')) {
                                        street = component.long_name;
                                    } else if (types.includes('street_number')) {
                                        street = component.long_name + " " + street;
                                    } else if (types.includes('sublocality') || types.includes('neighborhood')) {
                                        district = component.long_name;
                                    } else if (types.includes('administrative_area_level_1')) {
                                        city = component.long_name;
                                    } else if (types.includes('postal_code')) {
                                        postalCode = component.long_name;
                                    }
                                });
                                
                                document.getElementById('address').textContent = formattedAddress;
                                
                                // Java tarafına bilgileri gönder
                                if (window.javaApp) {
                                    window.javaApp.locationSelected({
                                        latitude: lat,
                                        longitude: lng,
                                        address: formattedAddress,
                                        street: street || "",
                                        district: district || "",
                                        city: city || "",
                                        postalCode: postalCode || ""
                                    });
                                }
                            } else {
                                document.getElementById('address').textContent = "Adres bilgisi alınamadı";
                                
                                // Temel bilgilerle Java'ya gönder
                                if (window.javaApp) {
                                    window.javaApp.locationSelected({
                                        latitude: lat,
                                        longitude: lng,
                                        address: `${lat.toFixed(6)}, ${lng.toFixed(6)}`,
                                        street: "",
                                        district: "",
                                        city: "",
                                        postalCode: ""
                                    });
                                }
                            }
                        });
                    }
                    
                    // Kullanıcının mevcut konumunu al
                    function getCurrentLocation() {
                        if (navigator.geolocation) {
                            navigator.geolocation.getCurrentPosition(
                                (position) => {
                                    const lat = position.coords.latitude;
                                    const lng = position.coords.longitude;
                                    
                                    map.setCenter({ lat: lat, lng: lng });
                                    map.setZoom(16);
                                    selectLocation(lat, lng);
                                },
                                (error) => {
                                    alert("Konum alınamadı: " + error.message);
                                }
                            );
                        } else {
                            alert("Tarayıcınız konum servislerini desteklemiyor.");
                        }
                    }
                    
                    // Seçimi temizle
                    function clearSelection() {
                        if (selectedMarker) {
                            selectedMarker.setMap(null);
                            selectedMarker = null;
                        }
                        selectedLocation = null;
                        document.getElementById('coordinates').style.display = 'none';
                        document.getElementById('search-input').value = '';
                        
                        if (window.javaApp) {
                            window.javaApp.locationCleared();
                        }
                    }
                    
                    // Seçilen konumu döndür
                    function getSelectedLocation() {
                        return selectedLocation;
                    }
                </script>
                
                <!-- Google Maps API -->
                <script async defer
                    src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBRYfrvFsxgARSM_iE7JA1EHu1nSpaWAxc&libraries=places&callback=initMap">
                </script>
            </body>
            </html>
            """;
        
        webEngine.loadContent(mapHTML);
        
        // WebEngine yüklendiğinde Java-JavaScript köprüsünü kur
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // JavaScript'e Java nesnesini gönder
                    Object window = webEngine.executeScript("window");
                    if (window instanceof JSObject) {
                        ((JSObject) window).setMember("javaApp", new JavaScriptBridge());
                    }
                } catch (Exception e) {
                    System.err.println("JavaScript bridge error: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * JavaScript köprüsü sınıfı
     */
    public class JavaScriptBridge {
        public void locationSelected(Object locationData) {
            Platform.runLater(() -> {
                try {
                    if (locationData instanceof JSObject) {
                        JSObject loc = (JSObject) locationData;
                        double latitude = ((Number) loc.getMember("latitude")).doubleValue();
                        double longitude = ((Number) loc.getMember("longitude")).doubleValue();
                        String address = (String) loc.getMember("address");
                        String street = (String) loc.getMember("street");
                        String district = (String) loc.getMember("district");
                        String city = (String) loc.getMember("city");
                        String postalCode = (String) loc.getMember("postalCode");
                        
                        // Form alanlarını otomatik doldur
                        latitudeField.setText(String.valueOf(latitude));
                        longitudeField.setText(String.valueOf(longitude));
                        streetField.setText(street.trim());
                        districtField.setText(district);
                        cityField.setText(city);
                        postalCodeField.setText(postalCode);
                        
                        // Status güncelle
                        locationStatusLabel.setText("Konum seçildi: " + address);
                        locationStatusLabel.setTextFill(Color.web("#27AE60"));
                    }
                } catch (Exception e) {
                    locationStatusLabel.setText("Konum bilgisi alınırken hata oluştu: " + e.getMessage());
                    locationStatusLabel.setTextFill(Color.web("#E74C3C"));
                }
            });
        }
    }
    
    /**
     * Seçilen konumu onayla
     */
    private void confirmSelectedLocation() {
        if (latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty()) {
            showAlert("Lütfen önce haritadan bir konum seçin.", Alert.AlertType.WARNING);
            return;
        }
        
        showAlert("Konum bilgileri form alanlarına aktarıldı.", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Form panelini oluşturur
     */
    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        panel.setPadding(new Insets(25));
        
        // Form başlığı
        Label formTitle = new Label("Ödeme Noktası Bilgileri");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#34495e"));
        
        // Temel bilgiler
        VBox basicInfoSection = createBasicInfoSection();
        
        // Konum bilgileri (otomatik doldurulacak)
        VBox locationSection = createLocationSection();
        
        // İletişim bilgileri
        VBox contactSection = createContactSection();
        
        // Çalışma saatleri
        VBox workingHoursSection = createWorkingHoursSection();
        
        // Ödeme yöntemleri
        VBox paymentMethodsSection = createPaymentMethodsSection();
        
        // Diğer bilgiler
        VBox otherInfoSection = createOtherInfoSection();
        
        panel.getChildren().addAll(
            formTitle,
            basicInfoSection,
            locationSection,
            contactSection,
            workingHoursSection,
            paymentMethodsSection,
            otherInfoSection
        );
        
        return panel;
    }
    
    // Diğer metodlar buraya eklenecek...
    
    /**
     * Alert gösterir
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Placeholder metodlar
    private VBox createBasicInfoSection() { return new VBox(); }
    private VBox createLocationSection() { return new VBox(); }
    private VBox createContactSection() { return new VBox(); }
    private VBox createWorkingHoursSection() { return new VBox(); }
    private VBox createPaymentMethodsSection() { return new VBox(); }
    private VBox createOtherInfoSection() { return new VBox(); }
    private HBox createButtonContainer() { return new HBox(); }
}
