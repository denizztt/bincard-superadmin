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

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Durak Haritası Sayfası
 * Google Maps üzerinde tüm durakları gösterir
 */
public class StationsMapPage {
    
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    
    public StationsMapPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
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
        
        // Üst kısım tümü - sol üst buton + başlık ve kontroller
        VBox fullTopContainer = new VBox(10);
        
        // Sol üst buton
        HBox topLeftBox = new HBox();
        topLeftBox.setAlignment(Pos.TOP_LEFT);
        Button backToMenuButton = new Button("⬅️ Ana Menü");
        backToMenuButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 16 8 16;");
        backToMenuButton.setOnAction(e -> {
            new SuperadminDashboardFX(stage, accessToken, refreshToken);
        });
        topLeftBox.getChildren().add(backToMenuButton);
        
        // Üst kısım - Başlık ve kontroller
        VBox topContainer = createTopSection();
        
        fullTopContainer.getChildren().addAll(topLeftBox, topContainer);
        root.setTop(fullTopContainer);
        
        // Orta kısım - Bilgi kartları
        VBox centerContainer = createCenterSection();
        root.setCenter(centerContainer);
        
        // Alt kısım - Butonlar
        HBox bottomContainer = createBottomSection();
        root.setBottom(bottomContainer);
        
        // Sahne oluştur
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Durak Haritası - BinCard Superadmin");
        stage.setScene(scene);
        stage.show();
        
        // Token'i local dosyaya kaydet
        saveTokenToLocalFile();
        
