package com.bincard.bincard_superadmin;

import javafx.application.Platform;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Task;
import javafx.scene.Node;
// import javafx.util.Duration;
// import javafx.animation.Timeline;
// import javafx.animation.KeyFrame;
import javafx.concurrent.Worker;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;
// import javafx.event.EventHandler;
import javafx.stage.Stage;
// import netscape.javascript.JSObject; // Not available in all JavaFX versions
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

/**
 * Ödeme noktalarını Google Maps üzerinde gösteren sayfa
 */
public class PaymentPointsMapPage extends SuperadminPageBase {

    private WebView mapWebView;
    private WebEngine webEngine;
    private ComboBox<String> cityFilter;
    private ComboBox<String> paymentMethodFilter;
    private ComboBox<String> statusFilter;
    private Button refreshButton;
    private Button searchNearbyButton;
    private TextField searchLocationField;
    private Spinner<Double> radiusSpinner;
    private ListView<PaymentPoint> paymentPointsList;
    private ObservableList<PaymentPoint> paymentPointsData;
    private Label infoLabel;
    private ProgressIndicator loadingIndicator;
    private boolean mapInitialized = false;
    // private ObjectMapper objectMapper;
    private List<PaymentPoint> allPaymentPoints;
    private ScheduledExecutorService scheduler;

    // Sample data for demonstration
    private static final List<PaymentPoint> samplePaymentPoints = List.of(
        new PaymentPoint(1L, "Merkez Kart Yükleme", "İSTANBUL", "Taksim", 
                        "İstiklal Caddesi No:142", "Metro İstasyonu", 
                        41.0370, 28.9857, "Kart,Nakit", "Aktif", "İstanbul", 
                        "Merkez lokasyon", "09:00-22:00", "0212-123-4567"),
        new PaymentPoint(2L, "Kadıköy Kart Merkezi", "İSTANBUL", "Kadıköy", 
                        "Bahariye Caddesi No:45", "Otobüs Durağı", 
                        40.9890, 29.0266, "Kart,Nakit,Kredi Kartı", "Aktif", "İstanbul", 
                        "Kadıköy merkez", "08:00-21:00", "0216-456-7890"),
        new PaymentPoint(3L, "Ankara Kızılay Noktası", "ANKARA", "Çankaya", 
                        "Kızılay Meydanı No:12", "Metro İstasyonu", 
                        39.9208, 32.8541, "Kart,Nakit", "Aktif", "Ankara", 
                        "Kızılay merkez", "08:30-20:30", "0312-789-0123"),
        new PaymentPoint(4L, "İzmir Konak Terminali", "İZMİR", "Konak", 
                        "Cumhuriyet Meydanı No:7", "Terminal", 
                        38.4189, 27.1287, "Kart,Nakit,Kredi Kartı", "Aktif", "İzmir", 
                        "Ana terminal", "07:00-23:00", "0232-345-6789"),
        new PaymentPoint(5L, "Bursa Merkez Kart", "BURSA", "Osmangazi", 
                        "Atatürk Caddesi No:89", "Otobüs Durağı", 
                        40.1826, 29.0665, "Kart,Nakit", "Pasif", "Bursa", 
                        "Geçici kapalı", "09:00-18:00", "0224-567-8901")
    );

