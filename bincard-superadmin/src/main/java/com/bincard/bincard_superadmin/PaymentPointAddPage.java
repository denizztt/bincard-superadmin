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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Yeni ödeme noktası ekleme sayfası - Harita ile konum seçimi
 */
public class PaymentPointAddPage extends SuperadminPageBase {

    // Form alanları
    private TextField nameField;
    private TextField latitudeField;
    private TextField longitudeField;
    private TextField addressField;
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
    
    // Harita ve konum - WEB TABALLI
    private Label locationStatusLabel;
    private Button openMapButton;
    private Button checkLocationButton;
    private ScheduledExecutorService locationChecker;
    private Path locationDataFile;
    
    // Butonlar
    private Button saveButton;
    private Button cancelButton;
    private Button clearButton;

    public PaymentPointAddPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktası Ekle");
        
        // Konum veri dosyası yolu
        try {
            locationDataFile = Paths.get(System.getProperty("java.io.tmpdir"), "bincard_location_data.json");
            System.out.println("📍 Konum veri dosyası: " + locationDataFile.toString());
        } catch (Exception e) {
            System.err.println("❌ Konum veri dosyası oluşturulamadı: " + e.getMessage());
        }
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
        
        // Sol panel - Harita (büyütüldü)
        VBox mapPanel = createMapPanel();
        mapPanel.setPrefWidth(700);
        
        // Sağ panel - Form
        ScrollPane formScrollPane = new ScrollPane(createFormPanel());
        formScrollPane.setFitToWidth(true);
        formScrollPane.setPrefWidth(450);
        formScrollPane.setStyle("-fx-background-color: transparent;");
        
        mainLayout.getChildren().addAll(mapPanel, formScrollPane);
        
        // Buton container
        HBox buttonContainer = createButtonContainer();

        mainContainer.getChildren().addAll(titleContainer, mainLayout, buttonContainer);
        
