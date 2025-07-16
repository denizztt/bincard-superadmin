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

public class PaymentPointsMapPage {
    
    // Yeni constructor - SuperadminPageBase tarzı
    public PaymentPointsMapPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        // Şimdilik tüm ödeme noktalarını yükle ve göster
        new Thread(() -> {
            try {
                String response = ApiClientFX.getAllPaymentPoints(accessToken);
                List<PaymentPoint> allPoints = new ArrayList<>();
                if (response != null && !response.isEmpty()) {
                    allPoints = parsePaymentPointsResponse(response);
                }
                final List<PaymentPoint> finalPoints = allPoints;
                javafx.application.Platform.runLater(() -> {
                    try {
                        displayMap(stage, finalPoints);
                    } catch (Exception ex) {
                        System.err.println("Harita gösterilirken hata: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("Ödeme noktaları alınırken hata: " + ex.getMessage());
                    ex.printStackTrace();
                });
            }
        }).start();
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
    
    public static void showMap(Stage owner, List<PaymentPoint> paymentPoints) {
        javafx.application.Platform.runLater(() -> {
            // Kullanıcıya seçenek sun: WebView mi, tarayıcı mı?
            List<String> choices = new ArrayList<>();
            choices.add("JavaFX WebView");
            choices.add("Varsayılan Tarayıcıda Aç");
            ChoiceDialog<String> dialog = new ChoiceDialog<>("JavaFX WebView", choices);
            dialog.setTitle("Harita Görüntüleme Seçimi");
            dialog.setHeaderText("Haritayı nasıl görüntülemek istersiniz?");
            dialog.setContentText("Bir seçenek seçin:");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return; // Kullanıcı iptal etti
            }
            String selected = result.get();

            List<PaymentPoint> pointsToShow;
            if (paymentPoints == null || paymentPoints.isEmpty()) {
                System.out.println("[DEBUG] Haritada gösterilecek veri yok. Marker eklenmeyecek.");
                pointsToShow = new java.util.ArrayList<>();
            } else {
                System.out.println("[DEBUG] Haritada gösterilecek ödeme noktası sayısı: " + paymentPoints.size());
                pointsToShow = paymentPoints;
            }

            // HTML ve JS ile Leaflet haritası oluştur
            StringBuilder markersJs = new StringBuilder();
            for (PaymentPoint point : pointsToShow) {
                if (point.getLocation() != null && point.getLocation().getLatitude() != 0.0 && point.getLocation().getLongitude() != 0.0) {
                    String address = "";
                    if (point.getAddress() != null) {
                        address = String.format("%s, %s, %s %s",
                            point.getAddress().getStreet() != null ? point.getAddress().getStreet() : "",
                            point.getAddress().getDistrict() != null ? point.getAddress().getDistrict() : "",
                            point.getAddress().getCity() != null ? point.getAddress().getCity() : "",
                            point.getAddress().getPostalCode() != null ? point.getAddress().getPostalCode() : "");
                    }
                    String description = point.getDescription() != null && !point.getDescription().isEmpty() ? point.getDescription() : "Açıklama yok";
                    String popupHtml = String.format(
                        "<b>%s</b><br/><span style='font-size:12px;'>%s</span><br/><i style='color:#555;'>%s</i>",
                        point.getName().replace("'", " "),
                        address.replace("'", " "),
                        description.replace("'", " ")
                    );
                    markersJs.append(String.format(java.util.Locale.US,
                        "L.marker([%f, %f]).addTo(map).bindPopup('%s');\n",
                        point.getLocation().getLatitude(), point.getLocation().getLongitude(), popupHtml
                    ));
                }
            }
            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='utf-8'/>
                <!-- <meta http-equiv=\"Content-Security-Policy\" content=\"default-src * 'unsafe-inline' 'unsafe-eval'\"> -->
                <title>Ödeme Noktaları Harita</title>
                <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
                <style> #map { width: 100vw; height: 100vh; } body { margin:0; } </style>
            </head>
            <body>
                <div id='map'></div>
                <div id='debug' style='color:red; font-size:14px;'></div>
                <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
                <script>
                    document.getElementById('debug').innerText = 'JavaScript çalışıyor!';
                    try {
                        var map = L.map('map').setView([39.0, 35.0], 6);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 18,
                            attribution: '© OpenStreetMap'
                        }).addTo(map);
                        %s
                    } catch (e) {
                        document.getElementById('debug').innerText = 'Harita yüklenemedi: ' + e;
                    }
                </script>
            </body>
            </html>
            """.formatted(markersJs.toString());

            // DEBUG: HTML içeriğini konsola yazdır
            System.out.println("[DEBUG] Harita HTML içeriği:\n" + html);

            if (selected.equals("JavaFX WebView")) {
                // WebView ile göster
                Stage mapStage = new Stage();
                mapStage.initOwner(owner);
                mapStage.initModality(Modality.APPLICATION_MODAL);
                mapStage.setTitle("Ödeme Noktalarını Haritada Göster");
                mapStage.setWidth(900);
                mapStage.setHeight(600);

                BorderPane root = new BorderPane();
                WebView webView = new WebView();
                WebEngine webEngine = webView.getEngine();
                webEngine.loadContent(html);
                webView.setPrefSize(900, 600);
                webView.setMinSize(600, 400);
                webView.setVisible(true);
                root.setCenter(webView);
                mapStage.setScene(new Scene(root));
                mapStage.showAndWait();
            } else {
                // Tarayıcıda göster
                try {
                    File tempFile = Files.createTempFile("payment_points_map_", ".html").toFile();
                    tempFile.deleteOnExit();
                    try (FileWriter writer = new FileWriter(tempFile)) {
                        writer.write(html);
                    }
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(tempFile.toURI());
                    } else {
                        System.err.println("[ERROR] Desktop API desteklenmiyor. Harita açılamadı.");
                    }
                } catch (Exception ex) {
                    System.err.println("[ERROR] Harita tarayıcıda açılırken hata: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void displayMap(Stage owner, List<PaymentPoint> allPoints) {
        // Geçerli koordinatları olan noktaları filtrele
        List<PaymentPoint> validPoints = new ArrayList<>();
        for (PaymentPoint point : allPoints) {
            if (point.getLocation() != null && 
                point.getLocation().getLatitude() != 0.0 && 
                point.getLocation().getLongitude() != 0.0) {
                validPoints.add(point);
                System.out.println("Geçerli nokta: " + point.getName() + " - " + 
                    point.getLocation().getLatitude() + ", " + point.getLocation().getLongitude());
            }
        }
        
        System.out.println("Toplam " + allPoints.size() + " nokta, " + validPoints.size() + " geçerli nokta");
        
        if (validPoints.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Harita Gösterilemiyor");
            alert.setContentText("Geçerli koordinatları olan ödeme noktası bulunamadı.");
            alert.showAndWait();
            return;
        }
        
        // WebView ile harita göster
        Stage mapStage = new Stage();
        mapStage.initOwner(owner);
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("Ödeme Noktaları Haritası (" + validPoints.size() + " nokta)");
        mapStage.setWidth(1000);
        mapStage.setHeight(700);

        BorderPane root = new BorderPane();
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        // HTML içeriğini oluştur
        StringBuilder markersJs = new StringBuilder();
        for (PaymentPoint point : validPoints) {
            String address = "";
            if (point.getAddress() != null) {
                address = String.format("%s, %s, %s",
                    point.getAddress().getStreet() != null ? point.getAddress().getStreet() : "",
                    point.getAddress().getDistrict() != null ? point.getAddress().getDistrict() : "",
                    point.getAddress().getCity() != null ? point.getAddress().getCity() : "");
            }
            
            String status = point.isActive() ? "Aktif" : "Pasif";
            String statusColor = point.isActive() ? "green" : "red";
            
            String popupHtml = String.format(
                "<b>%s</b><br/><span style='font-size:12px;'>%s</span><br/><span style='color:%s;'>%s</span>",
                escapeHtml(point.getName()),
                escapeHtml(address),
                statusColor,
                status
            );
            
            markersJs.append(String.format(Locale.US,
                "L.marker([%f, %f]).addTo(map).bindPopup('%s');\n",
                point.getLocation().getLatitude(), 
                point.getLocation().getLongitude(), 
                escapeHtml(popupHtml)
            ));
        }
        
        String html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='utf-8'/>
            <title>Ödeme Noktaları Harita</title>
            <meta name='viewport' content='width=device-width, initial-scale=1.0'>
            <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
            <style> 
                #map { width: 100vw; height: 100vh; } 
                body { margin:0; font-family: Arial, sans-serif; }
                .info { position: absolute; top: 10px; left: 10px; background: white; padding: 10px; z-index: 1000; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.3); }
            </style>
        </head>
        <body>
            <div class='info'>Toplam %d ödeme noktası</div>
            <div id='map'></div>
            <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
            <script>
                var map = L.map('map').setView([39.0, 35.0], 6);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 18,
                    attribution: '© OpenStreetMap'
                }).addTo(map);
                %s
            </script>
        </body>
        </html>
        """.formatted(validPoints.size(), markersJs.toString());

        System.out.println("HTML içeriği hazırlandı, WebView'e yükleniyor...");
        
        webEngine.loadContent(html);
        root.setCenter(webView);
        mapStage.setScene(new Scene(root));
        mapStage.showAndWait();
    }
    
    // ...existing code...
    
    // ...existing code...
    
    private static void createMapWindow(Stage owner, List<PaymentPoint> paymentPoints) {
        try {
            System.out.println("🗺️ Stage oluşturuluyor... Thread: " + Thread.currentThread().getName());
            
            Stage mapStage = new Stage();
            mapStage.initOwner(owner);
            mapStage.initModality(Modality.APPLICATION_MODAL);
            mapStage.setTitle("🗺️ Ödeme Noktaları Haritası (OpenStreetMap)");
            mapStage.setWidth(1200);
            mapStage.setHeight(800);

            BorderPane root = new BorderPane();
            
            // Üst bilgi paneli
            VBox topPanel = createTopPanel(paymentPoints != null ? paymentPoints.size() : 0);
            root.setTop(topPanel);
            
            System.out.println("🗺️ WebView oluşturuluyor... Thread: " + Thread.currentThread().getName());
            
            // WebView oluştur
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            System.out.println("✅ WebView başarıyla oluşturuldu - Thread: " + Thread.currentThread().getName());
            
            webView.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            webView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            root.setCenter(webView);
            BorderPane.setMargin(webView, new Insets(0));

            // Alt panel
            HBox bottomPanel = createBottomPanel(mapStage);
            root.setBottom(bottomPanel);

            Scene scene = new Scene(root);
            mapStage.setScene(scene);
            
            System.out.println("🗺️ Stage gösteriliyor... Thread: " + Thread.currentThread().getName());
            mapStage.show();
            
            System.out.println("🗺️ WebEngine konfigürasyonu... Thread: " + Thread.currentThread().getName());
            
            // JavaScript console mesajlarını dinle
            webEngine.setOnAlert(e -> System.out.println("🗺️ JavaScript Alert: " + e.getData()));
            
            // Sayfa yükleme durumunu izle
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                System.out.println("🔄 WebEngine durumu: " + newState + " - Thread: " + Thread.currentThread().getName());
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    System.out.println("✅ Harita başarıyla yüklendi!");
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("❌ Harita yüklenemedi!");
                }
            });
            
