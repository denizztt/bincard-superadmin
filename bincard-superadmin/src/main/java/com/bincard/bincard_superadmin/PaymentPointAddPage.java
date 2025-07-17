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
     * Basit harita HTML'ini yükler
     */
    private void loadMapHTML() {
        String mapHTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Konum Seçimi</title>
                <style>
                    body { margin: 0; padding: 20px; font-family: Arial, sans-serif; }
                    #map-container { 
                        width: 100%; 
                        height: 300px; 
                        border: 2px solid #ddd; 
                        background: #f0f0f0;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        cursor: crosshair;
                    }
                    .coordinates { margin-top: 10px; padding: 10px; background: #e8f5e8; }
                    .mock-location { 
                        background: #3498db; 
                        color: white; 
                        padding: 20px; 
                        border-radius: 5px;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div id="map-container" onclick="selectLocation(event)">
                    <div class="mock-location">
                        <strong>Harita Simülasyonu</strong><br>
                        Tıklayarak konum seçin
                    </div>
                </div>
                
                <div id="coordinates" class="coordinates" style="display: none;">
                    <strong>Seçilen Konum:</strong><br>
                    <span id="lat-lng"></span><br>
                    <span id="address"></span>
                </div>
                
                <script>
                    var selectedLocation = null;
                    
                    function selectLocation(event) {
                        // Basit koordinat simülasyonu (İstanbul civarı)
                        var rect = event.target.getBoundingClientRect();
                        var x = event.clientX - rect.left;
                        var y = event.clientY - rect.top;
                        
                        // Mock koordinatlar (İstanbul)
                        var lat = 41.0082 + (Math.random() - 0.5) * 0.1;
                        var lng = 28.9784 + (Math.random() - 0.5) * 0.1;
                        
                        selectedLocation = {lat: lat, lng: lng};
                        
                        // Mock adres bilgileri
                        var mockAddresses = [
                            {street: "Atatürk Caddesi No: 123", district: "Beyoğlu", city: "İstanbul", postalCode: "34433"},
                            {street: "İstiklal Caddesi No: 45", district: "Beyoğlu", city: "İstanbul", postalCode: "34435"},
                            {street: "Bağdat Caddesi No: 67", district: "Kadıköy", city: "İstanbul", postalCode: "34710"},
                            {street: "Nişantaşı Sokak No: 89", district: "Şişli", city: "İstanbul", postalCode: "34367"}
                        ];
                        
                        var randomAddress = mockAddresses[Math.floor(Math.random() * mockAddresses.length)];
                        
                        document.getElementById('lat-lng').textContent = 
                            'Enlem: ' + lat.toFixed(6) + ', Boylam: ' + lng.toFixed(6);
                        document.getElementById('address').textContent = 
                            randomAddress.street + ', ' + randomAddress.district + '/' + randomAddress.city;
                        document.getElementById('coordinates').style.display = 'block';
                        
                        // Java tarafına bilgileri gönder
                        if (window.javaApp) {
                            window.javaApp.locationSelected({
                                latitude: lat,
                                longitude: lng,
                                address: randomAddress.street + ', ' + randomAddress.district + '/' + randomAddress.city,
                                street: randomAddress.street,
                                district: randomAddress.district,
                                city: randomAddress.city,
                                postalCode: randomAddress.postalCode
                            });
                        }
                    }
                    
                    function getSelectedLocation() {
                        return selectedLocation;
                    }
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
