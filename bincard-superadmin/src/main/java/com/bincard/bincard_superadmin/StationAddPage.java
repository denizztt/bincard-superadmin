package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Durak Ekleme Sayfası
 * Web tabanlı Google Maps entegrasyonu ile durak konumu seçimi
 */
public class StationAddPage {
    
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    
    public StationAddPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        
        initializePage();
    }
    
    private void initializePage() {
        // Ana sayfa düzeni
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Sol üst ana menü butonu
        Button backToMenuButton = new Button("⬅️ Ana Menü");
        backToMenuButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        backToMenuButton.setPrefWidth(120);
        backToMenuButton.setOnAction(e -> {
            new SuperadminDashboardFX(stage, accessToken, refreshToken);
        });
        
        // Sol üst köşeye yerleştirme
        HBox topLeftBox = new HBox();
        topLeftBox.setPadding(new Insets(0, 0, 20, 0));
        topLeftBox.getChildren().add(backToMenuButton);
        
        // Üst kısım - Başlık
        VBox topContainer = createTopSection();
        
        // Üst kısmı birleştir
        VBox fullTopContainer = new VBox(10);
        fullTopContainer.getChildren().addAll(topLeftBox, topContainer);
        root.setTop(fullTopContainer);
        
        // Orta kısım - Form ve Web harita
        VBox centerContainer = createCenterSection();
        root.setCenter(centerContainer);
        
        // Alt kısım - Butonlar
        HBox bottomContainer = createBottomSection();
        root.setBottom(bottomContainer);
        
        // Sahne oluştur
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Durak Ekle - BinCard Superadmin");
        stage.setScene(scene);
        stage.show();
        
        // Token'i local dosyaya kaydet (web sayfası için)
        saveTokenToLocalFile();
        
        // Web sayfasını aç
        openMapInBrowser();
    }
    
    private VBox createTopSection() {
        VBox topContainer = new VBox(15);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Başlık
        Label titleLabel = new Label("🚌 Yeni Durak Ekle");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Açıklama
        Label descLabel = new Label("Google Maps üzerinden durak konumunu seçin ve bilgileri doldurun");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        topContainer.getChildren().addAll(titleLabel, descLabel);
        return topContainer;
    }
    
    private VBox createCenterSection() {
        VBox centerContainer = new VBox(20);
        
        // Bilgi kartı
        VBox infoCard = createInfoCard();
        
        // Form alanları
        VBox formCard = createFormCard();
        
        centerContainer.getChildren().addAll(infoCard, formCard);
        return centerContainer;
    }
    
    private VBox createInfoCard() {
        VBox infoCard = new VBox(15);
        infoCard.setPadding(new Insets(20));
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label infoTitle = new Label("📍 Durak Konumu Seçimi");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        infoTitle.setTextFill(Color.web("#2c3e50"));
        
        Label infoText = new Label("• Web tarayıcısında açılan harita üzerinden durak konumunu seçin\\n" +
                                  "• Seçilen konum otomatik olarak form alanlarına aktarılacaktır\\n" +
                                  "• Adres bilgileri Google Maps'ten otomatik doldurulur\\n" +
                                  "• Durak tipini ve diğer bilgileri manuel olarak girin");
        infoText.setFont(Font.font("System", FontWeight.NORMAL, 12));
        infoText.setTextFill(Color.web("#34495e"));
        infoText.setWrapText(true);
        
        Button openMapButton = new Button("🗺️ Haritayı Aç");
        openMapButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        openMapButton.setOnAction(e -> openMapInBrowser());
        
        infoCard.getChildren().addAll(infoTitle, infoText, openMapButton);
        return infoCard;
    }
    
    private VBox createFormCard() {
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label formTitle = new Label("📝 Durak Bilgileri");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.setTextFill(Color.web("#2c3e50"));
        
        // Form alanları için GridPane
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        
        // Durak adı
        Label nameLabel = new Label("Durak Adı:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField nameField = new TextField();
        nameField.setPromptText("Örn: Taksim Meydanı Durağı");
        nameField.setPrefWidth(300);
        
        // Durak tipi
        Label typeLabel = new Label("Durak Tipi:");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "METRO", "TRAMVAY", "OTOBUS", "METROBUS", "TREN", 
            "VAPUR", "TELEFERIK", "DOLMUS", "MINIBUS", "HAVARAY",
            "FERIBOT", "HIZLI_TREN", "BISIKLET", "SCOOTER", 
            "PARK_YERI", "AKILLI_DURAK", "TERMINAL", "ULAŞIM_AKTARMA", "DIGER"
        );
        typeCombo.setValue("OTOBUS");
        typeCombo.setPrefWidth(300);
        
        // Konum bilgileri (otomatik doldurulacak)
        Label latLabel = new Label("Enlem (Latitude):");
        latLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField latField = new TextField();
        latField.setPromptText("Haritadan otomatik doldurulacak");
        latField.setEditable(false);
        latField.setStyle("-fx-background-color: #ecf0f1;");
        
        Label lonLabel = new Label("Boylam (Longitude):");
        lonLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField lonField = new TextField();
        lonField.setPromptText("Haritadan otomatik doldurulacak");
        lonField.setEditable(false);
        lonField.setStyle("-fx-background-color: #ecf0f1;");
        
        // Adres bilgileri (otomatik doldurulacak)
        Label cityLabel = new Label("Şehir:");
        cityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField cityField = new TextField();
        cityField.setPromptText("Haritadan otomatik doldurulacak");
        
        Label districtLabel = new Label("İlçe:");
        districtLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField districtField = new TextField();
        districtField.setPromptText("Haritadan otomatik doldurulacak");
        
        Label streetLabel = new Label("Sokak:");
        streetLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField streetField = new TextField();
        streetField.setPromptText("Haritadan otomatik doldurulacak");
        
        Label postalLabel = new Label("Posta Kodu:");
        postalLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField postalField = new TextField();
        postalField.setPromptText("Opsiyonel");
        
        // Grid'e ekle
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(typeLabel, 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(latLabel, 0, 2);
        grid.add(latField, 1, 2);
        grid.add(lonLabel, 0, 3);
        grid.add(lonField, 1, 3);
        grid.add(cityLabel, 0, 4);
        grid.add(cityField, 1, 4);
        grid.add(districtLabel, 0, 5);
        grid.add(districtField, 1, 5);
        grid.add(streetLabel, 0, 6);
        grid.add(streetField, 1, 6);
        grid.add(postalLabel, 0, 7);
        grid.add(postalField, 1, 7);
        
        formCard.getChildren().addAll(formTitle, grid);
        
        // Otomatik form doldurma için timer başlat
        startAutoFormFilling(nameField, latField, lonField, cityField, districtField, streetField, postalField);
        
        return formCard;
    }
    
    private HBox createBottomSection() {
        HBox bottomContainer = new HBox(15);
        bottomContainer.setAlignment(Pos.CENTER_RIGHT);
        bottomContainer.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = new Button("💾 Durak Kaydet");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        saveButton.setPrefWidth(120);
        saveButton.setOnAction(e -> saveStation());
        
        bottomContainer.getChildren().add(saveButton);
        return bottomContainer;
    }
    
    private void saveTokenToLocalFile() {
        try {
            String userHome = System.getProperty("user.home");
            Path tokenPath = Paths.get(userHome, "token_temp.txt");
            Files.write(tokenPath, accessToken.getToken().getBytes());
            System.out.println("📁 Token dosyaya kaydedildi: " + tokenPath);
        } catch (Exception e) {
            System.err.println("❌ Token kaydetme hatası: " + e.getMessage());
        }
    }
    
    private void openMapInBrowser() {
        try {
            // HTML dosya yolu
            String userHome = System.getProperty("user.home");
            String htmlContent = createStationMapHTML();
            
            // HTML dosyasını oluştur
            File htmlFile = new File(userHome, "station_map.html");
            try (FileWriter writer = new FileWriter(htmlFile)) {
                writer.write(htmlContent);
            }
            
            // Tarayıcıda aç
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", htmlFile.getAbsolutePath());
            pb.start();
            
            System.out.println("🌐 Durak haritası tarayıcıda açıldı: " + htmlFile.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ Harita açma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String createStationMapHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>Durak Konumu Seç</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { margin: 0; padding: 20px; font-family: Arial, sans-serif; background: #f5f5f5; }
        .container { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 20px; }
        .title { font-size: 24px; font-weight: bold; color: #2c3e50; margin: 0; }
        .subtitle { color: #7f8c8d; margin: 5px 0 0 0; }
        #map { height: 400px; width: 100%; border-radius: 8px; margin: 20px 0; }
        .info-panel { background: #ecf0f1; padding: 15px; border-radius: 8px; margin: 10px 0; }
        .coordinates { font-family: monospace; font-size: 14px; }
        .btn { background: #3498db; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin: 5px; }
        .btn:hover { background: #2980b9; }
        .btn-success { background: #27ae60; }
        .btn-success:hover { background: #229954; }
        .form-row { margin: 10px 0; }
        .form-row label { display: inline-block; width: 120px; font-weight: bold; }
        .form-row input { width: 300px; padding: 5px; border: 1px solid #bdc3c7; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="title">🚌 Durak Konumu Seç</h1>
            <p class="subtitle">Harita üzerinden durak konumunu seçin</p>
        </div>
        
        <div id="map"></div>
        
        <div class="info-panel">
            <h3>📍 Seçilen Konum Bilgileri</h3>
            <div class="coordinates">
                <div class="form-row">
                    <label>Enlem:</label>
                    <input type="text" id="latitude" readonly style="background: #f8f9fa;">
                </div>
                <div class="form-row">
                    <label>Boylam:</label>
                    <input type="text" id="longitude" readonly style="background: #f8f9fa;">
                </div>
                <div class="form-row">
                    <label>Şehir:</label>
                    <input type="text" id="city" readonly style="background: #f8f9fa;">
                </div>
                <div class="form-row">
                    <label>İlçe:</label>
                    <input type="text" id="district" readonly style="background: #f8f9fa;">
                </div>
                <div class="form-row">
                    <label>Sokak:</label>
                    <input type="text" id="street" readonly style="background: #f8f9fa;">
                </div>
                <div class="form-row">
                    <label>Posta Kodu:</label>
                    <input type="text" id="postal_code" readonly style="background: #f8f9fa;">
                </div>
            </div>
            <div style="text-align: center; margin-top: 15px;">
                <button class="btn btn-success" onclick="saveLocationData()">💾 Konumu Kaydet</button>
                <button class="btn" onclick="centerToIstanbul()">📍 İstanbul'a Git</button>
            </div>
        </div>
    </div>

    <script>
        let map;
        let marker;
        let selectedLocation = null;

        function initMap() {
            // İstanbul merkez konumu
            const istanbul = { lat: 41.0082, lng: 28.9784 };
            
            map = new google.maps.Map(document.getElementById("map"), {
                zoom: 13,
                center: istanbul,
                mapTypeId: google.maps.MapTypeId.ROADMAP,
                styles: [
                    {
                        featureType: "poi",
                        elementType: "labels",
                        stylers: [{ visibility: "off" }]
                    }
                ]
            });

            // Harita tıklama olayı
            map.addListener("click", (event) => {
                placeMarker(event.latLng);
                getAddressFromCoordinates(event.latLng.lat(), event.latLng.lng());
            });

            // İlk marker'ı İstanbul'a yerleştir
            placeMarker(new google.maps.LatLng(istanbul.lat, istanbul.lng));
            getAddressFromCoordinates(istanbul.lat, istanbul.lng);
        }

        function placeMarker(location) {
            if (marker) {
                marker.setMap(null);
            }
            
            marker = new google.maps.Marker({
                position: location,
                map: map,
                title: "Seçilen Durak Konumu",
                icon: {
                    url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#e74c3c">
                            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                        </svg>
                    `),
                    scaledSize: new google.maps.Size(32, 32)
                }
            });

            selectedLocation = location;
            
            // Koordinatları göster
            document.getElementById('latitude').value = location.lat().toFixed(6);
            document.getElementById('longitude').value = location.lng().toFixed(6);
        }

        function getAddressFromCoordinates(lat, lng) {
            const geocoder = new google.maps.Geocoder();
            const latlng = { lat: lat, lng: lng };

            geocoder.geocode({ location: latlng }, (results, status) => {
                if (status === "OK" && results[0]) {
                    parseAddressComponents(results[0]);
                } else {
                    console.error("Adres bulunamadı: " + status);
                }
            });
        }

        function parseAddressComponents(result) {
            let city = "";
            let district = "";
            let street = "";
            let postal_code = "";

            for (let component of result.address_components) {
                const types = component.types;
                
                if (types.includes("administrative_area_level_1")) {
                    city = component.long_name;
                } else if (types.includes("administrative_area_level_2")) {
                    district = component.long_name;
                } else if (types.includes("route") || types.includes("street_address")) {
                    street = component.long_name;
                } else if (types.includes("postal_code")) {
                    postal_code = component.long_name;
                }
            }

            // Form alanlarını doldur
            document.getElementById('city').value = city;
            document.getElementById('district').value = district;
            document.getElementById('street').value = street;
            document.getElementById('postal_code').value = postal_code;
        }

        function centerToIstanbul() {
            const istanbul = { lat: 41.0082, lng: 28.9784 };
            map.setCenter(istanbul);
            map.setZoom(13);
        }

        function saveLocationData() {
            if (!selectedLocation) {
                alert("Lütfen önce harita üzerinden bir konum seçin!");
                return;
            }

            const locationData = {
                latitude: parseFloat(document.getElementById('latitude').value),
                longitude: parseFloat(document.getElementById('longitude').value),
                city: document.getElementById('city').value,
                district: document.getElementById('district').value,
                street: document.getElementById('street').value,
                postal_code: document.getElementById('postal_code').value,
                timestamp: new Date().toISOString()
            };

            // Veriyi dosyaya kaydet
            const jsonData = JSON.stringify(locationData, null, 2);
            const blob = new Blob([jsonData], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            
            const a = document.createElement('a');
            a.href = url;
            a.download = 'station_location.json';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);

            alert("✅ Konum bilgileri başarıyla kaydedildi!");
        }
    </script>
    
    <script async defer 
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBpKV71_29LUW6kKj2aH4CjSFrFJCN8Ye4&callback=initMap&libraries=geometry">
    </script>
</body>
</html>
        """;
    }
    
    private void startAutoFormFilling(TextField nameField, TextField latField, TextField lonField, 
                                    TextField cityField, TextField districtField, TextField streetField, TextField postalField) {
        // Downloads klasöründe JSON dosyasını kontrol et
        Task<Void> checkTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String downloadsPath = System.getProperty("user.home") + "\\Downloads";
                File downloadsDir = new File(downloadsPath);
                
                while (!isCancelled()) {
                    // station_location.json dosyasını ara
                    File[] jsonFiles = downloadsDir.listFiles((dir, name) -> 
                        name.equals("station_location.json") || name.matches("station_location \\(\\d+\\)\\.json"));
                    
                    if (jsonFiles != null && jsonFiles.length > 0) {
                        // En son oluşturulan dosyayı al
                        File latestFile = null;
                        long latestTime = 0;
                        for (File file : jsonFiles) {
                            if (file.lastModified() > latestTime) {
                                latestTime = file.lastModified();
                                latestFile = file;
                            }
                        }
                        
                        if (latestFile != null) {
                            processLocationFile(latestFile, nameField, latField, lonField, cityField, districtField, streetField, postalField);
                            break;
                        }
                    }
                    
                    Thread.sleep(2000); // 2 saniye bekle
                }
                return null;
            }
        };
        
        Thread checkThread = new Thread(checkTask);
        checkThread.setDaemon(true);
        checkThread.start();
    }
    
    private void processLocationFile(File file, TextField nameField, TextField latField, TextField lonField, 
                                   TextField cityField, TextField districtField, TextField streetField, TextField postalField) {
        try {
            String content = Files.readString(file.toPath());
            System.out.println("📍 Konum dosyası okundu: " + content);
            
            // JSON parse etme (basit string parsing)
            String latitude = extractJsonValue(content, "latitude");
            String longitude = extractJsonValue(content, "longitude");
            String city = extractJsonValue(content, "city");
            String district = extractJsonValue(content, "district");
            String street = extractJsonValue(content, "street");
            String postalCode = extractJsonValue(content, "postal_code");
            
            Platform.runLater(() -> {
                latField.setText(latitude);
                lonField.setText(longitude);
                cityField.setText(city);
                districtField.setText(district);
                streetField.setText(street);
                postalField.setText(postalCode);
                
                // Durak adını otomatik oluştur
                String autoName = "";
                if (!street.isEmpty()) {
                    autoName = street + " Durağı";
                } else if (!district.isEmpty()) {
                    autoName = district + " Durağı";
                } else if (!city.isEmpty()) {
                    autoName = city + " Durağı";
                }
                
                if (!autoName.isEmpty() && nameField.getText().isEmpty()) {
                    nameField.setText(autoName);
                }
                
                showSuccessAlert("📍 Konum Başarıyla Alındı", "Harita üzerinden seçilen konum form alanlarına aktarıldı.");
            });
            
            // Dosyayı sil
            file.delete();
            
        } catch (Exception e) {
            System.err.println("❌ Dosya işleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return "";
            
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return "";
            
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            if (valueStart >= json.length()) return "";
            
            if (json.charAt(valueStart) == '"') {
                // String değer
                valueStart++;
                int valueEnd = json.indexOf('"', valueStart);
                if (valueEnd == -1) return "";
                return json.substring(valueStart, valueEnd);
            } else {
                // Sayısal değer
                int valueEnd = valueStart;
                while (valueEnd < json.length() && 
                       (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '.' || json.charAt(valueEnd) == '-')) {
                    valueEnd++;
                }
                return json.substring(valueStart, valueEnd);
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    private void saveStation() {
        System.out.println("🔍 DEBUG: saveStation() çağrıldı");
        System.out.println("🔍 DEBUG: AccessToken var mı? " + (accessToken != null ? "Evet - " + accessToken.getToken().substring(0, 20) + "..." : "Hayır"));
        
        // Form verilerini simüle et (gerçek form alanları henüz eklenmemiş)
        String stationName = "Örnek Durak"; // stationNameField.getText().trim();
        String description = "Test açıklaması"; // descriptionField.getText().trim();
        String latitude = "41.0082"; // latitudeField.getText().trim();
        String longitude = "28.9784"; // longitudeField.getText().trim();
        String address = "İstanbul, Türkiye"; // addressField.getText().trim();
        String stationType = "OTOBUS"; // typeComboBox.getValue();
        String city = "İstanbul"; // cityField.getText().trim();
        String district = "Fatih"; // districtField.getText().trim();
        
        System.out.println("🔍 DEBUG: Form verileri - Name: " + stationName + ", Type: " + stationType + ", Lat: " + latitude + ", Lng: " + longitude);
        
        // Temel validasyon
        if (stationName.isEmpty()) {
            showErrorAlert("❌ Hata", "Durak adı boş olamaz!");
            return;
        }
        
        if (latitude.isEmpty() || longitude.isEmpty()) {
            showErrorAlert("❌ Hata", "Konum bilgileri eksik! Lütfen haritadan konum seçin.");
            return;
        }
        
        if (stationType == null || stationType.isEmpty()) {
            showErrorAlert("❌ Hata", "Durak tipi seçilmeli!");
            return;
        }
        
        // JSON formatında durak verisi oluştur
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"name\":\"").append(stationName).append("\",");
        jsonBuilder.append("\"description\":\"").append(description).append("\",");
        jsonBuilder.append("\"location\":{");
        jsonBuilder.append("\"latitude\":").append(latitude).append(",");
        jsonBuilder.append("\"longitude\":").append(longitude);
        jsonBuilder.append("},");
        jsonBuilder.append("\"address\":{");
        jsonBuilder.append("\"fullAddress\":\"").append(address).append("\",");
        jsonBuilder.append("\"city\":\"").append(city).append("\",");
        jsonBuilder.append("\"district\":\"").append(district).append("\"");
        jsonBuilder.append("},");
        jsonBuilder.append("\"type\":\"").append(stationType).append("\",");
        jsonBuilder.append("\"active\":true");
        jsonBuilder.append("}");
        
        String stationJson = jsonBuilder.toString();
        
        System.out.println("🔍 DEBUG: Oluşturulan JSON: " + stationJson);
        
        showInfoAlert("💾 Durak Kaydediliyor", "Durak bilgileri backend'e gönderiliyor...");
        
        // API'ye gönder
        Task<String> saveTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                System.out.println("🔍 DEBUG: StationApiClient.createStation() çağrılıyor...");
                String result = StationApiClient.createStation(accessToken, stationJson);
                System.out.println("🔍 DEBUG: API sonucu döndü: " + result);
                return result;
            }
            
            @Override
            protected void succeeded() {
                String result = getValue();
                if (result != null && !result.isEmpty()) {
                    System.out.println("✅ Durak kaydedildi: " + result);
                    showSuccessAlert("✅ Başarılı", "Durak başarıyla kaydedildi!\n\nAPI Yanıtı: " + result);
                    
                    // 3 saniye sonra ana menüye dön
                    Platform.runLater(() -> {
                        new SuperadminDashboardFX(stage, accessToken, refreshToken);
                    });
                } else {
                    showErrorAlert("❌ Hata", "Durak kaydedilemedi! API yanıtı boş.");
                }
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                System.err.println("❌ Durak kaydetme hatası: " + exception.getMessage());
                showErrorAlert("❌ API Hatası", "Durak kaydedilemedi!\n\nHata: " + exception.getMessage());
            }
        };
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
        
        System.out.println("📝 Durak kaydediliyor - JSON: " + stationJson);
    }
    
    private void showSuccessAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
    
    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