            // HTML ve JS ile Leaflet haritası oluştur
            StringBuilder markersJs = new StringBuilder();
            for (PaymentPoint point : paymentPoints) {
                if (point.getLocation() != null && point.getLocation().getLatitude() != null && 
                    point.getLocation().getLongitude() != null && 
                    point.getLocation().getLatitude() != 0.0 && point.getLocation().getLongitude() != 0.0) {
                    
                    String address = "";
                    if (point.getAddress() != null) {
                        address = String.format("%s, %s, %s %s",
                            point.getAddress().getStreet() != null ? point.getAddress().getStreet() : "",
                            point.getAddress().getDistrict() != null ? point.getAddress().getDistrict() : "",
                            point.getAddress().getCity() != null ? point.getAddress().getCity() : "",
                            point.getAddress().getPostalCode() != null ? point.getAddress().getPostalCode() : "");
                    }
                    
                    String description = point.getDescription() != null && !point.getDescription().isEmpty() ? point.getDescription() : "Açıklama yok";
                    String popupHtml = String.format(
                        "<b>%s</b><br/><span style='font-size:12px;'>%s</span><br/><i style='color:#555;'>%s</i>",
                        point.getName().replace("'", " "),
                        address.replace("'", " "),
                        description.replace("'", " ")
                    );
                    markersJs.append(String.format(java.util.Locale.US,
                        "L.marker([%f, %f]).addTo(map).bindPopup('%s');\n",
                        point.getLocation().getLatitude(), point.getLocation().getLongitude(), popupHtml
                    ));
                }
            }
            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='utf-8'/>
                <meta http-equiv=\"Content-Security-Policy\" content=\"default-src * 'unsafe-inline' 'unsafe-eval'\">
                <title>Ödeme Noktaları Harita</title>
                <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                <link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css'/>
                <style> #map { width: 100vw; height: 100vh; } body { margin:0; } </style>
            </head>
            <body>
                <div id='map'></div>
                <script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>
                <script>
                    var map = L.map('map').setView([39.0, 35.0], 6);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 18,
                        attribution: '© OpenStreetMap'
                    }).addTo(map);
                    %s
                </script>
            </body>
            </html>
            """.formatted(markersJs.toString());

            // DEBUG: HTML içeriğini konsola yazdır
            System.out.println("[DEBUG] Harita HTML içeriği:\n" + html);

            webEngine.loadContent(html);
            webView.setPrefSize(900, 600);
            webView.setMinSize(600, 400);
            webView.setVisible(true);
            root.setCenter(webView);
            mapStage.setScene(new Scene(root));
            mapStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Harita penceresi oluşturulurken hata: " + e.getMessage());
            System.err.println("❌ Hata thread'i: " + Thread.currentThread().getName());
            System.err.println("❌ Hata stack trace:");
            e.printStackTrace();
            
            // Hata detayını bir dialog ile göster
            Platform.runLater(() -> {
                try {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Harita Hatası - Detay");
                    alert.setHeaderText("PaymentPointsMapPage.createMapWindow() Hatası");
                    alert.setContentText("Hata: " + e.getClass().getSimpleName() + "\nMesaj: " + e.getMessage() + "\nThread: " + Thread.currentThread().getName());
                    
                    // Stack trace'i göster
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    
                    javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(stackTrace);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setMaxWidth(Double.MAX_VALUE);
                    textArea.setMaxHeight(Double.MAX_VALUE);
                    
                    alert.getDialogPane().setExpandableContent(textArea);
                    alert.showAndWait();
                } catch (Exception alertException) {
                    System.err.println("Alert gösterilirken de hata: " + alertException.getMessage());
                    alertException.printStackTrace();
                }
            });
        }
    }
    
    private static VBox createTopPanel(int pointCount) {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(15));
        topPanel.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #34495e); -fx-text-fill: white;");
        
        Label titleLabel = new Label("🗺️ Ödeme Noktaları Haritası");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label infoLabel = new Label(String.format("📍 Toplam %d ödeme noktası gösteriliyor", pointCount));
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ecf0f1;");
        
        Label instructionLabel = new Label("💡 Fare ile sürükleyerek hareket ettirin, tekerlek ile yakınlaştırın/uzaklaştırın. Pin'lere tıklayarak detayları görün.");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");
        
        topPanel.getChildren().addAll(titleLabel, infoLabel, instructionLabel);
        return topPanel;
    }
    
    private static HBox createBottomPanel(Stage mapStage) {
        HBox bottomPanel = new HBox(15);
        bottomPanel.setPadding(new Insets(15));
        bottomPanel.setStyle("-fx-background-color: #34495e;");
        
        Button closeButton = new Button("❌ Kapat");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 12 20; -fx-font-weight: bold;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 12 20; -fx-font-weight: bold;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 12 20; -fx-font-weight: bold;"));
        closeButton.setOnAction(e -> mapStage.close());
        
        Region spacer = new Region();
        
        Label powerLabel = new Label("🌍 Powered by OpenStreetMap • Leaflet");
        powerLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px;");
        
        bottomPanel.getChildren().addAll(closeButton, spacer, powerLabel);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        return bottomPanel;
    }
    
    
    private static void addTestMarkers(StringBuilder markersJs, StringBuilder boundsJs) {
        System.out.println("🎯 Test marker'ları ekleniyor - 5 adet İstanbul lokasyonu");
        
        // Test noktaları - İstanbul'daki gerçek yerler
        String[][] testPoints = {
            {"1", "41.0082", "28.9784", "#e74c3c", "🕌 Sultanahmet Camii", "Sultanahmet, Fatih/İstanbul", "Tarihi yarımada merkezinde yer alan muhteşem cami", "09:00-17:00", "0212-518-1319", "Nakit, Kart"},
            {"2", "41.0086", "29.0103", "#27ae60", "🏰 Galata Kulesi", "Galata, Beyoğlu/İstanbul", "İstanbul'un en güzel manzarasını sunan tarihi kule", "09:00-21:00", "0212-293-8180", "Nakit, Kart, Mobil"},
            {"3", "41.0055", "28.9770", "#3498db", "🏛️ Topkapı Sarayı", "Fatih/İstanbul", "Osmanlı padişahlarının yaşadığı tarihi saray", "09:00-18:00", "0212-512-0480", "Nakit, Kart"},
            {"4", "41.0256", "28.9742", "#f39c12", "🛍️ Taksim Meydanı", "Taksim, Beyoğlu/İstanbul", "İstanbul'un kalbi, alışveriş ve eğlence merkezi", "24/7", "0212-251-1000", "Nakit, Kart, Mobil, Bitcoin"},
            {"5", "41.0408", "29.0055", "#9b59b6", "🌉 Çamlıca Tepesi", "Üsküdar/İstanbul", "İstanbul'un en yüksek noktası, muhteşem şehir manzarası", "00:00-24:00", "0216-422-0025", "Nakit, Kart, Mobil"}
        };
        
        for (String[] point : testPoints) {
            String index = point[0];
            double lat = Double.parseDouble(point[1]);
            double lng = Double.parseDouble(point[2]);
            String color = point[3];
            String name = point[4];
            String address = point[5];
            String description = point[6];
            String hours = point[7];
            String phone = point[8];
            String paymentMethods = point[9];
            
            System.out.println("   📍 " + name + " - Lat: " + lat + ", Lng: " + lng);
            
            // Popup içeriği
            String popupHtml = String.format("""
                <div style='font-family: Arial, sans-serif; max-width: 320px; min-width: 250px;'>
                    <h3 style='margin: 0 0 12px 0; color: #2c3e50; border-bottom: 2px solid %s; padding-bottom: 6px; font-size: 16px;'>
                        %s
                    </h3>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>📍 Konum:</strong><br>
                        <span style='color: #7f8c8d;'>Enlem: %.6f, Boylam: %.6f</span>
                    </div>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>🏠 Adres:</strong><br>
                        <span style='font-size: 12px;'>%s</span>
                    </div>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>📞 Telefon:</strong> %s
                    </div>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>🕒 Çalışma Saatleri:</strong> %s
                    </div>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>💰 Ödeme Yöntemleri:</strong><br>
                        <span style='font-size: 12px; color: #2980b9;'>%s</span>
                    </div>
                    <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                        <strong>📝 Açıklama:</strong><br>
                        <span style='font-size: 12px; font-style: italic; color: #7f8c8d;'>%s</span>
                    </div>
                    <div style='margin: 12px 0 0 0; padding: 8px; background-color: %s; color: white; text-align: center; border-radius: 6px; font-weight: bold;'>
                        ✅ Demo Nokta
                    </div>
                </div>
                """,
                color, name, lat, lng, address, phone, hours, paymentMethods, description, color
            );
            
            // Marker oluştur - daha basit ve güvenilir
            markersJs.append(String.format("""
                console.log('🎯 Test marker %s oluşturuluyor: [%f, %f]');
                var testMarker%s = L.marker([%f, %f]).addTo(map);
                testMarker%s.bindPopup('%s');
                console.log('✅ Test marker %s eklendi');
                
                """,
                index, lat, lng, index, lat, lng, index, 
                popupHtml.replace("'", "\\'").replace("\n", " ").replace("\r", ""), index
            ));

            // Bounds için koordinat ekle
            boundsJs.append(String.format("[%f, %f],", lat, lng));
        }
        
        System.out.println("✅ Test marker'ları başarıyla eklendi!");
    }
    
    private static String createAdvancedMapHTML(List<PaymentPoint> paymentPoints) {
        StringBuilder markersJs = new StringBuilder();
        StringBuilder boundsJs = new StringBuilder();
        int validPointCount = 0;
        
        // Null kontrolü ekle
        if (paymentPoints == null) {
            paymentPoints = new ArrayList<>();
        }
        
        System.out.println("🗺️ Harita için koordinat analizi başlıyor...");
        System.out.println("📊 Toplam " + paymentPoints.size() + " ödeme noktası işlenecek");
        
        // Gerçek veriler olmadığında test noktalarını göster
        if (paymentPoints.isEmpty()) {
            System.out.println("⚠️ Gerçek veri yok, test marker'ları ekleniyor...");
            addTestMarkers(markersJs, boundsJs);
            validPointCount = 5; // Test noktası sayısı
        }
        
        // Marker'ları ve bounds'ları oluştur
        for (PaymentPoint point : paymentPoints) {
            System.out.println("\n📍 İşlenen nokta: " + point.getName());
            
            Double latitude = point.getLocation() != null ? point.getLocation().getLatitude() : null;
            Double longitude = point.getLocation() != null ? point.getLocation().getLongitude() : null;
            String city = point.getAddress() != null ? point.getAddress().getCity() : "";
            String district = point.getAddress() != null ? point.getAddress().getDistrict() : "";
            
            System.out.println("   - Latitude: " + latitude);
            System.out.println("   - Longitude: " + longitude);
            System.out.println("   - Şehir: " + city);
            System.out.println("   - İlçe: " + district);
            
            // Koordinat kontrolü - sıfır olmayan ve gerçekçi koordinatlar
            if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0 &&
                Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180) {
                
                validPointCount++;
                System.out.println("   ✅ Geçerli koordinat - haritaya eklenecek");
                
                // Türkiye koordinat kontrolü (yaklaşık)
                boolean isInTurkey = (latitude >= 35.0 && latitude <= 43.0) &&
                                   (longitude >= 25.0 && longitude <= 45.0);
                System.out.println("   🇹🇷 Türkiye sınırları içinde: " + (isInTurkey ? "✅ Evet" : "❌ Hayır"));
                
                // Beklenen koordinatlar (DB'den)
                System.out.println("   📊 Beklenen koordinatlar kontrol:");
                if (point.getName().contains("Merkez") && city.equals("İstanbul")) {
                    System.out.println("      - Beklenen: Lat ~40.998, Lng ~29.123");
                    System.out.println("      - Gerçek:   Lat " + latitude + ", Lng " + longitude);
                }
                
                // Adres bilgisini hazırla
                String street = point.getAddress() != null ? point.getAddress().getStreet() : "";
                String postalCode = point.getAddress() != null ? point.getAddress().getPostalCode() : "";
                String address = String.format("%s, %s, %s %s",
                    street,
                    district,
                    city,
                    postalCode);
                
                String description = point.getDescription() != null && !point.getDescription().isEmpty() ? 
                    point.getDescription() : "Açıklama bulunmuyor";
                
                // Ödeme yöntemlerini string'e çevir
                String paymentMethodsStr = "";
                if (point.getPaymentMethods() != null) {
                    paymentMethodsStr = point.getPaymentMethods().stream()
                            .map(pm -> pm.getDisplayName())
                            .collect(java.util.stream.Collectors.joining(", "));
                }
                
                // Durum ve renk
                String status = point.isActive() ? "Aktif" : "Pasif";
                String iconColor = point.isActive() ? "#27ae60" : "#e74c3c";
                String statusIcon = point.isActive() ? "✅" : "❌";
                
                // Popup içeriği - HTML formatında
                String popupHtml = String.format("""
                    <div style='font-family: Arial, sans-serif; max-width: 320px; min-width: 250px;'>
                        <h3 style='margin: 0 0 12px 0; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 6px; font-size: 16px;'>
                            💳 %s
                        </h3>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>📍 Konum:</strong><br>
                            <span style='color: #7f8c8d;'>Enlem: %.6f, Boylam: %.6f</span>
                        </div>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>🏠 Adres:</strong><br>
                            <span style='font-size: 12px;'>%s</span>
                        </div>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>📞 Telefon:</strong> %s
                        </div>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>🕒 Çalışma Saatleri:</strong> %s
                        </div>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>💰 Ödeme Yöntemleri:</strong><br>
                            <span style='font-size: 12px; color: #2980b9;'>%s</span>
                        </div>
                        <div style='margin: 8px 0; color: #34495e; font-size: 13px;'>
                            <strong>📝 Açıklama:</strong><br>
                            <span style='font-size: 12px; font-style: italic; color: #7f8c8d;'>%s</span>
                        </div>
                        <div style='margin: 12px 0 0 0; padding: 8px; background-color: %s; color: white; text-align: center; border-radius: 6px; font-weight: bold;'>
                            %s %s
                        </div>
                    </div>
                    """,
                    escapeHtml(point.getName()),
                    latitude,
                    longitude,
                    escapeHtml(address),
                    escapeHtml(point.getContactNumber() != null ? point.getContactNumber() : "Bilgi yok"),
                    escapeHtml(point.getWorkingHours() != null ? point.getWorkingHours() : "Bilgi yok"),
                    escapeHtml(paymentMethodsStr),
                    escapeHtml(description),
                    iconColor,
                    statusIcon,
                    status
                );
                
                // Modern marker oluştur - daha büyük ve görünür
                markersJs.append(String.format("""
                    var marker%d = L.marker([%f, %f], {
                        icon: L.divIcon({
                            className: 'custom-marker-%d',
                            html: '<div class="marker-pin" style="background-color: %s; width: 30px; height: 30px; border-radius: 50%%; border: 4px solid white; box-shadow: 0 3px 8px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; cursor: pointer; transition: transform 0.2s;"><div style="color: white; font-size: 14px; font-weight: bold;">💳</div></div>',
                            iconSize: [38, 38],
                            iconAnchor: [19, 19]
                        })
                    }).addTo(map);
                    
                    marker%d.bindPopup('%s', {
                        maxWidth: 350,
                        minWidth: 280,
                        closeButton: true,
                        autoClose: false,
                        closeOnClick: false
                    });
                    
                    // Hover efekti
                    marker%d.on('mouseover', function() {
                        this.getElement().style.transform = 'scale(1.2)';
                        this.getElement().style.zIndex = '1000';
                    });
                    
                    marker%d.on('mouseout', function() {
                        this.getElement().style.transform = 'scale(1)';
                        this.getElement().style.zIndex = 'auto';
                    });
                    
                    """,
                    validPointCount,
                    latitude, 
                    longitude,
                    validPointCount,
                    iconColor,
                    validPointCount,
                    popupHtml.replace("'", "\\'").replace("\n", " ").replace("\r", ""),
                    validPointCount,
                    validPointCount
                ));
                
                // Bounds için koordinat ekle
                boundsJs.append(String.format("[%f, %f],", latitude, longitude));
                
            } else {
                System.out.println("   ❌ Geçersiz koordinat - atlanıyor");
                System.out.println("   - Latitude: " + latitude + " (geçerli aralık: -90 ile 90)");
                System.out.println("   - Longitude: " + longitude + " (geçerli aralık: -180 ile 180)");
            }
        }
        
        System.out.println("\n✅ Koordinat analizi tamamlandı:");
        System.out.println("   - Toplam nokta: " + paymentPoints.size());
        System.out.println("   - Geçerli koordinat: " + validPointCount);
        
        if (paymentPoints.isEmpty()) {
            System.out.println("   - Test noktaları: 5 adet (veri yok)");
            System.out.println("   - Geçersiz koordinat: 0");
        } else {
            System.out.println("   - Test noktaları: 0 (gerçek veri var)");
            System.out.println("   - Geçersiz koordinat: " + (paymentPoints.size() - validPointCount));
        }
        
        // Varsayılan merkez koordinatları - İstanbul
        String centerLat = "41.0082";
        String centerLng = "28.9784";
        String zoomLevel = "11";
        
        // Eğer geçerli noktalar varsa, otomatik zoom ve merkez ayarla
        String boundsCode = "";
        if (validPointCount > 0 && boundsJs.length() > 0) {
            // Son virgülü kaldır
            String coordinates = boundsJs.toString();
            if (coordinates.endsWith(",")) {
                coordinates = coordinates.substring(0, coordinates.length() - 1);
            }
            
            System.out.println("🎯 Otomatik zoom ayarlanıyor: " + validPointCount + " nokta için");
            
            boundsCode = String.format("""
                // Tüm marker'ları kapsayacak şekilde haritayı otomatik ayarla
                setTimeout(function() {
                    var coordinates = [%s];
                    if (coordinates.length > 0) {
                        if (coordinates.length === 1) {
                            // Tek nokta varsa merkez al ve zoom yap
                            map.setView(coordinates[0], 15);
                            console.log('Tek nokta gösteriliyor, zoom: 15');
                        } else {
                            // Birden fazla nokta varsa hepsini kapsayacak şekilde ayarla
                            var group = new L.featureGroup();
                            coordinates.forEach(function(coord) {
                                L.marker(coord).addTo(group);
                            });
                            if (group.getBounds().isValid()) {
                                map.fitBounds(group.getBounds(), {
                                    padding: [30, 30],
                                    maxZoom: 14
                                });
                                console.log('Birden fazla nokta gösteriliyor, otomatik bounds');
                            }
                        }
                    }
                }, 500);
                """, coordinates);
        } else {
            System.out.println("⚠️ Geçerli koordinat bulunamadı, varsayılan İstanbul merkezi kullanılıyor");
        }
        // HTML içeriğini oluştur
        System.out.println("🌐 HTML içeriği oluşturuluyor...");
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='utf-8'/>
                <title>Ödeme Noktaları Haritası</title>
                <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
                <style>
                    body { margin: 0; font-family: 'Segoe UI', Arial, sans-serif; }
                    #map { width: 100vw; height: 100vh; }
                    .custom-marker { border: none !important; background: none !important; }
                    .marker-pin:hover { transform: scale(1.2) !important; z-index: 1000 !important; }
                    
                    /* Test marker'lar için pulse animasyonu */
                    @keyframes pulse {
                        0% { box-shadow: 0 4px 12px rgba(0,0,0,0.5), 0 0 0 0 rgba(255,255,255,0.7); }
                        50% { box-shadow: 0 6px 16px rgba(0,0,0,0.6), 0 0 0 10px rgba(255,255,255,0.3); }
                        100% { box-shadow: 0 4px 12px rgba(0,0,0,0.5), 0 0 0 20px rgba(255,255,255,0); }
                    }
                    
                    /* Popup styling */
                    .leaflet-popup-content-wrapper {
                        border-radius: 12px;
                        box-shadow: 0 6px 20px rgba(0,0,0,0.25);
                        border: 2px solid #3498db;
                        background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
                    }
                    .leaflet-popup-content {
                        margin: 16px;
                        line-height: 1.5;
                        font-family: 'Segoe UI', Arial, sans-serif;
                    }
                    .leaflet-popup-tip {
                        border-top-color: #3498db !important;
                    }
                    
                    /* Control styling */
                    .leaflet-control-zoom {
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                    }
                    
                    /* Custom marker effects */
                    .custom-test-marker-1, .custom-test-marker-2, .custom-test-marker-3, 
                    .custom-test-marker-4, .custom-test-marker-5 {
                        border: none !important; 
                        background: none !important;
                    }
                </style>
            </head>
            <body>
                <div id='map'></div>
                <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
                <script>
                    console.log('🗺️ Harita başlatılıyor...');
                    
                    // Harita oluştur
                    var map = L.map('map', {
                        center: [%s, %s],
                        zoom: %s,
                        zoomControl: true,
                        scrollWheelZoom: true,
                        doubleClickZoom: true,
                        boxZoom: true,
                        keyboard: true,
                        worldCopyJump: true
                    });
                    
                    console.log('🌍 Harita merkezi: [' + %s + ', ' + %s + '], Zoom: ' + %s);
                    
                    // OpenStreetMap tile layer - yüksek kalite
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                        tileSize: 256,
                        updateWhenZooming: false,
                        keepBuffer: 4
                    }).addTo(map);
                    
                    console.log('🗺️ Tile layer eklendi');
                    
                    // Harita yüklendiğinde marker'ları ekle
                    map.whenReady(function() {
                        console.log('✅ Harita hazır, marker ları ekleniyor...');
                        
                        // Marker'ları ekle
                        %s
                        
                        console.log('📍 Tüm marker lar eklendi');
                        
                        %s
                        
                        // Harita kontrolleri
                        L.control.scale({
                            position: 'bottomleft',
                            metric: true,
                            imperial: false
                        }).addTo(map);
                        
                        console.log('✅ Harita tam olarak yüklendi: ' + %d + ' ödeme noktası gösteriliyor');
                    });
                    
                    // Hata durumları için log
                    map.on('error', function(e) {
                        console.error('❌ Harita hatası:', e);
                    });
                    
                    // Marker tıklama olayları
                    map.on('popupopen', function() {
                        console.log('📋 Popup açıldı');
                    });
                    
                    // Sayfa tamamen yüklendiğinde son kontrol
                    window.addEventListener('load', function() {
                        console.log('🔄 Sayfa tamamen yüklendi');
                        setTimeout(function() {
                            console.log('⏰ 2 saniye sonrası kontrol - harita çalışıyor mu?');
                            if (map) {
                                console.log('✅ Harita nesnesi mevcut');
                                console.log('📊 Harita zoom seviyesi:', map.getZoom());
                                console.log('📍 Harita merkezi:', map.getCenter());
                            } else {
                                console.error('❌ Harita nesnesi bulunamadı!');
                            }
                        }, 2000);
                    });
                </script>
            </body>
            </html>
            """, centerLat, centerLng, zoomLevel, centerLat, centerLng, zoomLevel, markersJs.toString(), boundsCode, validPointCount);
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;")
                  .replace("\n", "<br>");
    }
}