        return mainContainer;
    }
    
    /**
     * Harita panelini oluşturur - WEB TABANLI
     */
    private VBox createMapPanel() {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        panel.setPadding(new Insets(25));
        
        // Harita başlığı
        Label mapTitle = new Label("🗺️ Konum Seçimi - Web Harita");
        mapTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        mapTitle.setTextFill(Color.web("#34495e"));
        
        // Açıklama
        Label descLabel = new Label("Harita web tarayıcısında açılacak. Konum seçtikten sonra buraya otomatik gelecek.");
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        descLabel.setWrapText(true);
        
        // Lokasyon durumu
        locationStatusLabel = new Label("Henüz konum seçilmedi");
        locationStatusLabel.setTextFill(Color.web("#e74c3c"));
        locationStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Harita açma butonu
        openMapButton = new Button("🌍 Web Haritayı Aç");
        openMapButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 30;");
        FontIcon mapIcon = new FontIcon(FontAwesomeSolid.MAP_MARKED_ALT);
        mapIcon.setIconColor(Color.WHITE);
        mapIcon.setIconSize(20);
        openMapButton.setGraphic(mapIcon);
        openMapButton.setOnAction(e -> openWebMap());
        
        // Konum kontrol butonu
        checkLocationButton = new Button("🔄 Konum Kontrolü");
        checkLocationButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
        FontIcon checkIcon = new FontIcon(FontAwesomeSolid.SYNC_ALT);
        checkIcon.setIconColor(Color.WHITE);
        checkLocationButton.setGraphic(checkIcon);
        checkLocationButton.setOnAction(e -> checkLocationData());
        
        // Buton container
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(openMapButton, checkLocationButton);
        
        // Konum bilgileri gösterimi
        VBox locationInfoBox = createLocationInfoDisplay();
        
        panel.getChildren().addAll(mapTitle, descLabel, locationStatusLabel, buttonBox, locationInfoBox);
        return panel;
    }
    
    /**
     * Konum bilgileri gösterimi
     */
    private VBox createLocationInfoDisplay() {
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 8;");
        
        Label infoTitle = new Label("📍 Seçilen Konum Bilgileri");
        infoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web("#2c3e50"));
        
        Label coordsLabel = new Label("Koordinatlar: Henüz seçilmedi");
        coordsLabel.setFont(Font.font("Segoe UI", 12));
        coordsLabel.setTextFill(Color.web("#34495e"));
        
        Label addressLabel = new Label("Adres: Henüz seçilmedi");
        addressLabel.setFont(Font.font("Segoe UI", 12));
        addressLabel.setTextFill(Color.web("#34495e"));
        addressLabel.setWrapText(true);
        
        infoBox.getChildren().addAll(infoTitle, coordsLabel, addressLabel);
        return infoBox;
    }
    
    /**
     * Web haritayı açar
     */
    private void openWebMap() {
        try {
            // Konum veri dosyasını temizle
            if (Files.exists(locationDataFile)) {
                Files.delete(locationDataFile);
                System.out.println("🗑️ Eski konum verisi temizlendi");
            }
            
            // HTML dosyasının yolunu al - proje dizininde
            Path htmlFile = Paths.get("location_picker.html").toAbsolutePath();
            
            if (!Files.exists(htmlFile)) {
                // Alternatif yollar dene
                htmlFile = Paths.get("bincard-superadmin", "location_picker.html").toAbsolutePath();
                if (!Files.exists(htmlFile)) {
                    htmlFile = Paths.get(".", "location_picker.html").toAbsolutePath();
                }
            }
            
            if (!Files.exists(htmlFile)) {
                showAlert("HTML dosyası bulunamadı!\n\nAranan konum: " + htmlFile.toString() + 
                         "\n\nLütfen 'location_picker.html' dosyasının proje dizininde olduğundan emin olun.", 
                         Alert.AlertType.ERROR);
                return;
            }
            
            System.out.println("📁 HTML dosyası bulundu: " + htmlFile.toString());
            
            // Desktop desteğini kontrol et
            if (!Desktop.isDesktopSupported()) {
                showAlert("Bu sistemde web tarayıcısı açma desteklenmiyor!", Alert.AlertType.ERROR);
                return;
            }
            
            Desktop desktop = Desktop.getDesktop();
            
            // Web tarayıcısı desteğini kontrol et
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                showAlert("Bu sistemde web tarayıcısı açma desteklenmiyor!", Alert.AlertType.ERROR);
                return;
            }
            
            // Web tarayıcısında aç
            desktop.browse(htmlFile.toUri());
            
            locationStatusLabel.setText("🌐 Web harita açıldı - Konum seçimi bekleniyor...");
            locationStatusLabel.setTextFill(Color.web("#f39c12"));
            
            // Otomatik konum kontrolü başlat
            startLocationMonitoring();
            
            System.out.println("🌐 Web harita başarıyla açıldı: " + htmlFile.toUri().toString());
            
            // Bilgilendirme mesajı
            showAlert("Web harita açıldı!\n\n" +
                     "• Haritadan bir konum seçin\n" +
                     "• 'Konumu Onayla' butonuna tıklayın\n" +
                     "• Dosya indirilecek ve form otomatik dolacak\n" +
                     "• İndirilen JSON dosyasını Downloads klasörünüzden bulabilirsiniz", 
                     Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("❌ Web harita açma hatası: " + e.getMessage());
            e.printStackTrace();
            showAlert("Web harita açılamadı!\n\nHata: " + e.getMessage() + 
                     "\n\nLütfen varsayılan web tarayıcınızın düzgün çalıştığından emin olun.", 
                     Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Konum izlemeyi başlat
     */
    private void startLocationMonitoring() {
        if (locationChecker != null) {
            locationChecker.shutdown();
        }
        
        locationChecker = Executors.newSingleThreadScheduledExecutor();
        locationChecker.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                checkLocationData();
                checkDownloadsFolder(); // Downloads klasörünü de kontrol et
            });
        }, 2, 3, TimeUnit.SECONDS); // Her 3 saniyede bir kontrol et
        
        System.out.println("🔄 Konum izleme başlatıldı (Temp + Downloads klasörleri)");
    }
    
    /**
     * Downloads klasöründe JSON dosyasını kontrol et
     */
    private void checkDownloadsFolder() {
        try {
            // Kullanıcının Downloads klasörünü bul
            String userHome = System.getProperty("user.home");
            Path downloadsDir = Paths.get(userHome, "Downloads");
            Path jsonFile = downloadsDir.resolve("bincard_location_data.json");
            
            if (Files.exists(jsonFile)) {
                System.out.println("📥 Downloads klasöründe JSON dosyası bulundu: " + jsonFile.toString());
                
                String jsonContent = Files.readString(jsonFile, StandardCharsets.UTF_8);
                
                if (!jsonContent.trim().isEmpty() && jsonContent.contains("latitude")) {
                    System.out.println("📋 Downloads'dan konum verisi okunuyor...");
                    
                    // JSON'dan değerleri çıkar
                    String latitude = extractJsonValue(jsonContent, "latitude");
                    String longitude = extractJsonValue(jsonContent, "longitude");
                    String address = extractJsonValue(jsonContent, "address");
                    String street = extractJsonValue(jsonContent, "street");
                    String district = extractJsonValue(jsonContent, "district");
                    String city = extractJsonValue(jsonContent, "city");
                    String postalCode = extractJsonValue(jsonContent, "postalCode");
                    
                    // Form alanlarını doldur
                    if (!latitude.isEmpty()) latitudeField.setText(latitude);
                    if (!longitude.isEmpty()) longitudeField.setText(longitude);
                    if (!address.isEmpty()) addressField.setText(address);
                    if (!street.isEmpty()) streetField.setText(street);
                    if (!district.isEmpty()) districtField.setText(district);
                    if (!city.isEmpty()) cityField.setText(city);
                    if (!postalCode.isEmpty()) postalCodeField.setText(postalCode);
                    
                    // Status güncelle
                    locationStatusLabel.setText("✅ Konum Downloads'dan alındı ve form dolduruldu!");
                    locationStatusLabel.setTextFill(Color.web("#27AE60"));
                    
                    // Başarı mesajı
                    showAlert("🎉 Konum bilgileri başarıyla alındı!\n\n" +
                             "📍 Koordinatlar: " + latitude + ", " + longitude + "\n" +
                             "📍 Adres: " + address + "\n\n" +
                             "Form alanları otomatik olarak dolduruldu.", 
                             Alert.AlertType.INFORMATION);
                    
                    // İzlemeyi durdur
                    if (locationChecker != null) {
                        locationChecker.shutdown();
                        locationChecker = null;
                    }
                    
                    // Dosyayı temizle
                    Files.delete(jsonFile);
                    System.out.println("✅ Downloads'daki JSON dosyası işlendi ve temizlendi");
                }
            }
            
        } catch (Exception e) {
            // Sessizce ignore et - normal durum
        }
    }
    
    /**
     * Konum verilerini kontrol et
     */
    private void checkLocationData() {
        try {
            if (!Files.exists(locationDataFile)) {
                return; // Henüz dosya yok
            }
            
            String jsonContent = Files.readString(locationDataFile, StandardCharsets.UTF_8);
            
            if (jsonContent.trim().isEmpty()) {
                return; // Boş dosya
            }
            
            System.out.println("📋 Konum verisi alındı: " + jsonContent);
            
            // Basit JSON parsing (gerçek JSON parsing yerine)
            if (jsonContent.contains("latitude") && jsonContent.contains("longitude")) {
                
                // JSON'dan değerleri çıkar (basit string manipülasyonu)
                String latitude = extractJsonValue(jsonContent, "latitude");
                String longitude = extractJsonValue(jsonContent, "longitude");
                String address = extractJsonValue(jsonContent, "address");
                String street = extractJsonValue(jsonContent, "street");
                String district = extractJsonValue(jsonContent, "district");
                String city = extractJsonValue(jsonContent, "city");
                String postalCode = extractJsonValue(jsonContent, "postalCode");
                
                // Form alanlarını doldur
                if (!latitude.isEmpty()) latitudeField.setText(latitude);
                if (!longitude.isEmpty()) longitudeField.setText(longitude);
                if (!address.isEmpty()) addressField.setText(address);
                if (!street.isEmpty()) streetField.setText(street);
                if (!district.isEmpty()) districtField.setText(district);
                if (!city.isEmpty()) cityField.setText(city);
                if (!postalCode.isEmpty()) postalCodeField.setText(postalCode);
                
                // Status güncelle
                locationStatusLabel.setText("✅ Konum başarıyla alındı ve form dolduruldu!");
                locationStatusLabel.setTextFill(Color.web("#27AE60"));
                
                // Başarı mesajı
                showAlert("Konum bilgileri başarıyla alındı ve form dolduruldu!", Alert.AlertType.INFORMATION);
                
                // İzlemeyi durdur
                if (locationChecker != null) {
                    locationChecker.shutdown();
                    locationChecker = null;
                }
                
                // Dosyayı temizle
                Files.delete(locationDataFile);
                
                System.out.println("✅ Konum verisi başarıyla işlendi ve dosya temizlendi");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Konum verisi kontrolü hatası: " + e.getMessage());
        }
    }
    
    /**
     * JSON'dan değer çıkarma (basit)
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return "";
            
            startIndex += searchKey.length();
            
            // Değerin başlangıcını bul
            while (startIndex < json.length() && (json.charAt(startIndex) == ' ' || json.charAt(startIndex) == '"')) {
                startIndex++;
            }
            
            // Değerin sonunu bul
            int endIndex = startIndex;
            boolean inQuotes = json.charAt(startIndex - 1) == '"';
            
            if (inQuotes) {
                endIndex = json.indexOf('"', startIndex);
            } else {
                while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                    endIndex++;
                }
            }
            
            if (endIndex == -1) endIndex = json.length();
            
            return json.substring(startIndex, endIndex).trim();
            
        } catch (Exception e) {
            System.err.println("JSON değer çıkarma hatası: " + e.getMessage());
            return "";
        }
    }
    
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
    
    // Form bölümlerini oluşturan metodlar
    private VBox createBasicInfoSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Temel Bilgiler");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // İsim alanı
        Label nameLabel = new Label("Ödeme Noktası Adı:");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        nameField = new TextField();
        nameField.setPromptText("Örn: Merkez ATM, Üniversite Kantini");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        // Açıklama alanı
        Label descLabel = new Label("Açıklama:");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Ödeme noktası hakkında detaylı bilgi...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setStyle("-fx-font-size: 14px;");
        
        // Aktif durumu
        activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setSelected(true);
        activeCheckBox.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        section.getChildren().addAll(sectionTitle, nameLabel, nameField, descLabel, descriptionArea, activeCheckBox);
        return section;
    }
    
    private VBox createLocationSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Konum Bilgileri (Harita ile otomatik dolacak)");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#27AE60"));
        
        // Koordinatlar
        HBox coordBox = new HBox(10);
        Label latLabel = new Label("Enlem:");
        latLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        latitudeField = new TextField();
        latitudeField.setPromptText("41.0082");
        latitudeField.setEditable(false);
        latitudeField.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 14px;");
        
        Label lngLabel = new Label("Boylam:");
        lngLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        longitudeField = new TextField();
        longitudeField.setPromptText("28.9784");
        longitudeField.setEditable(false);
        longitudeField.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 14px;");
        
        coordBox.getChildren().addAll(latLabel, latitudeField, lngLabel, longitudeField);
        
        // Adres bilgileri
        Label streetLabel = new Label("Sokak/Cadde:");
        streetLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        streetField = new TextField();
        streetField.setPromptText("Haritadan otomatik doldurulacak");
        streetField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        Label districtLabel = new Label("İlçe:");
        districtLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        districtField = new TextField();
        districtField.setPromptText("Haritadan otomatik doldurulacak");
        districtField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        Label cityLabel = new Label("Şehir:");
        cityLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        cityField = new TextField();
        cityField.setPromptText("Haritadan otomatik doldurulacak");
        cityField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        Label postalLabel = new Label("Posta Kodu:");
        postalLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        postalCodeField = new TextField();
        postalCodeField.setPromptText("Haritadan otomatik doldurulacak");
        postalCodeField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        section.getChildren().addAll(sectionTitle, coordBox, streetLabel, streetField, 
                                   districtLabel, districtField, cityLabel, cityField, 
                                   postalLabel, postalCodeField);
        return section;
    }
    
    private VBox createContactSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #fff3cd; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("İletişim Bilgileri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#856404"));
        
        Label contactLabel = new Label("İletişim Numarası:");
        contactLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        contactNumberField = new TextField();
        contactNumberField.setPromptText("Örn: +90 212 555 0123");
        contactNumberField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        
        section.getChildren().addAll(sectionTitle, contactLabel, contactNumberField);
        return section;
    }
    
    private VBox createWorkingHoursSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #e1f5fe; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Çalışma Saatleri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#01579b"));
        
        // Başlangıç saati
        Label startLabel = new Label("Başlangıç Saati:");
        startLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        HBox startBox = new HBox(10);
        startHourSlider = new Slider(0, 23, 8);
        startHourSlider.setShowTickLabels(true);
        startHourSlider.setShowTickMarks(true);
        startHourSlider.setMajorTickUnit(6);
        startMinuteSlider = new Slider(0, 59, 0);
        startMinuteSlider.setShowTickLabels(true);
        startMinuteSlider.setMajorTickUnit(15);
        startBox.getChildren().addAll(new Label("Saat:"), startHourSlider, new Label("Dakika:"), startMinuteSlider);
        
        // Bitiş saati
        Label endLabel = new Label("Bitiş Saati:");
        endLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        HBox endBox = new HBox(10);
        endHourSlider = new Slider(0, 23, 18);
        endHourSlider.setShowTickLabels(true);
        endHourSlider.setShowTickMarks(true);
        endHourSlider.setMajorTickUnit(6);
        endMinuteSlider = new Slider(0, 59, 0);
        endMinuteSlider.setShowTickLabels(true);
        endMinuteSlider.setMajorTickUnit(15);
        endBox.getChildren().addAll(new Label("Saat:"), endHourSlider, new Label("Dakika:"), endMinuteSlider);
        
        // Çalışma saatleri etiketi
        workingHoursLabel = new Label("Çalışma Saatleri: 08:00 - 18:00");
        workingHoursLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        workingHoursLabel.setTextFill(Color.web("#01579b"));
        
        // Slider değişiklik dinleyicileri
        startHourSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateWorkingHoursLabel());
        startMinuteSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateWorkingHoursLabel());
        endHourSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateWorkingHoursLabel());
        endMinuteSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateWorkingHoursLabel());
        
        section.getChildren().addAll(sectionTitle, startLabel, startBox, endLabel, endBox, workingHoursLabel);
        return section;
    }
    
    private VBox createPaymentMethodsSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #f3e5f5; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Ödeme Yöntemleri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#4a148c"));
        
        // Ödeme yöntemi checkboxları
        cashCheckBox = new CheckBox("Nakit");
        cashCheckBox.setSelected(true);
        cashCheckBox.setStyle("-fx-font-size: 14px;");
        
        creditCardCheckBox = new CheckBox("Kredi Kartı");
        creditCardCheckBox.setSelected(true);
        creditCardCheckBox.setStyle("-fx-font-size: 14px;");
        
        debitCardCheckBox = new CheckBox("Banka Kartı");
        debitCardCheckBox.setSelected(true);
        debitCardCheckBox.setStyle("-fx-font-size: 14px;");
        
        mobileAppCheckBox = new CheckBox("Mobil Uygulama");
        mobileAppCheckBox.setSelected(false);
        mobileAppCheckBox.setStyle("-fx-font-size: 14px;");
        
        qrCodeCheckBox = new CheckBox("QR Code");
        qrCodeCheckBox.setSelected(false);
        qrCodeCheckBox.setStyle("-fx-font-size: 14px;");
        
        VBox checkBoxContainer = new VBox(8);
        checkBoxContainer.getChildren().addAll(cashCheckBox, creditCardCheckBox, debitCardCheckBox, 
                                              mobileAppCheckBox, qrCodeCheckBox);
        
        section.getChildren().addAll(sectionTitle, checkBoxContainer);
        return section;
    }
    
    private VBox createOtherInfoSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #fce4ec; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Diğer Bilgiler");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#880e4f"));
        
        Label infoLabel = new Label("Ek bilgiler ve notlar buraya eklenebilir.");
        infoLabel.setFont(Font.font("Segoe UI", 14));
        infoLabel.setTextFill(Color.web("#ad1457"));
        
        section.getChildren().addAll(sectionTitle, infoLabel);
        return section;
    }
    
    private HBox createButtonContainer() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        
        // Kaydet butonu
        saveButton = new Button("Ödeme Noktası Kaydet");
        saveButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 25;");
        FontIcon saveIcon = new FontIcon(FontAwesomeSolid.SAVE);
        saveIcon.setIconColor(Color.WHITE);
        saveButton.setGraphic(saveIcon);
        saveButton.setOnAction(e -> savePaymentPoint());
        
        // Temizle butonu
        clearButton = new Button("Formu Temizle");
        clearButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
        FontIcon clearIcon = new FontIcon(FontAwesomeSolid.ERASER);
        clearIcon.setIconColor(Color.WHITE);
        clearButton.setGraphic(clearIcon);
        clearButton.setOnAction(e -> clearForm());
        
        // İptal butonu
        cancelButton = new Button("İptal");
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
        FontIcon cancelIcon = new FontIcon(FontAwesomeSolid.TIMES);
        cancelIcon.setIconColor(Color.WHITE);
        cancelButton.setGraphic(cancelIcon);
        cancelButton.setOnAction(e -> stage.close());
        
        container.getChildren().addAll(cancelButton, clearButton, saveButton);
        return container;
    }
    
    // Yardımcı metodlar
    private void updateWorkingHoursLabel() {
        int startHour = (int) startHourSlider.getValue();
        int startMinute = (int) startMinuteSlider.getValue();
        int endHour = (int) endHourSlider.getValue();
        int endMinute = (int) endMinuteSlider.getValue();
        
        String startTime = String.format("%02d:%02d", startHour, startMinute);
        String endTime = String.format("%02d:%02d", endHour, endMinute);
        
        workingHoursLabel.setText("Çalışma Saatleri: " + startTime + " - " + endTime);
    }
    
    private void savePaymentPoint() {
        // Form validation
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Ödeme noktası adı boş olamaz!", Alert.AlertType.ERROR);
            return;
        }
        
        if (latitudeField.getText().trim().isEmpty() || longitudeField.getText().trim().isEmpty()) {
            showAlert("Lütfen haritadan bir konum seçin!", Alert.AlertType.ERROR);
            return;
        }
        
        // Burada API çağrısı yapılacak
        showAlert("Ödeme noktası başarıyla kaydedildi!", Alert.AlertType.INFORMATION);
    }
    
    private void clearForm() {
        nameField.clear();
        latitudeField.clear();
        longitudeField.clear();
        streetField.clear();
        districtField.clear();
        cityField.clear();
        postalCodeField.clear();
        contactNumberField.clear();
        descriptionArea.clear();
        activeCheckBox.setSelected(true);
        
        startHourSlider.setValue(8);
        startMinuteSlider.setValue(0);
        endHourSlider.setValue(18);
        endMinuteSlider.setValue(0);
        
        cashCheckBox.setSelected(true);
        creditCardCheckBox.setSelected(true);
        debitCardCheckBox.setSelected(true);
        mobileAppCheckBox.setSelected(false);
        qrCodeCheckBox.setSelected(false);
        
        // Form temizle - konum izlemeyi durdur
        if (locationChecker != null) {
            locationChecker.shutdown();
            locationChecker = null;
        }
        
        locationStatusLabel.setText("Henüz konum seçilmedi");
        locationStatusLabel.setTextFill(Color.web("#e74c3c"));
        locationStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    }
}
