package com.bincard.bincard_superadmin;

import com.bincard.bincard_superadmin.model.PaymentPoint;
import com.bincard.bincard_superadmin.model.PaymentMethod;
import com.bincard.bincard_superadmin.model.Location;
import com.bincard.bincard_superadmin.model.Address;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ChoiceDialog;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Locale;

public class PaymentPointsMapPage extends SuperadminPageBase {
    
    private List<PaymentPoint> paymentPointsList;
    private WebView mapWebView;
    private WebEngine webEngine;
    
    // Static metod - PaymentPointsTablePage'den çağrılabilir
    public static void showMap(Stage stage, List<PaymentPoint> paymentPoints) {
        try {
            // Yeni bir Stage oluştur
            Stage mapStage = new Stage();
            mapStage.setTitle("Ödeme Noktaları - Harita Görünümü");
            mapStage.setWidth(1200);
            mapStage.setHeight(800);
            
            // Harita sayfasını oluştur
            // Dummy token'lar - gerçek uygulamada mevcut token'ları kullanın
            TokenDTO dummyAccessToken = new TokenDTO("dummy", 
                java.time.LocalDateTime.now(), 
                java.time.LocalDateTime.now().plusHours(1), 
                java.time.LocalDateTime.now(), 
                "127.0.0.1", 
                "dummy", 
                TokenType.ACCESS);
            TokenDTO dummyRefreshToken = new TokenDTO("dummy", 
                java.time.LocalDateTime.now(), 
                java.time.LocalDateTime.now().plusDays(7), 
                java.time.LocalDateTime.now(), 
                "127.0.0.1", 
                "dummy", 
                TokenType.REFRESH);
            
            PaymentPointsMapPage mapPage = new PaymentPointsMapPage(mapStage, dummyAccessToken, dummyRefreshToken);
            
            // Verilen payment point listesini ayarla
            if (paymentPoints != null) {
                mapPage.paymentPointsList = new ArrayList<>(paymentPoints);
            }
            
            // Haritayı göster
            mapStage.show();
            
        } catch (Exception e) {
            System.err.println("Harita gösterilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public PaymentPointsMapPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktaları - Harita Görünümü");
        this.paymentPointsList = new ArrayList<>();
        loadPaymentPointsData();
    }
    
    @Override
    protected javafx.scene.Node createContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        Label titleLabel = new Label("Ödeme Noktaları Haritası");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Kontrol butonları
        HBox controlsBox = createControlsBox();
        
        // Harita WebView
        mapWebView = new WebView();
        mapWebView.setPrefSize(800, 600);
        webEngine = mapWebView.getEngine();
        
        // Harita yükleme
        loadMapWithPoints();
        
        mainContent.getChildren().addAll(titleLabel, controlsBox, mapWebView);
        
        return mainContent;
    }
    
    private HBox createControlsBox() {
        HBox controlsBox = new HBox(15);
        controlsBox.setStyle("-fx-padding: 10; -fx-alignment: center-left;");
        
        Button refreshButton = new Button("🔄 Haritayı Yenile");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> {
            loadPaymentPointsData();
            loadMapWithPoints();
        });
        
        Button centerButton = new Button("📍 Merkeze Odakla");
        centerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5;");
        centerButton.setOnAction(e -> centerMap());
        
        Button tableViewButton = new Button("📋 Tablo Görünümü");
        tableViewButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5;");
        tableViewButton.setOnAction(e -> {
            PaymentPointsTablePage tablePage = new PaymentPointsTablePage(stage, accessToken, refreshToken);
            showPage(tablePage);
        });
        
        Label infoLabel = new Label("Toplam " + paymentPointsList.size() + " ödeme noktası");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        controlsBox.getChildren().addAll(refreshButton, centerButton, tableViewButton, spacer, infoLabel);
        