        // Haritayı otomatik aç
        openStationsMapInBrowser();
    }
    
    private VBox createTopSection() {
        VBox topContainer = new VBox(15);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Başlık satırı
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🗺️ Durak Haritası");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Haritayı yeniden aç butonu
        Button reopenButton = new Button("🌐 Haritayı Aç");
        reopenButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        reopenButton.setOnAction(e -> openStationsMapInBrowser());
        
        titleRow.getChildren().addAll(titleLabel, spacer, reopenButton);
        
        // Açıklama
        Label descLabel = new Label("Tüm duraklar Google Maps üzerinde interactive olarak görüntülenir");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        topContainer.getChildren().addAll(titleRow, descLabel);
        return topContainer;
    }
    
    private VBox createCenterSection() {
        VBox centerContainer = new VBox(20);
        
        // Harita bilgi kartı
        VBox mapInfoCard = createMapInfoCard();
        
        // Özellikler kartı
        VBox featuresCard = createFeaturesCard();
        
        // İstatistikler kartı
        VBox statsCard = createStatsCard();
        
        centerContainer.getChildren().addAll(mapInfoCard, featuresCard, statsCard);
        return centerContainer;
    }
    
    private VBox createMapInfoCard() {
        VBox mapInfoCard = new VBox(15);
        mapInfoCard.setPadding(new Insets(20));
        mapInfoCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label infoTitle = new Label("🗺️ Durak Haritası Bilgileri");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        infoTitle.setTextFill(Color.web("#2c3e50"));
        
        Label infoText = new Label(
            "• Tüm duraklar farklı renkli ikonlarla harita üzerinde gösterilir\\n" +
            "• Her durak tipinin kendine özgü ikonu vardır (🚇 Metro, 🚊 Tramvay, 🚌 Otobüs, ⛴️ Vapur vb.)\\n" +
            "• Durak detayları için marker'lara tıklayabilirsiniz\\n" +
            "• Harita üzerinde yakınlaştırma, uzaklaştırma ve sürükleme yapabilirsiniz\\n" +
            "• Durak tipine göre filtreleme yapabilirsiniz\\n" +
            "• Clustering özelliği ile yakın duraklar gruplanır"
        );
        infoText.setFont(Font.font("System", FontWeight.NORMAL, 12));
        infoText.setTextFill(Color.web("#34495e"));
        infoText.setWrapText(true);
        
        mapInfoCard.getChildren().addAll(infoTitle, infoText);
        return mapInfoCard;
    }
    
    private VBox createFeaturesCard() {
        VBox featuresCard = new VBox(15);
        featuresCard.setPadding(new Insets(20));
        featuresCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label featuresTitle = new Label("✨ Harita Özellikleri");
        featuresTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        featuresTitle.setTextFill(Color.web("#2c3e50"));
        
        // Özellikler grid'i
        GridPane featuresGrid = new GridPane();
        featuresGrid.setHgap(30);
        featuresGrid.setVgap(15);
        featuresGrid.setPadding(new Insets(10));
        
        // Sol sütun
        VBox leftFeatures = new VBox(10);
        leftFeatures.getChildren().addAll(
            createFeatureItem("🎯", "Real-time Konum", "Gerçek zamanlı durak konumları"),
            createFeatureItem("🏷️", "Tip Filtreleri", "Durak tipine göre filtreleme"),
            createFeatureItem("📍", "Detaylı Bilgi", "Her durak için detaylı bilgi popup'ı"),
            createFeatureItem("🔍", "Arama", "Durak adı ile arama yapabilme")
        );
        
        // Sağ sütun
        VBox rightFeatures = new VBox(10);
        rightFeatures.getChildren().addAll(
            createFeatureItem("📱", "Responsive", "Mobil ve masaüstü uyumlu"),
            createFeatureItem("🌍", "Satellite View", "Uydu görünümü desteği"),
            createFeatureItem("🗂️", "Clustering", "Yakın durakları gruplama"),
            createFeatureItem("📊", "İstatistikler", "Durak sayıları ve dağılım")
        );
        
        featuresGrid.add(leftFeatures, 0, 0);
        featuresGrid.add(rightFeatures, 1, 0);
        
        featuresCard.getChildren().addAll(featuresTitle, featuresGrid);
        return featuresCard;
    }
    
    private HBox createFeatureItem(String icon, String title, String description) {
        HBox featureItem = new HBox(10);
        featureItem.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(16));
        
        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        textBox.getChildren().addAll(titleLabel, descLabel);
        featureItem.getChildren().addAll(iconLabel, textBox);
        
        return featureItem;
    }
    
    private VBox createStatsCard() {
        VBox statsCard = new VBox(15);
        statsCard.setPadding(new Insets(20));
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label statsTitle = new Label("📊 Durak İstatistikleri");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        statsTitle.setTextFill(Color.web("#2c3e50"));
        
        // İstatistikler grid'i
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(10));
        
        // Örnek istatistikler
        statsGrid.add(createStatItem("🚌", "Otobüs Durakları", "1,245"), 0, 0);
        statsGrid.add(createStatItem("🚇", "Metro Durakları", "156"), 1, 0);
        statsGrid.add(createStatItem("🚊", "Tramvay Durakları", "89"), 0, 1);
        statsGrid.add(createStatItem("⛴️", "Vapur Durakları", "47"), 1, 1);
        statsGrid.add(createStatItem("🚌", "Metrobüs Durakları", "52"), 0, 2);
        statsGrid.add(createStatItem("🚆", "Tren Durakları", "23"), 1, 2);
        
        statsCard.getChildren().addAll(statsTitle, statsGrid);
        return statsCard;
    }
    
    private VBox createStatItem(String icon, String label, String value) {
        VBox statItem = new VBox(5);
        statItem.setAlignment(Pos.CENTER);
        statItem.setPadding(new Insets(10));
        statItem.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web("#2c3e50"));
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.NORMAL, 10));
        labelText.setTextFill(Color.web("#7f8c8d"));
        labelText.setWrapText(true);
        labelText.setAlignment(Pos.CENTER);
        
        statItem.getChildren().addAll(iconLabel, valueLabel, labelText);
        return statItem;
    }
    
    private HBox createBottomSection() {
        HBox bottomContainer = new HBox(15);
        bottomContainer.setAlignment(Pos.CENTER_RIGHT);
        bottomContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Empty bottom section since back button is now at top-left
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
    
    private void openStationsMapInBrowser() {
        try {
            String userHome = System.getProperty("user.home");
            String htmlContent = createStationsMapHTML();
            
            // HTML dosyasını oluştur
            File htmlFile = new File(userHome, "stations_map.html");
            try (FileWriter writer = new FileWriter(htmlFile)) {
                writer.write(htmlContent);
            }
            
            // Tarayıcıda aç
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", htmlFile.getAbsolutePath());
            pb.start();
            
            System.out.println("🌐 Duraklar haritası tarayıcıda açıldı: " + htmlFile.getAbsolutePath());
            
            // Başarı mesajı göster
            Platform.runLater(() -> {
                showSuccessAlert("🌐 Harita Açıldı", "Duraklar haritası web tarayıcısında açıldı.");
            });
            
        } catch (Exception e) {
            System.err.println("❌ Harita açma hatası: " + e.getMessage());
            e.printStackTrace();
            
            Platform.runLater(() -> {
                showErrorAlert("Hata", "Harita açılırken hata oluştu: " + e.getMessage());
            });
        }
    }
    
    private String createStationsMapHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>Duraklar Haritası - BinCard Superadmin</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f5f5; }
        .header { background: white; padding: 15px 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header h1 { margin: 0; color: #2c3e50; font-size: 24px; }
        .header .subtitle { color: #7f8c8d; margin: 5px 0 0 0; font-size: 14px; }
        .controls { background: white; padding: 15px 20px; margin: 10px 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .controls h3 { margin: 0 0 15px 0; color: #2c3e50; }
        .filter-group { display: inline-block; margin-right: 20px; }
        .filter-group label { display: block; font-weight: bold; margin-bottom: 5px; color: #2c3e50; }
        .filter-group select, .filter-group input { padding: 8px; border: 1px solid #bdc3c7; border-radius: 4px; font-size: 14px; }
        .btn { background: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; margin: 5px; }
        .btn:hover { background: #2980b9; }
        .btn-success { background: #27ae60; }
        .btn-success:hover { background: #229954; }
        .btn-warning { background: #f39c12; }
        .btn-warning:hover { background: #e67e22; }
        #map { height: 600px; margin: 10px 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .stats { background: white; padding: 15px 20px; margin: 10px 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .stats h3 { margin: 0 0 15px 0; color: #2c3e50; }
        .stat-item { display: inline-block; margin-right: 30px; text-align: center; }
        .stat-item .number { font-size: 24px; font-weight: bold; color: #3498db; }
        .stat-item .label { font-size: 12px; color: #7f8c8d; }
        .legend { background: white; padding: 15px 20px; margin: 10px 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .legend h3 { margin: 0 0 15px 0; color: #2c3e50; }
        .legend-item { display: inline-block; margin-right: 20px; margin-bottom: 10px; }
        .legend-icon { display: inline-block; width: 20px; text-align: center; margin-right: 5px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🗺️ Duraklar Haritası</h1>
        <p class="subtitle">BinCard Superadmin - Tüm duraklar interactive harita üzerinde</p>
    </div>
    
    <div class="controls">
        <h3>🔧 Harita Kontrolleri</h3>
        <div class="filter-group">
            <label>Durak Tipi:</label>
            <select id="stationType" onchange="filterStations()">
                <option value="all">Tümü</option>
                <option value="METRO">🚇 Metro</option>
                <option value="TRAMVAY">🚊 Tramvay</option>
                <option value="OTOBUS">🚌 Otobüs</option>
                <option value="METROBUS">🚌 Metrobüs</option>
                <option value="VAPUR">⛴️ Vapur</option>
                <option value="TREN">🚆 Tren</option>
                <option value="DOLMUS">🚐 Dolmuş</option>
            </select>
        </div>
        <div class="filter-group">
            <label>Durak Ara:</label>
            <input type="text" id="stationSearch" placeholder="Durak adı..." onkeyup="searchStations()">
        </div>
        <button class="btn" onclick="showAllStations()">🌍 Tümünü Göster</button>
        <button class="btn btn-success" onclick="centerToIstanbul()">📍 İstanbul'a Git</button>
        <button class="btn btn-warning" onclick="toggleClustering()">🗂️ Clustering</button>
    </div>
    
    <div id="map"></div>
    
    <div class="stats">
        <h3>📊 Durak İstatistikleri</h3>
        <div class="stat-item">
            <div class="number" id="totalStations">1,612</div>
            <div class="label">Toplam Durak</div>
        </div>
        <div class="stat-item">
            <div class="number" id="activeStations">1,558</div>
            <div class="label">Aktif Durak</div>
        </div>
        <div class="stat-item">
            <div class="number" id="visibleStations">1,612</div>
            <div class="label">Görünen Durak</div>
        </div>
        <div class="stat-item">
            <div class="number">5</div>
            <div class="label">Şehir</div>
        </div>
        <div class="stat-item">
            <div class="number">39</div>
            <div class="label">İlçe</div>
        </div>
    </div>
    
    <div class="legend">
        <h3>📋 Durak Tipleri Rehberi</h3>
        <div class="legend-item"><span class="legend-icon">🚇</span> Metro Durakları</div>
        <div class="legend-item"><span class="legend-icon">🚊</span> Tramvay Durakları</div>
        <div class="legend-item"><span class="legend-icon">🚌</span> Otobüs Durakları</div>
        <div class="legend-item"><span class="legend-icon">🚌</span> Metrobüs Durakları</div>
        <div class="legend-item"><span class="legend-icon">⛴️</span> Vapur Durakları</div>
        <div class="legend-item"><span class="legend-icon">🚆</span> Tren Durakları</div>
        <div class="legend-item"><span class="legend-icon">🚐</span> Dolmuş Durakları</div>
        <div class="legend-item"><span class="legend-icon">🏃</span> Aktif Duraklar</div>
        <div class="legend-item"><span class="legend-icon">⏸️</span> Pasif Duraklar</div>
    </div>

    <script>
        let map;
        let markers = [];
        let markerCluster;
        let clusteringEnabled = true;
        
        // Örnek durak verileri
        const stationsData = [
            { id: 1, name: "Taksim Meydanı", type: "METRO", lat: 41.0369, lng: 28.9851, status: "ACTIVE", city: "İstanbul", district: "Beyoğlu" },
            { id: 2, name: "Kadıköy İskele", type: "VAPUR", lat: 40.9996, lng: 29.0277, status: "ACTIVE", city: "İstanbul", district: "Kadıköy" },
            { id: 3, name: "Mecidiyeköy", type: "METROBUS", lat: 41.0631, lng: 28.9897, status: "ACTIVE", city: "İstanbul", district: "Şişli" },
            { id: 4, name: "Eminönü", type: "TRAMVAY", lat: 41.0169, lng: 28.9705, status: "INACTIVE", city: "İstanbul", district: "Fatih" },
            { id: 5, name: "Beşiktaş", type: "DOLMUS", lat: 41.0423, lng: 29.0061, status: "ACTIVE", city: "İstanbul", district: "Beşiktaş" },
            { id: 6, name: "Galata Köprüsü", type: "OTOBUS", lat: 41.0204, lng: 28.9739, status: "ACTIVE", city: "İstanbul", district: "Fatih" },
            { id: 7, name: "Sirkeci Garı", type: "TREN", lat: 41.0138, lng: 28.9768, status: "ACTIVE", city: "İstanbul", district: "Fatih" },
            { id: 8, name: "Üsküdar İskele", type: "VAPUR", lat: 41.0224, lng: 29.0092, status: "ACTIVE", city: "İstanbul", district: "Üsküdar" }
        ];

        function initMap() {
            const istanbul = { lat: 41.0082, lng: 28.9784 };
            
            map = new google.maps.Map(document.getElementById("map"), {
                zoom: 12,
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

            // Durakları haritaya ekle
            createStationMarkers();
            
            // Clustering başlat
            if (typeof MarkerClusterer !== 'undefined') {
                markerCluster = new MarkerClusterer(map, markers, {
                    imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'
                });
            }
            
            console.log("🗺️ Harita başarıyla yüklendi, " + markers.length + " durak gösteriliyor");
        }

        function createStationMarkers() {
            stationsData.forEach(station => {
                const icon = getStationIcon(station.type, station.status);
                
                const marker = new google.maps.Marker({
                    position: { lat: station.lat, lng: station.lng },
                    map: map,
                    title: station.name,
                    icon: icon,
                    stationData: station
                });

                const infoWindow = new google.maps.InfoWindow({
                    content: createInfoWindowContent(station)
                });

                marker.addListener("click", () => {
                    infoWindow.open(map, marker);
                });

                markers.push(marker);
            });
        }

        function getStationIcon(type, status) {
            let emoji = "🚏";
            switch(type) {
                case "METRO": emoji = "🚇"; break;
                case "TRAMVAY": emoji = "🚊"; break;
                case "OTOBUS": emoji = "🚌"; break;
                case "METROBUS": emoji = "🚌"; break;
                case "VAPUR": emoji = "⛴️"; break;
                case "TREN": emoji = "🚆"; break;
                case "DOLMUS": emoji = "🚐"; break;
            }
            
            const color = status === "ACTIVE" ? "#27ae60" : "#e74c3c";
            
            return {
                url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">
                        <circle cx="20" cy="20" r="18" fill="${color}" stroke="white" stroke-width="2"/>
                        <text x="20" y="28" text-anchor="middle" font-size="16" fill="white">${emoji}</text>
                    </svg>
                `),
                scaledSize: new google.maps.Size(40, 40),
                anchor: new google.maps.Point(20, 20)
            };
        }

        function createInfoWindowContent(station) {
            const statusText = station.status === "ACTIVE" ? "🟢 Aktif" : "🔴 Pasif";
            return `
                <div style="max-width: 250px; font-family: Arial, sans-serif;">
                    <h3 style="margin: 0 0 10px 0; color: #2c3e50;">${station.name}</h3>
                    <p style="margin: 5px 0;"><strong>Tip:</strong> ${station.type}</p>
                    <p style="margin: 5px 0;"><strong>Konum:</strong> ${station.district}, ${station.city}</p>
                    <p style="margin: 5px 0;"><strong>Koordinat:</strong> ${station.lat.toFixed(6)}, ${station.lng.toFixed(6)}</p>
                    <p style="margin: 5px 0;"><strong>Durum:</strong> ${statusText}</p>
                    <div style="margin-top: 10px;">
                        <button onclick="editStation(${station.id})" style="background: #f39c12; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer; margin-right: 5px;">✏️ Düzenle</button>
                        <button onclick="getDirections(${station.lat}, ${station.lng})" style="background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">🗺️ Yol Tarifi</button>
                    </div>
                </div>
            `;
        }

        function filterStations() {
            const selectedType = document.getElementById('stationType').value;
            let visibleCount = 0;
            
            markers.forEach(marker => {
                const stationData = marker.stationData;
                const shouldShow = selectedType === 'all' || stationData.type === selectedType;
                
                marker.setVisible(shouldShow);
                if (shouldShow) visibleCount++;
            });
            
            document.getElementById('visibleStations').textContent = visibleCount;
            
            if (markerCluster) {
                markerCluster.clearMarkers();
                const visibleMarkers = markers.filter(marker => marker.getVisible());
                markerCluster.addMarkers(visibleMarkers);
            }
        }

        function searchStations() {
            const searchText = document.getElementById('stationSearch').value.toLowerCase();
            let visibleCount = 0;
            
            markers.forEach(marker => {
                const stationData = marker.stationData;
                const matchesSearch = searchText === '' || 
                    stationData.name.toLowerCase().includes(searchText) ||
                    stationData.district.toLowerCase().includes(searchText) ||
                    stationData.city.toLowerCase().includes(searchText);
                
                marker.setVisible(matchesSearch);
                if (matchesSearch) visibleCount++;
            });
            
            document.getElementById('visibleStations').textContent = visibleCount;
            
            if (markerCluster) {
                markerCluster.clearMarkers();
                const visibleMarkers = markers.filter(marker => marker.getVisible());
                markerCluster.addMarkers(visibleMarkers);
            }
        }

        function showAllStations() {
            document.getElementById('stationType').value = 'all';
            document.getElementById('stationSearch').value = '';
            
            markers.forEach(marker => marker.setVisible(true));
            document.getElementById('visibleStations').textContent = markers.length;
            
            if (markerCluster) {
                markerCluster.clearMarkers();
                markerCluster.addMarkers(markers);
            }
        }

        function centerToIstanbul() {
            const istanbul = { lat: 41.0082, lng: 28.9784 };
            map.setCenter(istanbul);
            map.setZoom(12);
        }

        function toggleClustering() {
            if (markerCluster) {
                if (clusteringEnabled) {
                    markerCluster.clearMarkers();
                    clusteringEnabled = false;
                    alert("🗂️ Clustering kapatıldı");
                } else {
                    markerCluster.addMarkers(markers.filter(marker => marker.getVisible()));
                    clusteringEnabled = true;
                    alert("🗂️ Clustering açıldı");
                }
            }
        }

        function editStation(stationId) {
            alert(`✏️ Durak düzenleme özelliği yakında eklenecek. Durak ID: ${stationId}`);
        }

        function getDirections(lat, lng) {
            const url = `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`;
            window.open(url, '_blank');
        }
    </script>
    
    <script async defer 
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBpKV71_29LUW6kKj2aH4CjSFrFJCN8Ye4&callback=initMap&libraries=geometry">
    </script>
    <script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js">
    </script>
</body>
</html>
        """;
    }
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