    public PaymentPointsMapPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktaları - Harita Görünümü");
        this.allPaymentPoints = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        initializeComponents();
        loadPaymentPoints();
    }

    @Override
    protected Node createContent() {
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Ödeme Noktaları - Harita Görünümü");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Filter controls
        HBox filtersBox = new HBox(10);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.getChildren().addAll(
            new Label("Şehir:"), cityFilter,
            new Label("Ödeme Yöntemi:"), paymentMethodFilter,
            new Label("Durum:"), statusFilter,
            refreshButton
        );

        // Search controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(
            new Label("Konum:"), searchLocationField,
            new Label("Yarıçap (km):"), radiusSpinner,
            searchNearbyButton
        );

        // Info and loading
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getChildren().addAll(infoLabel, loadingIndicator);

        // Main content area
        HBox contentBox = new HBox(15);
        contentBox.setPrefHeight(600);
        
        // Map container
        VBox mapContainer = new VBox(5);
        mapContainer.getChildren().addAll(
            new Label("Harita Görünümü") {{
                setFont(Font.font("Arial", FontWeight.BOLD, 14));
            }},
            mapWebView
        );
        HBox.setHgrow(mapContainer, Priority.ALWAYS);

        // List container
        VBox listContainer = new VBox(5);
        listContainer.getChildren().addAll(
            new Label("Ödeme Noktaları Listesi") {{
                setFont(Font.font("Arial", FontWeight.BOLD, 14));
            }},
            paymentPointsList
        );
        listContainer.setPrefWidth(300);

        contentBox.getChildren().addAll(mapContainer, listContainer);

        mainContent.getChildren().addAll(titleLabel, filtersBox, searchBox, infoBox, contentBox);
        return mainContent;
    }

    private void initializeComponents() {
        // Filter controls
        cityFilter = new ComboBox<>();
        cityFilter.getItems().addAll("Tümü", "İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Adana");
        cityFilter.setValue("Tümü");
        cityFilter.setOnAction(e -> filterPaymentPoints());

        paymentMethodFilter = new ComboBox<>();
        paymentMethodFilter.getItems().addAll("Tümü", "Kart", "Nakit", "Kredi Kartı", "Mobil Ödeme");
        paymentMethodFilter.setValue("Tümü");
        paymentMethodFilter.setOnAction(e -> filterPaymentPoints());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "Aktif", "Pasif", "Bakımda");
        statusFilter.setValue("Tümü");
        statusFilter.setOnAction(e -> filterPaymentPoints());

        refreshButton = new Button("Yenile");
        refreshButton.setOnAction(e -> loadPaymentPoints());

        // Search controls
        searchLocationField = new TextField();
        searchLocationField.setPromptText("Konum ara (adres, ilçe, vb.)");
        searchLocationField.setPrefWidth(200);

        radiusSpinner = new Spinner<>(0.5, 50.0, 5.0, 0.5);
        radiusSpinner.setEditable(true);
        radiusSpinner.setPrefWidth(80);

        searchNearbyButton = new Button("Yakındaki Noktalar");
        searchNearbyButton.setOnAction(e -> searchNearbyPoints());

        // Map WebView
        mapWebView = new WebView();
        mapWebView.setPrefSize(800, 600);
        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        
        // Payment points list
        paymentPointsList = new ListView<>();
        paymentPointsList.setPrefWidth(300);
        paymentPointsList.setCellFactory(lv -> new PaymentPointListCell());
        paymentPointsList.setOnMouseClicked(this::onPaymentPointSelected);
        
        paymentPointsData = FXCollections.observableArrayList();
        paymentPointsList.setItems(paymentPointsData);

        // Info label
        infoLabel = new Label("Ödeme noktaları yükleniyor...");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        infoLabel.setTextFill(Color.GRAY);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        // Initialize map after WebView is ready
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    if (!mapInitialized) {
                        initializeMap();
                        mapInitialized = true;
                    }
                }
            }
        });

        // Load initial HTML
        loadMapHTML();
    }

    private void loadMapHTML() {
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Ödeme Noktaları Haritası</title>
                <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=places"></script>
                <style>
                    body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
                    #map { height: 100vh; width: 100%; }
                    .info-window { max-width: 300px; }
                    .info-window h3 { margin: 0 0 10px 0; color: #2c3e50; }
                    .info-window p { margin: 5px 0; font-size: 12px; }
                    .info-window .status { 
                        padding: 2px 8px; 
                        border-radius: 3px; 
                        font-size: 11px; 
                        font-weight: bold; 
                    }
                    .info-window .status.active { background-color: #d4edda; color: #155724; }
                    .info-window .status.inactive { background-color: #f8d7da; color: #721c24; }
                    .info-window .status.maintenance { background-color: #fff3cd; color: #856404; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    let map;
                    let markers = [];
                    let infoWindows = [];
                    
                    function initMap() {
                        // İstanbul merkez koordinatları
                        const center = { lat: 41.0082, lng: 28.9784 };
                        
                        map = new google.maps.Map(document.getElementById('map'), {
                            zoom: 10,
                            center: center,
                            mapTypeId: 'roadmap',
                            styles: [
                                {
                                    featureType: 'poi',
                                    elementType: 'labels',
                                    stylers: [{ visibility: 'on' }]
                                }
                            ]
                        });
                        
                        // Test marker
                        addTestMarkers();
                    }
                    
                    function addTestMarkers() {
                        // Test için birkaç marker ekle
                        const testPoints = [
                            { lat: 41.0370, lng: 28.9857, name: 'Merkez Kart Yükleme', status: 'active' },
                            { lat: 40.9890, lng: 29.0266, name: 'Kadıköy Kart Merkezi', status: 'active' },
                            { lat: 39.9208, lng: 32.8541, name: 'Ankara Kızılay Noktası', status: 'active' },
                            { lat: 38.4189, lng: 27.1287, name: 'İzmir Konak Terminali', status: 'active' },
                            { lat: 40.1826, lng: 29.0665, name: 'Bursa Merkez Kart', status: 'inactive' }
                        ];
                        
                        testPoints.forEach(point => {
                            addMarker(point.lat, point.lng, point.name, point.status, {
                                city: 'Test Şehir',
                                district: 'Test İlçe',
                                address: 'Test Adres',
                                paymentMethods: 'Kart,Nakit',
                                workingHours: '09:00-18:00'
                            });
                        });
                    }
                    
                    function addMarker(lat, lng, title, status, details) {
                        const marker = new google.maps.Marker({
                            position: { lat: lat, lng: lng },
                            map: map,
                            title: title,
                            icon: getMarkerIcon(status)
                        });
                        
                        const infoWindow = new google.maps.InfoWindow({
                            content: createInfoWindowContent(title, status, details)
                        });
                        
                        marker.addListener('click', () => {
                            closeAllInfoWindows();
                            infoWindow.open(map, marker);
                        });
                        
                        markers.push(marker);
                        infoWindows.push(infoWindow);
                    }
                    
                    function getMarkerIcon(status) {
                        const iconColors = {
                            active: 'green',
                            inactive: 'red',
                            maintenance: 'orange'
                        };
                        
                        return {
                            url: `https://maps.google.com/mapfiles/ms/icons/${iconColors[status] || 'red'}-dot.png`,
                            scaledSize: new google.maps.Size(32, 32)
                        };
                    }
                    
                    function createInfoWindowContent(title, status, details) {
                        const statusText = {
                            active: 'Aktif',
                            inactive: 'Pasif',
                            maintenance: 'Bakımda'
                        };
                        
                        return `
                            <div class="info-window">
                                <h3>${title}</h3>
                                <p><strong>Durum:</strong> <span class="status ${status}">${statusText[status] || 'Bilinmiyor'}</span></p>
                                <p><strong>Şehir:</strong> ${details.city || 'Bilinmiyor'}</p>
                                <p><strong>İlçe:</strong> ${details.district || 'Bilinmiyor'}</p>
                                <p><strong>Adres:</strong> ${details.address || 'Bilinmiyor'}</p>
                                <p><strong>Ödeme Yöntemleri:</strong> ${details.paymentMethods || 'Bilinmiyor'}</p>
                                <p><strong>Çalışma Saatleri:</strong> ${details.workingHours || 'Bilinmiyor'}</p>
                            </div>
                        `;
                    }
                    
                    function closeAllInfoWindows() {
                        infoWindows.forEach(infoWindow => {
                            infoWindow.close();
                        });
                    }
                    
                    function clearMarkers() {
                        markers.forEach(marker => {
                            marker.setMap(null);
                        });
                        markers = [];
                        infoWindows = [];
                    }
                    
                    function centerMap(lat, lng, zoom = 15) {
                        map.setCenter({ lat: lat, lng: lng });
                        map.setZoom(zoom);
                    }
                    
                    function addPaymentPoints(points) {
                        clearMarkers();
                        points.forEach(point => {
                            addMarker(
                                point.latitude,
                                point.longitude,
                                point.name,
                                point.status.toLowerCase(),
                                {
                                    city: point.city,
                                    district: point.district,
                                    address: point.address,
                                    paymentMethods: point.paymentMethods,
                                    workingHours: point.workingHours
                                }
                            );
                        });
                    }
                    
                    // Google Maps API yüklendikten sonra haritayı başlat
                    window.onload = function() {
                        if (typeof google !== 'undefined' && google.maps) {
                            initMap();
                        } else {
                            // API key yoksa basit bir mesaj göster
                            document.getElementById('map').innerHTML = 
                                '<div style="display: flex; align-items: center; justify-content: center; height: 100%; background-color: #f0f0f0; color: #666; font-size: 14px;">' +
                                'Google Maps API anahtarı gereklidir.<br>Lütfen HTML içindeki YOUR_API_KEY kısmını gerçek API anahtarınızla değiştirin.' +
                                '</div>';
                        }
                    };
                </script>
            </body>
            </html>
        """;
        
        webEngine.loadContent(htmlContent);
    }

    private void initializeMap() {
        // Map yüklendikten sonra JavaScript'e erişim sağla
        try {
            // JSObject window = (JSObject) webEngine.executeScript("window");
            // Map'e JavaFX'ten JavaScript'e erişim sağla
            Platform.runLater(() -> {
                updateMapWithPaymentPoints();
            });
        } catch (Exception e) {
            System.err.println("Map initialization error: " + e.getMessage());
        }
    }

    private void updateMapWithPaymentPoints() {
        if (!mapInitialized) return;
        
        try {
            // Sample data'yı JavaScript'e gönder
            StringBuilder jsArray = new StringBuilder("[");
            for (int i = 0; i < samplePaymentPoints.size(); i++) {
                PaymentPoint point = samplePaymentPoints.get(i);
                if (i > 0) jsArray.append(",");
                jsArray.append(String.format(
                    "{name:'%s', latitude:%f, longitude:%f, status:'%s', city:'%s', district:'%s', address:'%s', paymentMethods:'%s', workingHours:'%s'}",
                    point.getName().replace("'", "\\'"),
                    point.getLatitude(),
                    point.getLongitude(),
                    point.getStatus().toLowerCase(),
                    point.getCity().replace("'", "\\'"),
                    point.getDistrict().replace("'", "\\'"),
                    point.getAddress().replace("'", "\\'"),
                    point.getPaymentMethods().replace("'", "\\'"),
                    point.getWorkingHours().replace("'", "\\'")
                ));
            }
            jsArray.append("]");
            
            String jsCommand = "if (typeof addPaymentPoints === 'function') { addPaymentPoints(" + jsArray.toString() + "); }";
            webEngine.executeScript(jsCommand);
        } catch (Exception e) {
            System.err.println("Error updating map: " + e.getMessage());
        }
    }

    private void loadPaymentPoints() {
        loadingIndicator.setVisible(true);
        infoLabel.setText("Ödeme noktaları yükleniyor...");
        
        // Simulate API call with sample data
        Task<List<PaymentPoint>> task = new Task<List<PaymentPoint>>() {
            @Override
            protected List<PaymentPoint> call() throws Exception {
                // Simulate network delay
                Thread.sleep(1000);
                return new ArrayList<>(samplePaymentPoints);
            }
        };
        
        task.setOnSucceeded(e -> {
            allPaymentPoints = task.getValue();
            filterPaymentPoints();
            updateMapWithPaymentPoints();
            loadingIndicator.setVisible(false);
            infoLabel.setText(String.format("Toplam %d ödeme noktası yüklendi", allPaymentPoints.size()));
        });
        
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            infoLabel.setText("Ödeme noktaları yüklenemedi");
            showAlert("Hata", "Ödeme noktaları yüklenirken bir hata oluştu: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }

    private void filterPaymentPoints() {
        if (allPaymentPoints == null || allPaymentPoints.isEmpty()) {
            paymentPointsData.clear();
            return;
        }
        
        String selectedCity = cityFilter.getValue();
        String selectedPaymentMethod = paymentMethodFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        
        List<PaymentPoint> filteredPoints = allPaymentPoints.stream()
            .filter(point -> {
                if (!"Tümü".equals(selectedCity) && !point.getCity().equalsIgnoreCase(selectedCity)) {
                    return false;
                }
                if (!"Tümü".equals(selectedPaymentMethod) && !point.getPaymentMethods().contains(selectedPaymentMethod)) {
                    return false;
                }
                if (!"Tümü".equals(selectedStatus) && !point.getStatus().equalsIgnoreCase(selectedStatus)) {
                    return false;
                }
                return true;
            })
            .toList();
        
        paymentPointsData.clear();
        paymentPointsData.addAll(filteredPoints);
        
        infoLabel.setText(String.format("Filtrelenmiş %d ödeme noktası", filteredPoints.size()));
        
        // Update map with filtered points
        updateMapWithFilteredPoints(filteredPoints);
    }

    private void updateMapWithFilteredPoints(List<PaymentPoint> filteredPoints) {
        if (!mapInitialized) return;
        
        try {
            StringBuilder jsArray = new StringBuilder("[");
            for (int i = 0; i < filteredPoints.size(); i++) {
                PaymentPoint point = filteredPoints.get(i);
                if (i > 0) jsArray.append(",");
                jsArray.append(String.format(
                    "{name:'%s', latitude:%f, longitude:%f, status:'%s', city:'%s', district:'%s', address:'%s', paymentMethods:'%s', workingHours:'%s'}",
                    point.getName().replace("'", "\\'"),
                    point.getLatitude(),
                    point.getLongitude(),
                    point.getStatus().toLowerCase(),
                    point.getCity().replace("'", "\\'"),
                    point.getDistrict().replace("'", "\\'"),
                    point.getAddress().replace("'", "\\'"),
                    point.getPaymentMethods().replace("'", "\\'"),
                    point.getWorkingHours().replace("'", "\\'")
                ));
            }
            jsArray.append("]");
            
            String jsCommand = "if (typeof addPaymentPoints === 'function') { addPaymentPoints(" + jsArray.toString() + "); }";
            webEngine.executeScript(jsCommand);
        } catch (Exception e) {
            System.err.println("Error updating map with filtered points: " + e.getMessage());
        }
    }

    private void searchNearbyPoints() {
        String location = searchLocationField.getText().trim();
        double radius = radiusSpinner.getValue();
        
        if (location.isEmpty()) {
            showAlert("Uyarı", "Lütfen arama yapılacak konumu girin.");
            return;
        }
        
        loadingIndicator.setVisible(true);
        infoLabel.setText("Yakındaki ödeme noktaları aranıyor...");
        
        // Simulate geocoding and nearby search
        Task<List<PaymentPoint>> task = new Task<List<PaymentPoint>>() {
            @Override
            protected List<PaymentPoint> call() throws Exception {
                Thread.sleep(1500); // Simulate API call
                // In real implementation, geocode the location and search nearby points
                // For demo, return filtered results based on city name
                // Use radius parameter in actual implementation
                return allPaymentPoints.stream()
                    .filter(point -> point.getCity().toLowerCase().contains(location.toLowerCase()) ||
                                   point.getDistrict().toLowerCase().contains(location.toLowerCase()) ||
                                   point.getAddress().toLowerCase().contains(location.toLowerCase()))
                    .toList();
            }
        };
        
        task.setOnSucceeded(e -> {
            List<PaymentPoint> nearbyPoints = task.getValue();
            paymentPointsData.clear();
            paymentPointsData.addAll(nearbyPoints);
            updateMapWithFilteredPoints(nearbyPoints);
            loadingIndicator.setVisible(false);
            infoLabel.setText(String.format("'%s' yakınında %d ödeme noktası bulundu", location, nearbyPoints.size()));
        });
        
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            infoLabel.setText("Yakındaki ödeme noktaları aranamadı");
            showAlert("Hata", "Konum arama sırasında bir hata oluştu: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }

    private void onPaymentPointSelected(MouseEvent event) {
        PaymentPoint selected = paymentPointsList.getSelectionModel().getSelectedItem();
        if (selected != null && event.getClickCount() == 2) {
            // Haritada seçilen noktayı merkeze al
            try {
                String jsCommand = String.format("if (typeof centerMap === 'function') { centerMap(%f, %f, 16); }", 
                    selected.getLatitude(), selected.getLongitude());
                webEngine.executeScript(jsCommand);
            } catch (Exception e) {
                System.err.println("Error centering map: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // PaymentPoint sınıfı
    public static class PaymentPoint {
        private Long id;
        private String name;
        private String city;
        private String district;
        private String address;
        private String locationType;
        private Double latitude;
        private Double longitude;
        private String paymentMethods;
        private String status;
        private String state;
        private String description;
        private String workingHours;
        private String contactPhone;

        public PaymentPoint() {}

        public PaymentPoint(Long id, String name, String city, String district, String address, 
                           String locationType, Double latitude, Double longitude, String paymentMethods, 
                           String status, String state, String description, String workingHours, String contactPhone) {
            this.id = id;
            this.name = name;
            this.city = city;
            this.district = district;
            this.address = address;
            this.locationType = locationType;
            this.latitude = latitude;
            this.longitude = longitude;
            this.paymentMethods = paymentMethods;
            this.status = status;
            this.state = state;
            this.description = description;
            this.workingHours = workingHours;
            this.contactPhone = contactPhone;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getLocationType() { return locationType; }
        public void setLocationType(String locationType) { this.locationType = locationType; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getPaymentMethods() { return paymentMethods; }
        public void setPaymentMethods(String paymentMethods) { this.paymentMethods = paymentMethods; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getWorkingHours() { return workingHours; }
        public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

        @Override
        public String toString() {
            return name + " (" + city + " - " + district + ")";
        }
    }

    // Custom ListCell for PaymentPoint
    private static class PaymentPointListCell extends ListCell<PaymentPoint> {
        @Override
        protected void updateItem(PaymentPoint item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox content = new VBox(5);
                content.setPadding(new Insets(5));
                
                Label nameLabel = new Label(item.getName());
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                nameLabel.setTextFill(Color.DARKBLUE);
                
                Label locationLabel = new Label(item.getCity() + " - " + item.getDistrict());
                locationLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
                locationLabel.setTextFill(Color.GRAY);
                
                Label statusLabel = new Label(item.getStatus());
                statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                statusLabel.setTextFill("Aktif".equals(item.getStatus()) ? Color.GREEN : Color.RED);
                
                Label paymentMethodsLabel = new Label(item.getPaymentMethods());
                paymentMethodsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
                paymentMethodsLabel.setTextFill(Color.DARKGRAY);
                
                content.getChildren().addAll(nameLabel, locationLabel, statusLabel, paymentMethodsLabel);
                setGraphic(content);
            }
        }
    }
}
