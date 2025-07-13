package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

public class PaymentPointsMapPage {
    
    public static void showMap(Stage owner, List<PaymentPointsTablePage.PaymentPoint> paymentPoints) {
        // JavaFX Application Thread'de çalıştığımızdan emin olalım
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showMap(owner, paymentPoints));
            return;
        }
        
        Stage mapStage = new Stage();
        mapStage.initOwner(owner);
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("🗺️ Ödeme Noktaları Haritası (OpenStreetMap)");
        mapStage.setWidth(1200);
        mapStage.setHeight(800);

        BorderPane root = new BorderPane();
        
        // Üst bilgi paneli
        VBox topPanel = createTopPanel(paymentPoints.size());
        root.setTop(topPanel);
        
        // Harita - WebView'ı Platform.runLater ile oluştur
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        root.setCenter(webView);
        
        // Alt panel
        HBox bottomPanel = createBottomPanel(mapStage);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root);
        mapStage.setScene(scene);
        
        // Harita içeriğini yüklemeden önce Stage'i göster
        mapStage.show();
        
        // WebEngine'e içerik yüklemesini Platform.runLater ile yap
        Platform.runLater(() -> {
            try {
                String html = createAdvancedMapHTML(paymentPoints);
                webEngine.loadContent(html);
            } catch (Exception e) {
                System.err.println("Harita yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
    
    private static String createAdvancedMapHTML(List<PaymentPointsTablePage.PaymentPoint> paymentPoints) {
        StringBuilder markersJs = new StringBuilder();
        StringBuilder boundsJs = new StringBuilder();
        int validPointCount = 0;
        
        // Marker'ları ve bounds'ları oluştur
        for (PaymentPointsTablePage.PaymentPoint point : paymentPoints) {
            if (point.getLatitude() != 0.0 && point.getLongitude() != 0.0) {
                validPointCount++;
                
                // Adres bilgisini hazırla
                String address = String.format("%s, %s, %s %s",
                    point.getStreet() != null ? point.getStreet() : "",
                    point.getDistrict() != null ? point.getDistrict() : "",
                    point.getCity() != null ? point.getCity() : "",
                    point.getPostalCode() != null ? point.getPostalCode() : "");
                
                String description = point.getDescription() != null && !point.getDescription().isEmpty() ? 
                    point.getDescription() : "Açıklama bulunmuyor";
                
                // Durum ve renk
                String status = point.isActive() ? "Aktif" : "Pasif";
                String iconColor = point.isActive() ? "green" : "red";
                String statusIcon = point.isActive() ? "✅" : "❌";
                
                // Popup içeriği - HTML formatında
                String popupHtml = String.format("""
                    <div style='font-family: Arial, sans-serif; max-width: 300px;'>
                        <h3 style='margin: 0 0 10px 0; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 5px;'>
                            💳 %s
                        </h3>
                        <p style='margin: 5px 0; color: #34495e;'>
                            <strong>📍 Adres:</strong><br>
                            <span style='font-size: 12px;'>%s</span>
                        </p>
                        <p style='margin: 5px 0; color: #34495e;'>
                            <strong>📞 Telefon:</strong> %s
                        </p>
                        <p style='margin: 5px 0; color: #34495e;'>
                            <strong>🕒 Çalışma Saatleri:</strong> %s
                        </p>
                        <p style='margin: 5px 0; color: #34495e;'>
                            <strong>💰 Ödeme Yöntemleri:</strong><br>
                            <span style='font-size: 12px;'>%s</span>
                        </p>
                        <p style='margin: 5px 0; color: #34495e;'>
                            <strong>📝 Açıklama:</strong><br>
                            <span style='font-size: 12px; font-style: italic;'>%s</span>
                        </p>
                        <p style='margin: 10px 0 0 0; padding: 5px; background-color: %s; color: white; text-align: center; border-radius: 4px;'>
                            %s <strong>%s</strong>
                        </p>
                    </div>
                    """,
                    escapeHtml(point.getName()),
                    escapeHtml(address),
                    escapeHtml(point.getContactNumber() != null ? point.getContactNumber() : "Bilgi yok"),
                    escapeHtml(point.getWorkingHours() != null ? point.getWorkingHours() : "Bilgi yok"),
                    escapeHtml(point.getPaymentMethodsString()),
                    escapeHtml(description),
                    point.isActive() ? "#27ae60" : "#e74c3c",
                    statusIcon,
                    status
                );
                
                // Marker oluştur
                markersJs.append(String.format("""
                    var marker%d = L.marker([%f, %f], {
                        icon: L.divIcon({
                            className: 'custom-marker',
                            html: '<div style="background-color: %s; width: 20px; height: 20px; border-radius: 50%%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"><div style="color: white; text-align: center; line-height: 14px; font-size: 10px; font-weight: bold;">💳</div></div>',
                            iconSize: [26, 26],
                            iconAnchor: [13, 13]
                        })
                    }).addTo(map).bindPopup(`%s`, {maxWidth: 350});
                    """,
                    validPointCount,
                    point.getLatitude(), 
                    point.getLongitude(),
                    iconColor,
                    popupHtml.replace("`", "\\`")
                ));
                
                // Bounds için koordinat ekle
                boundsJs.append(String.format("[%f, %f],", point.getLatitude(), point.getLongitude()));
            }
        }
        
        // Varsayılan merkez - Türkiye
        String centerLat = "39.0";
        String centerLng = "35.0";
        String zoomLevel = "6";
        
        // Eğer geçerli noktalar varsa, bounds kullan
        String boundsCode = "";
        if (validPointCount > 0 && boundsJs.length() > 0) {
            // Son virgülü kaldır
            String coordinates = boundsJs.toString();
            if (coordinates.endsWith(",")) {
                coordinates = coordinates.substring(0, coordinates.length() - 1);
            }
            
            boundsCode = String.format("""
                // Tüm marker'ları kapsayacak şekilde haritayı ayarla
                var markers = [%s];
                var bounds = [];
                markers.forEach(function(coord) {
                    bounds.push(coord);
                });
                if (bounds.length > 0) {
                    var group = new L.featureGroup();
                    bounds.forEach(function(coord) {
                        L.marker(coord).addTo(group);
                    });
                    if (group.getBounds().isValid()) {
                        map.fitBounds(group.getBounds(), {padding: [20, 20]});
                    }
                }
                """, coordinates);
        }
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='utf-8'/>
                <title>Ödeme Noktaları Haritası</title>
                <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
                <style>
                    body { margin: 0; font-family: Arial, sans-serif; }
                    #map { width: 100vw; height: 100vh; }
                    .custom-marker { border: none !important; background: none !important; }
                    .leaflet-popup-content-wrapper {
                        border-radius: 8px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                    }
                    .leaflet-popup-content {
                        margin: 15px;
                        line-height: 1.4;
                    }
                </style>
            </head>
            <body>
                <div id='map'></div>
                <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
                <script>
                    // Harita oluştur
                    var map = L.map('map', {
                        center: [%s, %s],
                        zoom: %s,
                        zoomControl: true,
                        scrollWheelZoom: true,
                        doubleClickZoom: true,
                        boxZoom: true,
                        keyboard: true
                    });
                    
                    // OpenStreetMap tile layer
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    }).addTo(map);
                    
                    // Marker'ları ekle
                    %s
                    
                    %s
                    
                    // Harita kontrolleri
                    L.control.scale().addTo(map);
                    
                    console.log('Harita yüklendi: %%d ödeme noktası gösteriliyor', %d);
                </script>
            </body>
            </html>
            """, centerLat, centerLng, zoomLevel, markersJs.toString(), boundsCode, validPointCount);
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
