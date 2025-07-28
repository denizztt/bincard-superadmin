package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.concurrent.Task;

/**
 * Durak Arama Sayfası
 * Gelişmiş filtreleme ve arama özellikleri ile durak bulma
 */
public class StationSearchPage {
    
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    
    private TableView<SearchResult> resultsTable;
    private ObservableList<SearchResult> searchResults;
    private Label statusLabel;
    
    // Arama kriterleri
    private TextField nameSearchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> cityFilter;
    private ComboBox<String> districtFilter;
    private ComboBox<String> statusFilter;
    private Slider radiusSlider;
    private TextField coordinatesField;
    
    public StationSearchPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.searchResults = FXCollections.observableArrayList();
        
        initializePage();
    }
    
    private void initializePage() {
        // Ana sayfa düzeni
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Üst kısım tümü - sol üst buton + başlık
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
        
        // Üst kısım - Başlık
        VBox topContainer = createTopSection();
        
        fullTopContainer.getChildren().addAll(topLeftBox, topContainer);
        root.setTop(fullTopContainer);
        
        // Sol kısım - Arama filtreleri
        ScrollPane leftContainer = createLeftSection();
        root.setLeft(leftContainer);
        
        // Orta kısım - Sonuçlar tablosu
        VBox centerContainer = createCenterSection();
        root.setCenter(centerContainer);
        
        // Alt kısım - Durum ve butonlar
        HBox bottomContainer = createBottomSection();
        root.setBottom(bottomContainer);
        
        // Sahne oluştur
        Scene scene = new Scene(root, 1600, 900);
        stage.setTitle("Durak Ara - BinCard Superadmin");
        stage.setScene(scene);
        stage.show();
        
        // İlk arama yap
        performSearch();
    }
    
    private VBox createTopSection() {
        VBox topContainer = new VBox(15);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("🔍 Durak Arama");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        Label descLabel = new Label("Gelişmiş filtreler ve arama kriterleri ile durakları bulun");
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        topContainer.getChildren().addAll(titleLabel, descLabel);
        return topContainer;
    }
    
    private ScrollPane createLeftSection() {
        VBox filtersContainer = new VBox(20);
        filtersContainer.setPadding(new Insets(0, 20, 0, 0));
        filtersContainer.setPrefWidth(350);
        
        // Arama filtreleri kartı
        VBox searchCard = createSearchFiltersCard();
        
        // Konum filtreleri kartı
        VBox locationCard = createLocationFiltersCard();
        
        // Hızlı arama kartı
        VBox quickSearchCard = createQuickSearchCard();
        
        filtersContainer.getChildren().addAll(searchCard, locationCard, quickSearchCard);
        
        ScrollPane scrollPane = new ScrollPane(filtersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private VBox createSearchFiltersCard() {
        VBox searchCard = new VBox(15);
        searchCard.setPadding(new Insets(20));
        searchCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label searchTitle = new Label("🔍 Arama Filtreleri");
        searchTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        searchTitle.setTextFill(Color.web("#2c3e50"));
        
        // Durak adı arama
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Durak Adı:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameSearchField = new TextField();
        nameSearchField.setPromptText("Durak adı veya anahtar kelime...");
        nameSearchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
        nameBox.getChildren().addAll(nameLabel, nameSearchField);
        
        // Durak tipi
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Durak Tipi:");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tümü", "METRO", "TRAMVAY", "OTOBUS", "METROBUS", "TREN", 
                                    "VAPUR", "TELEFERIK", "DOLMUS", "MINIBUS", "HAVARAY",
                                    "FERIBOT", "HIZLI_TREN", "BISIKLET", "SCOOTER", 
                                    "PARK_YERI", "AKILLI_DURAK", "TERMINAL", "ULAŞIM_AKTARMA", "DIGER");
        typeFilter.setValue("Tümü");
        typeFilter.setMaxWidth(Double.MAX_VALUE);
        typeFilter.setOnAction(e -> performSearch());
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Durum filtresi
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Durum:");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "ACTIVE", "INACTIVE");
        statusFilter.setValue("Tümü");
        statusFilter.setMaxWidth(Double.MAX_VALUE);
        statusFilter.setOnAction(e -> performSearch());
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        searchCard.getChildren().addAll(searchTitle, nameBox, typeBox, statusBox);
        return searchCard;
    }
    
    private VBox createLocationFiltersCard() {
        VBox locationCard = new VBox(15);
        locationCard.setPadding(new Insets(20));
        locationCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label locationTitle = new Label("📍 Konum Filtreleri");
        locationTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        locationTitle.setTextFill(Color.web("#2c3e50"));
        
        // Şehir filtresi
        VBox cityBox = new VBox(5);
        Label cityLabel = new Label("Şehir:");
        cityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        cityFilter = new ComboBox<>();
        cityFilter.getItems().addAll("Tümü", "İstanbul", "Ankara", "İzmir", "Bursa", "Antalya");
        cityFilter.setValue("Tümü");
        cityFilter.setMaxWidth(Double.MAX_VALUE);
        cityFilter.setOnAction(e -> {
            updateDistrictFilter();
            performSearch();
        });
        cityBox.getChildren().addAll(cityLabel, cityFilter);
        
        // İlçe filtresi
        VBox districtBox = new VBox(5);
        Label districtLabel = new Label("İlçe:");
        districtLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        districtFilter = new ComboBox<>();
        districtFilter.getItems().add("Tümü");
        districtFilter.setValue("Tümü");
        districtFilter.setMaxWidth(Double.MAX_VALUE);
        districtFilter.setOnAction(e -> performSearch());
        districtBox.getChildren().addAll(districtLabel, districtFilter);
        
        // Yarıçap arama
        VBox radiusBox = new VBox(5);
        Label radiusLabel = new Label("Yarıçap Arama (km):");
        radiusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        radiusSlider = new Slider(0, 50, 0);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setMajorTickUnit(10);
        radiusSlider.setMinorTickCount(5);
        radiusSlider.setBlockIncrement(1);
        Label radiusValueLabel = new Label("Kapalı");
        radiusSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 0) {
                radiusValueLabel.setText("Kapalı");
            } else {
                radiusValueLabel.setText(String.format("%.1f km", newVal.doubleValue()));
            }
            performSearch();
        });
        radiusBox.getChildren().addAll(radiusLabel, radiusSlider, radiusValueLabel);
        
        // Koordinat girişi
        VBox coordBox = new VBox(5);
        Label coordLabel = new Label("Merkez Koordinat (Lat, Lng):");
        coordLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        coordinatesField = new TextField();
        coordinatesField.setPromptText("41.0082, 28.9784");
        coordinatesField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
        coordBox.getChildren().addAll(coordLabel, coordinatesField);
        
        locationCard.getChildren().addAll(locationTitle, cityBox, districtBox, radiusBox, coordBox);
        return locationCard;
    }
    
    private VBox createQuickSearchCard() {
        VBox quickCard = new VBox(15);
        quickCard.setPadding(new Insets(20));
        quickCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label quickTitle = new Label("⚡ Hızlı Arama");
        quickTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        quickTitle.setTextFill(Color.web("#2c3e50"));
        
        // Hızlı arama butonları
        VBox buttonsBox = new VBox(8);
        
        Button metroButton = createQuickSearchButton("🚇 Tüm Metro Durakları", "METRO");
        Button busButton = createQuickSearchButton("🚌 Tüm Otobüs Durakları", "OTOBUS");
        Button ferryButton = createQuickSearchButton("⛴️ Tüm Vapur Durakları", "VAPUR");
        Button activeButton = createQuickSearchButton("🟢 Sadece Aktif Duraklar", "ACTIVE_ONLY");
        Button istanbulButton = createQuickSearchButton("🏙️ İstanbul Durakları", "ISTANBUL");
        
        buttonsBox.getChildren().addAll(metroButton, busButton, ferryButton, activeButton, istanbulButton);
        
        // Temizle butonu
        Button clearButton = new Button("🗑️ Filtreleri Temizle");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(e -> clearAllFilters());
        
        quickCard.getChildren().addAll(quickTitle, buttonsBox, new Separator(), clearButton);
        return quickCard;
    }
    
    private Button createQuickSearchButton(String text, String searchType) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> performQuickSearch(searchType));
        return button;
    }
    
    private VBox createCenterSection() {
        VBox centerContainer = new VBox(15);
        centerContainer.setPadding(new Insets(0, 0, 0, 20));
        
        // Sonuçlar kartı
        VBox resultsCard = new VBox(15);
        resultsCard.setPadding(new Insets(20));
        resultsCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        HBox resultsHeader = new HBox();
        resultsHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label resultsTitle = new Label("📋 Arama Sonuçları");
        resultsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        resultsTitle.setTextFill(Color.web("#2c3e50"));
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Export butonu
        Button exportButton = new Button("📤 Excel'e Aktar");
        exportButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        exportButton.setOnAction(e -> exportResults());
        
        resultsHeader.getChildren().addAll(resultsTitle, spacer, exportButton);
        
        // Sonuçlar tablosu
        createResultsTable();
        
        resultsCard.getChildren().addAll(resultsHeader, resultsTable);
        centerContainer.getChildren().add(resultsCard);
        
        return centerContainer;
    }
    
    private void createResultsTable() {
        resultsTable = new TableView<>();
        resultsTable.setItems(searchResults);
        resultsTable.setPrefHeight(400);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Kolonu
        TableColumn<SearchResult, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(60);
        
        // Ad Kolonu
        TableColumn<SearchResult, String> nameColumn = new TableColumn<>("Durak Adı");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        // Tip Kolonu
        TableColumn<SearchResult, String> typeColumn = new TableColumn<>("Tip");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(100);
        
        // Konum Kolonu
        TableColumn<SearchResult, String> locationColumn = new TableColumn<>("Konum");
        locationColumn.setCellValueFactory(cellData -> {
            SearchResult result = cellData.getValue();
            return new SimpleStringProperty(result.getDistrict() + ", " + result.getCity());
        });
        locationColumn.setPrefWidth(150);
        
        // Koordinat Kolonu
        TableColumn<SearchResult, String> coordColumn = new TableColumn<>("Koordinatlar");
        coordColumn.setCellValueFactory(cellData -> {
            SearchResult result = cellData.getValue();
            return new SimpleStringProperty(String.format("%.4f, %.4f", result.getLatitude(), result.getLongitude()));
        });
        coordColumn.setPrefWidth(120);
        
        // Durum Kolonu
        TableColumn<SearchResult, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);
        statusColumn.setCellFactory(column -> new TableCell<SearchResult, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("ACTIVE".equals(status)) {
                        setStyle("-fx-background-color: #d5edda; -fx-text-fill: #155724;");
                    } else {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });
        
        // Mesafe Kolonu (yarıçap arama aktifse)
        TableColumn<SearchResult, String> distanceColumn = new TableColumn<>("Mesafe (km)");
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        distanceColumn.setPrefWidth(100);
        
        // İşlemler Kolonu
        TableColumn<SearchResult, Void> actionsColumn = new TableColumn<>("İşlemler");
        actionsColumn.setPrefWidth(150);
        actionsColumn.setCellFactory(column -> new TableCell<SearchResult, Void>() {
            private final Button viewButton = new Button("👁️ Görüntüle");
            private final Button mapButton = new Button("🗺️ Harita");
            private final HBox buttonsBox = new HBox(5);
            
            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                mapButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                
                viewButton.setOnAction(e -> {
                    SearchResult result = getTableView().getItems().get(getIndex());
                    viewStationDetails(result);
                });
                
                mapButton.setOnAction(e -> {
                    SearchResult result = getTableView().getItems().get(getIndex());
                    showOnMap(result);
                });
                
                buttonsBox.getChildren().addAll(viewButton, mapButton);
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
        
        resultsTable.getColumns().addAll(idColumn, nameColumn, typeColumn, locationColumn, 
                                        coordColumn, statusColumn, distanceColumn, actionsColumn);
    }
    
    private HBox createBottomSection() {
        HBox bottomContainer = new HBox(15);
        bottomContainer.setAlignment(Pos.CENTER_LEFT);
        bottomContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Durum etiketi
        statusLabel = new Label("📊 Sonuçlar yükleniyor...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#7f8c8d"));
        
        bottomContainer.getChildren().add(statusLabel);
        return bottomContainer;
    }
    
    private void updateDistrictFilter() {
        String selectedCity = cityFilter.getValue();
        districtFilter.getItems().clear();
        districtFilter.getItems().add("Tümü");
        
        if ("İstanbul".equals(selectedCity)) {
            districtFilter.getItems().addAll("Beyoğlu", "Kadıköy", "Üsküdar", "Fatih", "Beşiktaş", 
                                           "Şişli", "Bakırköy", "Maltepe", "Pendik", "Ataşehir");
        } else if ("Ankara".equals(selectedCity)) {
            districtFilter.getItems().addAll("Çankaya", "Keçiören", "Mamak", "Sincan", "Altındağ");
        } else if ("İzmir".equals(selectedCity)) {
            districtFilter.getItems().addAll("Konak", "Bornova", "Karşıyaka", "Alsancak", "Buca");
        }
        
        districtFilter.setValue("Tümü");
    }
    
    private void performSearch() {
        statusLabel.setText("🔍 Arama yapılıyor...");
        
        Task<Void> searchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(500); // Arama simülasyonu
                
                Platform.runLater(() -> {
                    searchResults.clear();
                    
                    // Simüle edilmiş arama sonuçları
                    // Gerçek uygulamada StationApiClient.searchStations() kullanılacak
                    
                    String nameSearch = nameSearchField.getText().toLowerCase();
                    String typeSearch = typeFilter.getValue();
                    String citySearch = cityFilter.getValue();
                    String districtSearch = districtFilter.getValue();
                    String statusSearch = statusFilter.getValue();
                    double radius = radiusSlider.getValue();
                    
                    // Örnek sonuçlar
                    if (nameSearch.isEmpty() || "taksim".contains(nameSearch)) {
                        searchResults.add(new SearchResult("1", "Taksim Meydanı", "METRO", "İstanbul", 
                                                          "Beyoğlu", 41.0369, 28.9851, "ACTIVE", "0.5"));
                    }
                    if (nameSearch.isEmpty() || "kadıköy".contains(nameSearch)) {
                        searchResults.add(new SearchResult("2", "Kadıköy İskele", "VAPUR", "İstanbul", 
                                                          "Kadıköy", 40.9996, 29.0277, "ACTIVE", "2.3"));
                    }
                    if (nameSearch.isEmpty() || "mecidiyeköy".contains(nameSearch)) {
                        searchResults.add(new SearchResult("3", "Mecidiyeköy", "METROBUS", "İstanbul", 
                                                          "Şişli", 41.0631, 28.9897, "ACTIVE", "1.8"));
                    }
                    
                    updateStatusLabel();
                });
                
                return null;
            }
        };
        
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }
    
    private void performQuickSearch(String searchType) {
        switch (searchType) {
            case "METRO":
                typeFilter.setValue("METRO");
                break;
            case "OTOBUS":
                typeFilter.setValue("OTOBUS");
                break;
            case "VAPUR":
                typeFilter.setValue("VAPUR");
                break;
            case "ACTIVE_ONLY":
                statusFilter.setValue("ACTIVE");
                break;
            case "ISTANBUL":
                cityFilter.setValue("İstanbul");
                updateDistrictFilter();
                break;
        }
        performSearch();
    }
    
    private void clearAllFilters() {
        nameSearchField.clear();
        typeFilter.setValue("Tümü");
        cityFilter.setValue("Tümü");
        districtFilter.setValue("Tümü");
        statusFilter.setValue("Tümü");
        radiusSlider.setValue(0);
        coordinatesField.clear();
        performSearch();
    }
    
    private void updateStatusLabel() {
        int resultCount = searchResults.size();
        statusLabel.setText(String.format("📊 %d arama sonucu bulundu", resultCount));
    }
    
    private void viewStationDetails(SearchResult result) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("🚌 Durak Detayları");
        detailAlert.setHeaderText(result.getName());
        detailAlert.setContentText(String.format(
            "ID: %s\\nTip: %s\\nKonum: %s, %s\\nKoordinat: %.4f, %.4f\\nDurum: %s",
            result.getId(), result.getType(), result.getDistrict(), result.getCity(),
            result.getLatitude(), result.getLongitude(), result.getStatus()
        ));
        detailAlert.showAndWait();
    }
    
    private void showOnMap(SearchResult result) {
        String mapsUrl = String.format(
            "https://www.google.com/maps?q=%.6f,%.6f",
            result.getLatitude(), result.getLongitude()
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", mapsUrl);
            pb.start();
        } catch (Exception e) {
            showErrorAlert("Hata", "Harita açılırken hata oluştu: " + e.getMessage());
        }
    }
    
    private void exportResults() {
        showInfoAlert("📤 Export", "Excel export özelliği yakında eklenecek.");
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // SearchResult model sınıfı
    public static class SearchResult {
        private String id;
        private String name;
        private String type;
        private String city;
        private String district;
        private double latitude;
        private double longitude;
        private String status;
        private String distance;
        
        public SearchResult(String id, String name, String type, String city, String district,
                           double latitude, double longitude, String status, String distance) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.city = city;
            this.district = district;
            this.latitude = latitude;
            this.longitude = longitude;
            this.status = status;
            this.distance = distance;
        }
        
        // Getter metotları
        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getCity() { return city; }
        public String getDistrict() { return district; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getStatus() { return status; }
        public String getDistance() { return distance; }
    }
}
