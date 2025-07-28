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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Duraklar Listesi Sayfası
 * Tüm durakları tabloda gösterir, filtreleme ve yönetim imkanı sağlar
 */
public class StationsListPage {
    
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    
    private TableView<Station> stationsTable;
    private ObservableList<Station> stationsList;
    private Label statusLabel;
    private ComboBox<String> typeFilter;
    private TextField searchField;
    
    public StationsListPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.stationsList = FXCollections.observableArrayList();
        
        initializePage();
        loadStationsData();
    }
    
    private void initializePage() {
        // Ana sayfa düzeni
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Üst kısım tümü - sol üst buton + başlık ve filtreler
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
        
        // Üst kısım - Başlık ve filtreler
        VBox topContainer = createTopSection();
        
        fullTopContainer.getChildren().addAll(topLeftBox, topContainer);
        root.setTop(fullTopContainer);
        
        // Orta kısım - Tablo
        VBox centerContainer = createCenterSection();
        root.setCenter(centerContainer);
        
        // Alt kısım - Durum ve butonlar
        HBox bottomContainer = createBottomSection();
        root.setBottom(bottomContainer);
        
        // Sahne oluştur
        Scene scene = new Scene(root, 1400, 900);
        stage.setTitle("Duraklar Listesi - BinCard Superadmin");
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createTopSection() {
        VBox topContainer = new VBox(20);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Başlık satırı
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🚌 Duraklar Listesi");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Yenile butonu
        Button refreshButton = new Button("🔄 Yenile");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> loadStationsData());
        
        // Yeni durak ekle butonu
        Button addButton = new Button("➕ Yeni Durak");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addButton.setOnAction(e -> {
            new StationAddPage(stage, accessToken, refreshToken);
        });
        
        titleRow.getChildren().addAll(titleLabel, spacer, refreshButton, addButton);
        
        // Filtreler kartı
        VBox filtersCard = createFiltersCard();
        
        topContainer.getChildren().addAll(titleRow, filtersCard);
        return topContainer;
    }
    
    private VBox createFiltersCard() {
        VBox filtersCard = new VBox(15);
        filtersCard.setPadding(new Insets(20));
        filtersCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label filtersTitle = new Label("🔍 Filtreler");
        filtersTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        filtersTitle.setTextFill(Color.web("#2c3e50"));
        
        HBox filtersRow = new HBox(20);
        filtersRow.setAlignment(Pos.CENTER_LEFT);
        
        // Arama kutusu
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Durak Ara:");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Durak adı veya adres...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Tip filtresi
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Durak Tipi:");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tümü", "METRO", "TRAMVAY", "OTOBUS", "METROBUS", "TREN", 
                                    "VAPUR", "TELEFERIK", "DOLMUS", "MINIBUS", "HAVARAY",
                                    "FERIBOT", "HIZLI_TREN", "BISIKLET", "SCOOTER", 
                                    "PARK_YERI", "AKILLI_DURAK", "TERMINAL", "ULAŞIM_AKTARMA", "DIGER");
        typeFilter.setValue("Tümü");
        typeFilter.setPrefWidth(200);
        typeFilter.setOnAction(e -> applyFilters());
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Temizle butonu
        Button clearButton = new Button("🗑️ Temizle");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> clearFilters());
        clearButton.setAlignment(Pos.BOTTOM_CENTER);
        
        filtersRow.getChildren().addAll(searchBox, typeBox, clearButton);
        filtersCard.getChildren().addAll(filtersTitle, filtersRow);
        
        return filtersCard;
    }
    
    private VBox createCenterSection() {
        VBox centerContainer = new VBox(15);
        
        // Tablo kartı
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label tableTitle = new Label("📋 Duraklar Tablosu");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web("#2c3e50"));
        
        // Tablo oluştur
        createStationsTable();
        
        tableCard.getChildren().addAll(tableTitle, stationsTable);
        centerContainer.getChildren().add(tableCard);
        
        return centerContainer;
    }
    
    private void createStationsTable() {
        stationsTable = new TableView<>();
        stationsTable.setItems(stationsList);
        stationsTable.setPrefHeight(400);
        stationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Kolonu
        TableColumn<Station, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        
        // Ad Kolonu
        TableColumn<Station, String> nameColumn = new TableColumn<>("Durak Adı");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        // Tip Kolonu
        TableColumn<Station, String> typeColumn = new TableColumn<>("Tip");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(120);
        
        // Şehir Kolonu
        TableColumn<Station, String> cityColumn = new TableColumn<>("Şehir");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityColumn.setPrefWidth(100);
        
        // İlçe Kolonu
        TableColumn<Station, String> districtColumn = new TableColumn<>("İlçe");
        districtColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
        districtColumn.setPrefWidth(120);
        
        // Koordinat Kolonu
        TableColumn<Station, String> coordinatesColumn = new TableColumn<>("Koordinatlar");
        coordinatesColumn.setCellValueFactory(cellData -> {
            Station station = cellData.getValue();
            return new SimpleStringProperty(station.getLatitude() + ", " + station.getLongitude());
        });
        coordinatesColumn.setPrefWidth(150);
        
        // Durum Kolonu
        TableColumn<Station, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);
        statusColumn.setCellFactory(column -> new TableCell<Station, String>() {
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
        
        // Oluşturma Tarihi Kolonu
        TableColumn<Station, String> createdColumn = new TableColumn<>("Oluşturma Tarihi");
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdColumn.setPrefWidth(150);
        
        // İşlemler Kolonu
        TableColumn<Station, Void> actionsColumn = new TableColumn<>("İşlemler");
        actionsColumn.setPrefWidth(200);
        actionsColumn.setCellFactory(column -> new TableCell<Station, Void>() {
            private final Button editButton = new Button("✏️ Düzenle");
            private final Button statusButton = new Button("🔄 Durum");
            private final Button deleteButton = new Button("🗑️ Sil");
            private final HBox buttonsBox = new HBox(5);
            
            {
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                statusButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                
                editButton.setOnAction(e -> {
                    Station station = getTableView().getItems().get(getIndex());
                    editStation(station);
                });
                
                statusButton.setOnAction(e -> {
                    Station station = getTableView().getItems().get(getIndex());
                    toggleStationStatus(station);
                });
                
                deleteButton.setOnAction(e -> {
                    Station station = getTableView().getItems().get(getIndex());
                    deleteStation(station);
                });
                
                buttonsBox.getChildren().addAll(editButton, statusButton, deleteButton);
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
        
        stationsTable.getColumns().addAll(idColumn, nameColumn, typeColumn, cityColumn, 
                                         districtColumn, coordinatesColumn, statusColumn, 
                                         createdColumn, actionsColumn);
    }
    
    private HBox createBottomSection() {
        HBox bottomContainer = new HBox(15);
        bottomContainer.setAlignment(Pos.CENTER_LEFT);
        bottomContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Durum etiketi
        statusLabel = new Label("📊 Duraklar yükleniyor...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#7f8c8d"));
        
        bottomContainer.getChildren().add(statusLabel);
        return bottomContainer;
    }
    
    private void loadStationsData() {
        statusLabel.setText("📊 Duraklar yükleniyor...");
        
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    System.out.println("🔍 DEBUG: loadStationsData() başlatıldı - Gerçek API çağrısı yapılıyor...");
                    System.out.println("🔍 DEBUG: AccessToken var mı? " + (accessToken != null ? "Evet" : "Hayır"));
                    
                    // GERÇEİ API ÇAĞRISI - StationApiClient kullan
                    // searchStations(String name, int page, int size) formatına uygun çağrı
                    String apiResponse = StationApiClient.searchStations("", 0, 100);
                    System.out.println("🔍 DEBUG: API Yanıtı alındı: " + apiResponse);
                    
                    // TODO: API yanıtını parse et ve stationsList'e ekle
                    // Şu an için hala simüle veri kullanıyoruz ama API çağrısı da yapılıyor
                    
                    Platform.runLater(() -> {
                        stationsList.clear();
                        
                        System.out.println("🔍 DEBUG: Stations listesi temizlendi, örnek veriler ekleniyor...");
                        
                        // Örnek durak verileri (geçici - API parse edilene kadar)
                        stationsList.addAll(
                            new Station("1", "Taksim Meydanı Durağı", "METRO", "İstanbul", "Beyoğlu", 
                                      "Taksim Meydanı", "34435", 41.0369, 28.9851, "ACTIVE", 
                                      LocalDateTime.now().minusDays(10).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                            new Station("2", "Kadıköy İskele Durağı", "VAPUR", "İstanbul", "Kadıköy", 
                                      "İskele Caddesi", "34710", 40.9996, 29.0277, "ACTIVE", 
                                      LocalDateTime.now().minusDays(8).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                            new Station("3", "Mecidiyeköy Metrobüs Durağı", "METROBUS", "İstanbul", "Şişli", 
                                      "Büyükdere Caddesi", "34394", 41.0631, 28.9897, "ACTIVE", 
                                      LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                            new Station("4", "Eminönü Tramvay Durağı", "TRAMVAY", "İstanbul", "Fatih", 
                                      "Eminönü Meydanı", "34110", 41.0169, 28.9705, "INACTIVE", 
                                      LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                            new Station("5", "Beşiktaş Dolmuş Durağı", "DOLMUS", "İstanbul", "Beşiktaş", 
                                      "Barbaros Bulvarı", "34353", 41.0423, 29.0061, "ACTIVE", 
                                      LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                        );
                        
                        updateStatusLabel();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Duraklar yüklenirken hata oluştu: " + e.getMessage());
                        showErrorAlert("Hata", "Duraklar yüklenirken hata oluştu: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void applyFilters() {
        ObservableList<Station> filteredList = FXCollections.observableArrayList();
        
        String searchText = searchField.getText().toLowerCase();
        String selectedType = typeFilter.getValue();
        
        for (Station station : stationsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                station.getName().toLowerCase().contains(searchText) ||
                station.getCity().toLowerCase().contains(searchText) ||
                station.getDistrict().toLowerCase().contains(searchText) ||
                station.getStreet().toLowerCase().contains(searchText);
            
            boolean matchesType = "Tümü".equals(selectedType) || station.getType().equals(selectedType);
            
            if (matchesSearch && matchesType) {
                filteredList.add(station);
            }
        }
        
        stationsTable.setItems(filteredList);
        updateStatusLabel();
    }
    
    private void clearFilters() {
        searchField.clear();
        typeFilter.setValue("Tümü");
        stationsTable.setItems(stationsList);
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        int totalStations = stationsList.size();
        int visibleStations = stationsTable.getItems().size();
        int activeStations = (int) stationsList.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
        
        if (visibleStations == totalStations) {
            statusLabel.setText(String.format("📊 Toplam %d durak • %d aktif • %d pasif", 
                                             totalStations, activeStations, totalStations - activeStations));
        } else {
            statusLabel.setText(String.format("📊 %d/%d durak gösteriliyor • %d aktif", 
                                             visibleStations, totalStations, activeStations));
        }
    }
    
    private void editStation(Station station) {
        showInfoAlert("✏️ Düzenleme", "Durak düzenleme özelliği yakında eklenecek.");
    }
    
    private void toggleStationStatus(Station station) {
        String newStatus = "ACTIVE".equals(station.getStatus()) ? "INACTIVE" : "ACTIVE";
        station.setStatus(newStatus);
        stationsTable.refresh();
        updateStatusLabel();
        
        showSuccessAlert("🔄 Durum Değişti", 
                        "Durak durumu başarıyla " + ("ACTIVE".equals(newStatus) ? "aktif" : "pasif") + " olarak değiştirildi.");
    }
    
    private void deleteStation(Station station) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("🗑️ Durak Sil");
        confirmAlert.setHeaderText("Durağı silmek istediğinizden emin misiniz?");
        confirmAlert.setContentText("Bu işlem geri alınamaz. Durak: " + station.getName());
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            stationsList.remove(station);
            updateStatusLabel();
            showSuccessAlert("🗑️ Silindi", "Durak başarıyla silindi.");
        }
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
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Station model sınıfı
    public static class Station {
        private String id;
        private String name;
        private String type;
        private String city;
        private String district;
        private String street;
        private String postalCode;
        private double latitude;
        private double longitude;
        private String status;
        private String createdAt;
        
        public Station(String id, String name, String type, String city, String district, 
                      String street, String postalCode, double latitude, double longitude, 
                      String status, String createdAt) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.city = city;
            this.district = district;
            this.street = street;
            this.postalCode = postalCode;
            this.latitude = latitude;
            this.longitude = longitude;
            this.status = status;
            this.createdAt = createdAt;
        }
        
        // Getter ve Setter metotları
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