        return controlsBox;
    }
    
    private void loadPaymentPointsData() {
        new Thread(() -> {
            try {
                System.out.println("🗺️ Harita için ödeme noktaları yükleniyor...");
                String response = ApiClientFX.getAllPaymentPoints(accessToken);
                
                if (response != null && !response.isEmpty()) {
                    paymentPointsList = parsePaymentPointsResponse(response);
                    System.out.println("✅ " + paymentPointsList.size() + " ödeme noktası yüklendi");
                } else {
                    System.err.println("❌ API'den boş yanıt alındı");
                    paymentPointsList = new ArrayList<>();
                }
                
                Platform.runLater(() -> {
                    loadMapWithPoints();
                    updateInfoLabel();
                });
                
            } catch (Exception e) {
                System.err.println("❌ Ödeme noktaları yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                paymentPointsList = new ArrayList<>();
                
                Platform.runLater(() -> {
                    showAlert("Ödeme noktaları yüklenirken hata oluştu: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void loadMapWithPoints() {
        if (webEngine == null) return;
        
        String htmlContent = generateMapHTML();
        webEngine.loadContent(htmlContent);
    }
    
    private String generateMapHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset='utf-8'>\n");
        html.append("    <title>Ödeme Noktaları Haritası</title>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1'>\n");
        html.append("    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />\n");
        html.append("    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n");
        html.append("    <style>\n");
        html.append("        body { margin: 0; padding: 0; font-family: Arial, sans-serif; }\n");
        html.append("        #map { height: 100vh; width: 100vw; }\n");
        html.append("        .custom-popup {\n");
        html.append("            font-family: Arial, sans-serif;\n");
        html.append("            max-width: 300px;\n");
        html.append("        }\n");
        html.append("        .popup-title {\n");
        html.append("            font-weight: bold;\n");
        html.append("            font-size: 16px;\n");
        html.append("            color: #2c3e50;\n");
        html.append("            margin-bottom: 8px;\n");
        html.append("        }\n");
        html.append("        .popup-info {\n");
        html.append("            font-size: 14px;\n");
        html.append("            color: #34495e;\n");
        html.append("            margin-bottom: 4px;\n");
        html.append("        }\n");
        html.append("        .popup-status {\n");
        html.append("            padding: 2px 8px;\n");
        html.append("            border-radius: 12px;\n");
        html.append("            font-size: 12px;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .status-active {\n");
        html.append("            background-color: #d4edda;\n");
        html.append("            color: #155724;\n");
        html.append("        }\n");
        html.append("        .status-inactive {\n");
        html.append("            background-color: #f8d7da;\n");
        html.append("            color: #721c24;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div id='map'></div>\n");
        html.append("    <script>\n");
        
        // Harita başlatma
        html.append("        var map = L.map('map').setView([41.0082, 28.9784], 10);\n"); // İstanbul merkezli
        html.append("        \n");
        html.append("        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n");
        html.append("            attribution: '© OpenStreetMap contributors'\n");
        html.append("        }).addTo(map);\n");
        html.append("        \n");
        
        // Ödeme noktalarını marker olarak ekle
        html.append("        var markers = [];\n");
        html.append("        \n");
        
        for (PaymentPoint point : paymentPointsList) {
            if (point.getLocation() != null && 
                point.getLocation().getLatitude() != null && 
                point.getLocation().getLongitude() != null) {
                
                double lat = point.getLocation().getLatitude();
                double lng = point.getLocation().getLongitude();
                
                // Marker rengi: aktif = yeşil, pasif = kırmızı
                String markerColor = point.isActive() ? "green" : "red";
                
                html.append("        var marker").append(point.getId()).append(" = L.marker([")
                    .append(lat).append(", ").append(lng).append("], {\n");
                html.append("            icon: L.icon({\n");
                html.append("                iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-").append(markerColor).append(".png',\n");
                html.append("                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',\n");
                html.append("                iconSize: [25, 41],\n");
                html.append("                iconAnchor: [12, 41],\n");
                html.append("                popupAnchor: [1, -34],\n");
                html.append("                shadowSize: [41, 41]\n");
                html.append("            })\n");
                html.append("        }).addTo(map);\n");
                
                // Popup içeriği
                html.append("        marker").append(point.getId()).append(".bindPopup(`\n");
                html.append("            <div class='custom-popup'>\n");
                html.append("                <div class='popup-title'>").append(escapeHtml(point.getName())).append("</div>\n");
                
                if (point.getAddress() != null) {
                    html.append("                <div class='popup-info'><strong>📍 Adres:</strong> ")
                        .append(escapeHtml(getFullAddress(point.getAddress()))).append("</div>\n");
                }
                
                if (point.getContactNumber() != null && !point.getContactNumber().isEmpty()) {
                    html.append("                <div class='popup-info'><strong>📞 Telefon:</strong> ")
                        .append(escapeHtml(point.getContactNumber())).append("</div>\n");
                }
                
                if (point.getWorkingHours() != null && !point.getWorkingHours().isEmpty()) {
                    html.append("                <div class='popup-info'><strong>🕒 Çalışma Saatleri:</strong> ")
                        .append(escapeHtml(point.getWorkingHours())).append("</div>\n");
                }
                
                if (point.getPaymentMethods() != null && !point.getPaymentMethods().isEmpty()) {
                    html.append("                <div class='popup-info'><strong>💳 Ödeme Yöntemleri:</strong> ")
                        .append(escapeHtml(getPaymentMethodsString(point.getPaymentMethods()))).append("</div>\n");
                }
                
                html.append("                <div class='popup-info'><strong>📊 Durum:</strong> ")
                    .append("<span class='popup-status ").append(point.isActive() ? "status-active" : "status-inactive").append("'>")
                    .append(point.isActive() ? "Aktif" : "Pasif").append("</span></div>\n");
                
                html.append("            </div>\n");
                html.append("        `);\n");
                
                html.append("        markers.push(marker").append(point.getId()).append(");\n");
            }
        }
        
        // Tüm marker'ları kapsayacak şekilde haritayı ayarla
        html.append("        \n");
        html.append("        if (markers.length > 0) {\n");
        html.append("            var group = new L.featureGroup(markers);\n");
        html.append("            map.fitBounds(group.getBounds().pad(0.1));\n");
        html.append("        }\n");
        html.append("        \n");
        
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String getFullAddress(Address address) {
        StringBuilder addr = new StringBuilder();
        if (address.getStreet() != null && !address.getStreet().isEmpty()) {
            addr.append(address.getStreet()).append(", ");
        }
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            addr.append(address.getDistrict()).append(", ");
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            addr.append(address.getCity());
        }
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            addr.append(" ").append(address.getPostalCode());
        }
        return addr.toString();
    }
    
    private String getPaymentMethodsString(List<PaymentMethod> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return "Belirtilmemiş";
        }
        
        StringBuilder methods = new StringBuilder();
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (i > 0) methods.append(", ");
            methods.append(paymentMethods.get(i).getDisplayName());
        }
        return methods.toString();
    }
    
    private void centerMap() {
        if (webEngine != null) {
            webEngine.executeScript("map.setView([41.0082, 28.9784], 10);");
        }
    }
    
    private void updateInfoLabel() {
        // Bu metod kontrol butonlarındaki bilgi labelini günceller
        // Şimdilik basit bir implementasyon
    }
    
    // JSON parse metodu
    private List<PaymentPoint> parsePaymentPointsResponse(String response) {
        List<PaymentPoint> paymentPoints = new ArrayList<>();
        try {
            // JSON'dan content array'ini çıkar
            String contentStart = "\"content\":[";
            int startIndex = response.indexOf(contentStart);
            if (startIndex == -1) return paymentPoints;
            
            startIndex += contentStart.length();
            int endIndex = response.indexOf("]", startIndex);
            if (endIndex == -1) return paymentPoints;
            
            String contentArray = response.substring(startIndex, endIndex);
            
            // Her payment point objesini parse et
            int objStart = 0;
            while ((objStart = contentArray.indexOf("{", objStart)) != -1) {
                int objEnd = findMatchingBrace(contentArray, objStart);
                if (objEnd == -1) break;
                
                String objStr = contentArray.substring(objStart, objEnd + 1);
                PaymentPoint point = parsePaymentPointObject(objStr);
                if (point != null) {
                    paymentPoints.add(point);
                }
                
                objStart = objEnd + 1;
            }
        } catch (Exception e) {
            System.err.println("JSON parse hatası: " + e.getMessage());
        }
        return paymentPoints;
    }
    
    private int findMatchingBrace(String str, int start) {
        int braceCount = 0;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '{') braceCount++;
            else if (str.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) return i;
            }
        }
        return -1;
    }
    
    private PaymentPoint parsePaymentPointObject(String objStr) {
        try {
            PaymentPoint point = new PaymentPoint();
            
            // ID
            String id = extractStringValue(objStr, "id");
            if (id != null) {
                try {
                    point.setId(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    // ID parse edilemezse atla
                }
            }
            
            // Name
            String name = extractStringValue(objStr, "name");
            if (name != null) point.setName(name);
            
            // Description
            String description = extractStringValue(objStr, "description");
            if (description != null) point.setDescription(description);
            
            // Contact Number
            String contactNumber = extractStringValue(objStr, "contactNumber");
            if (contactNumber != null) point.setContactNumber(contactNumber);
            
            // Working Hours
            String workingHours = extractStringValue(objStr, "workingHours");
            if (workingHours != null) point.setWorkingHours(workingHours);
            
            // Location
            Location location = parseLocation(objStr);
            if (location != null) point.setLocation(location);
            
            // Address
            Address address = parseAddress(objStr);
            if (address != null) point.setAddress(address);
            
            // Payment Methods
            List<PaymentMethod> methods = parsePaymentMethods(objStr);
            if (methods != null) point.setPaymentMethods(methods);
            
            // Status - active field'i kullan
            String active = extractStringValue(objStr, "active");
            if (active != null) point.setActive(Boolean.parseBoolean(active));
            
            return point;
        } catch (Exception e) {
            System.err.println("PaymentPoint parse hatası: " + e.getMessage());
            return null;
        }
    }
    
    private String extractStringValue(String json, String key) {
        // Hem string hem de numeric değerleri destekle
        String stringPattern = "\"" + key + "\":\"";
        String numericPattern = "\"" + key + "\":";
        
        int startIndex = json.indexOf(stringPattern);
        if (startIndex != -1) {
            // String değer
            startIndex += stringPattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        } else {
            // Numeric değer
            startIndex = json.indexOf(numericPattern);
            if (startIndex != -1) {
                startIndex += numericPattern.length();
                int endIndex = startIndex;
                while (endIndex < json.length() && 
                       (Character.isDigit(json.charAt(endIndex)) || 
                        json.charAt(endIndex) == '.' || 
                        json.charAt(endIndex) == '-')) {
                    endIndex++;
                }
                if (endIndex > startIndex) {
                    return json.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }
    
    private Location parseLocation(String objStr) {
        try {
            String locationStart = "\"location\":{";
            int startIndex = objStr.indexOf(locationStart);
            if (startIndex == -1) return null;
            
            startIndex += locationStart.length();
            int endIndex = findMatchingBrace(objStr, startIndex - 1);
            if (endIndex == -1) return null;
            
            String locationStr = objStr.substring(startIndex, endIndex);
            
            Location location = new Location();
            String latitude = extractStringValue(locationStr, "latitude");
            String longitude = extractStringValue(locationStr, "longitude");
            
            if (latitude != null) location.setLatitude(Double.parseDouble(latitude));
            if (longitude != null) location.setLongitude(Double.parseDouble(longitude));
            
            return location;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Address parseAddress(String objStr) {
        try {
            String addressStart = "\"address\":{";
            int startIndex = objStr.indexOf(addressStart);
            if (startIndex == -1) return null;
            
            startIndex += addressStart.length();
            int endIndex = findMatchingBrace(objStr, startIndex - 1);
            if (endIndex == -1) return null;
            
            String addressStr = objStr.substring(startIndex, endIndex);
            
            Address address = new Address();
            String street = extractStringValue(addressStr, "street");
            String city = extractStringValue(addressStr, "city");
            String district = extractStringValue(addressStr, "district");
            String postalCode = extractStringValue(addressStr, "postalCode");
            
            if (street != null) address.setStreet(street);
            if (city != null) address.setCity(city);
            if (district != null) address.setDistrict(district);
            if (postalCode != null) address.setPostalCode(postalCode);
            
            return address;
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<PaymentMethod> parsePaymentMethods(String objStr) {
        List<PaymentMethod> methods = new ArrayList<>();
        try {
            String methodsStart = "\"paymentMethods\":[";
            int startIndex = objStr.indexOf(methodsStart);
            if (startIndex == -1) return methods;
            
            startIndex += methodsStart.length();
            int endIndex = objStr.indexOf("]", startIndex);
            if (endIndex == -1) return methods;
            
            String methodsArray = objStr.substring(startIndex, endIndex);
            
            // Her payment method'u parse et
            int methodStart = 0;
            while ((methodStart = methodsArray.indexOf("\"", methodStart)) != -1) {
                int methodEnd = methodsArray.indexOf("\"", methodStart + 1);
                if (methodEnd == -1) break;
                
                String methodStr = methodsArray.substring(methodStart + 1, methodEnd);
                try {
                    PaymentMethod method = PaymentMethod.valueOf(methodStr);
                    methods.add(method);
                } catch (IllegalArgumentException e) {
                    // Geçersiz method, atla
                }
                
                methodStart = methodEnd + 1;
            }
        } catch (Exception e) {
            System.err.println("PaymentMethods parse hatası: " + e.getMessage());
        }
        return methods;
    }
    
    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Harita Hatası");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showPage(SuperadminPageBase page) {
        try {
            Scene scene = new Scene(page.root, 1200, 800);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Sayfa gösterilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
