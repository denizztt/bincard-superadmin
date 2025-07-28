package com.bincard.bincard_superadmin;

import com.bincard.bincard_superadmin.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaymentPointsPage extends SuperadminPageBase {
    
    private TableView<PaymentPoint> paymentPointsTable;
    private ObservableList<PaymentPoint> paymentPointsData = FXCollections.observableArrayList();
    private TextField searchField;
    private ComboBox<String> cityFilter;
    private ComboBox<PaymentMethod> paymentMethodFilter;
    private CheckBox activeFilter;
    private Label statusLabel;
    private Button refreshButton;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button toggleStatusButton;
    private Pagination pagination;
    
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    
    public PaymentPointsPage(Stage primaryStage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(primaryStage, accessToken, refreshToken, "Ödeme Noktaları Yönetimi");
        
        // İçeriği ayarla
        root.setCenter(createContent());
        
        // Sahneyi ayarla
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Verileri yükle
        loadPaymentPoints();
    }

    @Override
    protected Node createContent() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Üst kontrol paneli
        HBox topControls = createTopControls();
        
        // Tablo
        paymentPointsTable = createPaymentPointsTable();
        
        // Alt kontrol paneli
        HBox bottomControls = createBottomControls();
        
        // Pagination
        pagination = new Pagination();
        pagination.setPageFactory(this::createPage);
        
        // Status label
        statusLabel = new Label("Ödeme noktaları yükleniyor...");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        mainLayout.getChildren().addAll(topControls, paymentPointsTable, bottomControls, pagination, statusLabel);
        
        return mainLayout;
    }
    
    private HBox createTopControls() {
        HBox topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.setPadding(new Insets(10));
        
        // Arama kutusu
        searchField = new TextField();
        searchField.setPromptText("Ödeme noktası ara...");
        searchField.setPrefWidth(200);
        searchField.setOnKeyReleased(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                applyFilters();
            }
        });
        
        // Şehir filtresi
        cityFilter = new ComboBox<>();
        cityFilter.setPromptText("Şehir Seç");
        cityFilter.setPrefWidth(150);
        cityFilter.setOnAction(e -> applyFilters());
        
        // Ödeme yöntemi filtresi
        paymentMethodFilter = new ComboBox<>();
        paymentMethodFilter.setPromptText("Ödeme Yöntemi");
        paymentMethodFilter.setPrefWidth(150);
        paymentMethodFilter.getItems().addAll(PaymentMethod.values());
        paymentMethodFilter.setOnAction(e -> applyFilters());
        
        // Aktif durumu filtresi
        activeFilter = new CheckBox("Sadece Aktif");
        activeFilter.setSelected(false);
        activeFilter.setOnAction(e -> applyFilters());
        
        // Yenile butonu
        refreshButton = new Button("🔄 Yenile");
        refreshButton.setOnAction(e -> {
            currentPage = 0;
            loadPaymentPoints();
        });
        
        // Temizle butonu
        Button clearButton = new Button("🗑️ Temizle");
        clearButton.setOnAction(e -> clearFilters());
        
        topControls.getChildren().addAll(
            new Label("Arama:"), searchField,
            new Label("Şehir:"), cityFilter,
            new Label("Ödeme:"), paymentMethodFilter,
            activeFilter, refreshButton, clearButton
        );
        
        return topControls;
    }
    
    private TableView<PaymentPoint> createPaymentPointsTable() {
        TableView<PaymentPoint> table = new TableView<>();
        table.setItems(paymentPointsData);
        table.setRowFactory(tv -> {
            TableRow<PaymentPoint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showPaymentPointDetails(row.getItem());
                }
            });
            return row;
        });
        
        // ID kolonu
        TableColumn<PaymentPoint, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);
        
        // İsim kolonu
        TableColumn<PaymentPoint, String> nameColumn = new TableColumn<>("İsim");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        // Şehir kolonu
        TableColumn<PaymentPoint, String> cityColumn = new TableColumn<>("Şehir");
        cityColumn.setCellValueFactory(cellData -> {
            Address address = cellData.getValue().getAddress();
            return new javafx.beans.property.SimpleStringProperty(
                address != null ? address.getCity() : ""
            );
        });
        cityColumn.setPrefWidth(100);
        
        // İlçe kolonu
        TableColumn<PaymentPoint, String> districtColumn = new TableColumn<>("İlçe");
        districtColumn.setCellValueFactory(cellData -> {
            Address address = cellData.getValue().getAddress();
            return new javafx.beans.property.SimpleStringProperty(
                address != null ? address.getDistrict() : ""
            );
        });
        districtColumn.setPrefWidth(100);
        
        // Çalışma saatleri kolonu
        TableColumn<PaymentPoint, String> workingHoursColumn = new TableColumn<>("Çalışma Saatleri");
        workingHoursColumn.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        workingHoursColumn.setPrefWidth(120);
        
        // Ödeme yöntemleri kolonu
        TableColumn<PaymentPoint, String> paymentMethodsColumn = new TableColumn<>("Ödeme Yöntemleri");
        paymentMethodsColumn.setCellValueFactory(cellData -> {
            List<PaymentMethod> methods = cellData.getValue().getPaymentMethods();
            String methodsStr = "";
            if (methods != null) {
                methodsStr = methods.stream()
                    .map(PaymentMethod::getDisplayName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            }
            return new javafx.beans.property.SimpleStringProperty(methodsStr);
        });
        paymentMethodsColumn.setPrefWidth(150);
        
        // Aktif durum kolonu
        TableColumn<PaymentPoint, Boolean> activeColumn = new TableColumn<>("Durum");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(column -> new TableCell<PaymentPoint, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(active ? "Aktif" : "Pasif");
                    setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        activeColumn.setPrefWidth(80);
        
        // Mesafe kolonu
        TableColumn<PaymentPoint, Double> distanceColumn = new TableColumn<>("Mesafe (km)");
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        distanceColumn.setCellFactory(column -> new TableCell<PaymentPoint, Double>() {
            @Override
            protected void updateItem(Double distance, boolean empty) {
                super.updateItem(distance, empty);
                if (empty || distance == null) {
                    setText("");
                } else {
                    setText(String.format("%.2f km", distance));
                }
            }
        });
        distanceColumn.setPrefWidth(100);
        
        // Aksiyonlar kolonu
        TableColumn<PaymentPoint, Void> actionsColumn = new TableColumn<>("Aksiyonlar");
        actionsColumn.setCellFactory(new Callback<TableColumn<PaymentPoint, Void>, TableCell<PaymentPoint, Void>>() {
            @Override
            public TableCell<PaymentPoint, Void> call(TableColumn<PaymentPoint, Void> param) {
                return new TableCell<PaymentPoint, Void>() {
                    private final Button viewButton = new Button("👁️");
                    private final Button editButton = new Button("✏️");
                    private final Button deleteButton = new Button("🗑️");
                    
                    {
                        viewButton.setOnAction(e -> {
                            PaymentPoint paymentPoint = getTableView().getItems().get(getIndex());
                            showPaymentPointDetails(paymentPoint);
                        });
                        
                        editButton.setOnAction(e -> {
                            PaymentPoint paymentPoint = getTableView().getItems().get(getIndex());
                            showEditPaymentPointDialog(paymentPoint);
                        });
                        
                        deleteButton.setOnAction(e -> {
                            PaymentPoint paymentPoint = getTableView().getItems().get(getIndex());
                            deletePaymentPoint(paymentPoint);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.setAlignment(Pos.CENTER);
                            buttons.getChildren().addAll(viewButton, editButton, deleteButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
        actionsColumn.setPrefWidth(150);
        
        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(cityColumn);
        table.getColumns().add(districtColumn);
        table.getColumns().add(workingHoursColumn);
        table.getColumns().add(paymentMethodsColumn);
        table.getColumns().add(activeColumn);
        table.getColumns().add(distanceColumn);
        table.getColumns().add(actionsColumn);
        
        return table;
    }
    
    private HBox createBottomControls() {
        HBox bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER_LEFT);
        bottomControls.setPadding(new Insets(10));
        
        // Yeni ödeme noktası ekle butonu
        addButton = new Button("➕ Yeni Ödeme Noktası");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddPaymentPointDialog());
        
        // Düzenle butonu
        editButton = new Button("✏️ Düzenle");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editButton.setDisable(true);
        editButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditPaymentPointDialog(selected);
            }
        });
        
        // Sil butonu
        deleteButton = new Button("🗑️ Sil");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deletePaymentPoint(selected);
            }
        });
        
        // Durum değiştir butonu
        toggleStatusButton = new Button("🔄 Durum Değiştir");
        toggleStatusButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        toggleStatusButton.setDisable(true);
        toggleStatusButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                togglePaymentPointStatus(selected);
            }
        });
        
        // Tablo seçim durumu dinleyicisi
        paymentPointsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            toggleStatusButton.setDisable(!hasSelection);
        });
        
        bottomControls.getChildren().addAll(addButton, editButton, deleteButton, toggleStatusButton);
        
        return bottomControls;
    }
    
    private VBox createPage(int pageIndex) {
        // Sadece sayfa değiştirildiyse yükle
        if (currentPage != pageIndex) {
            currentPage = pageIndex;
            loadPaymentPoints();
        }
        return new VBox(); // Boş VBox döndürüyoruz çünkü asıl içerik table'da
    }
    
    private void loadPaymentPoints() {
        // Çoklu yükleme yapılmasın
        if (statusLabel.getText().contains("yükleniyor")) {
            return;
        }
        
        setStatus("Ödeme noktaları yükleniyor...", false);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                String response = ApiClientFX.getAllPaymentPoints(accessToken);
                return parsePaymentPointsResponse(response);
            } catch (IOException e) {
                Platform.runLater(() -> {
                    setStatus("Ödeme noktaları yüklenirken hata oluştu: " + e.getMessage(), true);
                    showAlert("Hata", "Ödeme noktaları yüklenirken hata oluştu: " + e.getMessage());
                });
                return null;
            }
        }).thenAccept(paymentPointPage -> {
            if (paymentPointPage != null) {
                Platform.runLater(() -> {
                    paymentPointsData.clear();
                    paymentPointsData.addAll(paymentPointPage.getContent());
                    
                    totalPages = paymentPointPage.getTotalPages();
                    pagination.setPageCount(Math.max(1, totalPages));
                    
                    updateCityFilter();
                    setStatus("Toplam " + paymentPointPage.getTotalElements() + " ödeme noktası yüklendi", false);
                });
            }
        });
    }
    
    private PaymentPointPage parsePaymentPointsResponse(String jsonResponse) {
        try {
            System.out.println("Parsing response: " + jsonResponse);
            
            PaymentPointPage page = new PaymentPointPage();
            List<PaymentPoint> paymentPoints = new ArrayList<>();
            
            // JSON'dan temel bilgileri çıkart
            if (jsonResponse.contains("\"success\":true")) {
                // Data kısmından sayfa bilgilerini parse et
                if (jsonResponse.contains("\"pageNumber\":")) {
                    String pageNumStr = extractJsonValue(jsonResponse, "pageNumber");
                    page.setPageNumber(Integer.parseInt(pageNumStr));
                }
                
                if (jsonResponse.contains("\"pageSize\":")) {
                    String pageSizeStr = extractJsonValue(jsonResponse, "pageSize");
                    page.setPageSize(Integer.parseInt(pageSizeStr));
                }
                
                if (jsonResponse.contains("\"totalElements\":")) {
                    String totalElementsStr = extractJsonValue(jsonResponse, "totalElements");
                    page.setTotalElements(Long.parseLong(totalElementsStr));
                }
                
                if (jsonResponse.contains("\"totalPages\":")) {
                    String totalPagesStr = extractJsonValue(jsonResponse, "totalPages");
                    page.setTotalPages(Integer.parseInt(totalPagesStr));
                }
                
                if (jsonResponse.contains("\"first\":")) {
                    String firstStr = extractJsonValue(jsonResponse, "first");
                    page.setFirst(Boolean.parseBoolean(firstStr));
                }
                
                if (jsonResponse.contains("\"last\":")) {
                    String lastStr = extractJsonValue(jsonResponse, "last");
                    page.setLast(Boolean.parseBoolean(lastStr));
                }
                
                // Content array'i parse et
                if (jsonResponse.contains("\"content\":[")) {
                    int contentStart = jsonResponse.indexOf("\"content\":[") + 11;
                    int contentEnd = findMatchingBracket(jsonResponse, contentStart - 1);
                    String contentArray = jsonResponse.substring(contentStart, contentEnd);
                    
                    // Her bir payment point object'i parse et
                    int start = 0;
                    while (start < contentArray.length()) {
                        int objStart = contentArray.indexOf("{", start);
                        if (objStart == -1) break;
                        
                        int objEnd = findMatchingBrace(contentArray, objStart);
                        if (objEnd == -1) break;
                        
                        String paymentPointJson = contentArray.substring(objStart, objEnd + 1);
                        PaymentPoint pp = parsePaymentPoint(paymentPointJson);
                        if (pp != null) {
                            paymentPoints.add(pp);
                        }
                        
                        start = objEnd + 1;
                    }
                }
            }
            
            page.setContent(paymentPoints);
            System.out.println("Parsed " + paymentPoints.size() + " payment points");
            
            return page;
            
        } catch (Exception e) {
            System.err.println("Error parsing payment points response: " + e.getMessage());
            e.printStackTrace();
            return new PaymentPointPage();
        }
    }
    
    private PaymentPoint parsePaymentPoint(String json) {
        try {
            PaymentPoint pp = new PaymentPoint();
            
            // ID
            String idStr = extractJsonValue(json, "id");
            if (idStr != null) {
                pp.setId(Long.parseLong(idStr));
            }
            
            // Name
            String name = extractJsonValue(json, "name");
            if (name != null) {
                pp.setName(name.replace("\\\"", "\""));
            }
            
            // Location
            if (json.contains("\"location\":{")) {
                int locStart = json.indexOf("\"location\":{") + 12;
                int locEnd = findMatchingBrace(json, locStart - 1);
                String locationJson = json.substring(locStart, locEnd);
                
                Location location = new Location();
                String latStr = extractJsonValue(locationJson, "latitude");
                String lonStr = extractJsonValue(locationJson, "longitude");
                
                if (latStr != null && lonStr != null) {
                    try {
                        location.setLatitude(Double.parseDouble(latStr));
                        location.setLongitude(Double.parseDouble(lonStr));
                        pp.setLocation(location);
                    } catch (NumberFormatException e) {
                        System.err.println("Koordinat parse hatası: " + e.getMessage());
                    }
                }
            }
            
            // Address
            if (json.contains("\"address\":{")) {
                int addrStart = json.indexOf("\"address\":{") + 11;
                int addrEnd = findMatchingBrace(json, addrStart - 1);
                String addressJson = json.substring(addrStart, addrEnd);
                
                Address address = new Address();
                String street = extractJsonValue(addressJson, "street");
                String district = extractJsonValue(addressJson, "district");
                String city = extractJsonValue(addressJson, "city");
                String postalCode = extractJsonValue(addressJson, "postalCode");
                
                if (street != null) address.setStreet(street.replace("\\\"", "\""));
                if (district != null) address.setDistrict(district.replace("\\\"", "\""));
                if (city != null) address.setCity(city.replace("\\\"", "\""));
                if (postalCode != null) address.setPostalCode(postalCode.replace("\\\"", "\""));
                
                pp.setAddress(address);
            }
            
            // Contact Number
            String contactNumber = extractJsonValue(json, "contactNumber");
            if (contactNumber != null) {
                pp.setContactNumber(contactNumber.replace("\\\"", "\""));
            }
            
            // Working Hours
            String workingHours = extractJsonValue(json, "workingHours");
            if (workingHours != null) {
                pp.setWorkingHours(workingHours.replace("\\\"", "\""));
            }
            
            // Description
            String description = extractJsonValue(json, "description");
            if (description != null) {
                pp.setDescription(description.replace("\\\"", "\""));
            }
            
            // Active
            String activeStr = extractJsonValue(json, "active");
            if (activeStr != null) {
                pp.setActive(Boolean.parseBoolean(activeStr));
            }
            
            // Distance
            String distanceStr = extractJsonValue(json, "distance");
            if (distanceStr != null && !distanceStr.equals("null")) {
                pp.setDistance(Double.parseDouble(distanceStr));
            }
            
            // Payment Methods
            if (json.contains("\"paymentMethods\":[")) {
                int pmStart = json.indexOf("\"paymentMethods\":[") + 18;
                int pmEnd = json.indexOf("]", pmStart);
                String paymentMethodsJson = json.substring(pmStart, pmEnd);
                
                List<PaymentMethod> methods = new ArrayList<>();
                String[] methodStrings = paymentMethodsJson.split(",");
                for (String methodStr : methodStrings) {
                    methodStr = methodStr.trim().replace("\"", "");
                    if (!methodStr.isEmpty()) {
                        try {
                            PaymentMethod method = PaymentMethod.valueOf(methodStr);
                            methods.add(method);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Unknown payment method: " + methodStr);
                        }
                    }
                }
                pp.setPaymentMethods(methods);
            }
            
            // Photos (basit implementation)
            pp.setPhotos(new ArrayList<>());
            
            return pp;
            
        } catch (Exception e) {
            System.err.println("Error parsing payment point: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        
        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        if (start >= json.length()) return null;
        
        int end;
        if (json.charAt(start) == '"') {
            // String value
            start++; // Skip opening quote
            end = start;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') {
                    end++; // Skip escaped character
                }
                end++;
            }
            return json.substring(start, end);
        } else {
            // Number, boolean, or null
            end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
    
    private int findMatchingBrace(String json, int start) {
        int braceCount = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '{') {
                braceCount++;
            } else if (json.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private int findMatchingBracket(String json, int start) {
        int bracketCount = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '[') {
                bracketCount++;
            } else if (json.charAt(i) == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private void updateCityFilter() {
        Set<String> cities = new HashSet<>();
        for (PaymentPoint pp : paymentPointsData) {
            if (pp.getAddress() != null && pp.getAddress().getCity() != null) {
                cities.add(pp.getAddress().getCity());
            }
        }
        
        String selectedCity = cityFilter.getValue();
        cityFilter.getItems().clear();
        cityFilter.getItems().add("Tümü");
        cityFilter.getItems().addAll(cities.stream().sorted().toList());
        
        if (selectedCity != null && cityFilter.getItems().contains(selectedCity)) {
            cityFilter.setValue(selectedCity);
        }
    }
    
    private void clearFilters() {
        searchField.clear();
        cityFilter.setValue(null);
        paymentMethodFilter.setValue(null);
        activeFilter.setSelected(false);
        currentPage = 0;
        loadPaymentPoints();
    }
    
    private void showPaymentPointDetails(PaymentPoint paymentPoint) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Ödeme Noktası Detayları");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        // Detay alanları
        layout.getChildren().addAll(
            new Label("ID: " + paymentPoint.getId()),
            new Label("İsim: " + paymentPoint.getName()),
            new Label("Şehir: " + (paymentPoint.getAddress() != null ? paymentPoint.getAddress().getCity() : "")),
            new Label("İlçe: " + (paymentPoint.getAddress() != null ? paymentPoint.getAddress().getDistrict() : "")),
            new Label("Adres: " + (paymentPoint.getAddress() != null ? paymentPoint.getAddress().getStreet() : "")),
            new Label("Telefon: " + (paymentPoint.getContactNumber() != null ? paymentPoint.getContactNumber() : "")),
            new Label("Çalışma Saatleri: " + (paymentPoint.getWorkingHours() != null ? paymentPoint.getWorkingHours() : "")),
            new Label("Durum: " + (paymentPoint.isActive() ? "Aktif" : "Pasif")),
            new Label("Açıklama: " + (paymentPoint.getDescription() != null ? paymentPoint.getDescription() : ""))
        );
        
        if (paymentPoint.getLocation() != null) {
            layout.getChildren().add(new Label("Konum: " + 
                paymentPoint.getLocation().getLatitude() + ", " + 
                paymentPoint.getLocation().getLongitude()));
        }
        
        if (paymentPoint.getPaymentMethods() != null) {
            String methods = paymentPoint.getPaymentMethods().stream()
                .map(PaymentMethod::getDisplayName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
            layout.getChildren().add(new Label("Ödeme Yöntemleri: " + methods));
        }
        
        Button closeButton = new Button("Kapat");
        closeButton.setOnAction(e -> detailStage.close());
        layout.getChildren().add(closeButton);
        
        Scene scene = new Scene(layout, 400, 500);
        detailStage.setScene(scene);
        detailStage.showAndWait();
    }
    
    private void showAddPaymentPointDialog() {
        showPaymentPointDialog(null);
    }
    
    private void showEditPaymentPointDialog(PaymentPoint paymentPoint) {
        showPaymentPointDialog(paymentPoint);
    }
    
    private void showPaymentPointDialog(PaymentPoint paymentPoint) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(paymentPoint == null ? "Yeni Ödeme Noktası" : "Ödeme Noktası Düzenle");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        // Form alanları
        TextField nameField = new TextField(paymentPoint != null ? paymentPoint.getName() : "");
        nameField.setPromptText("İsim");
        
        TextField latitudeField = new TextField(paymentPoint != null && paymentPoint.getLocation() != null ? 
            paymentPoint.getLocation().getLatitude().toString() : "");
        latitudeField.setPromptText("Enlem");
        
        TextField longitudeField = new TextField(paymentPoint != null && paymentPoint.getLocation() != null ? 
            paymentPoint.getLocation().getLongitude().toString() : "");
        longitudeField.setPromptText("Boylam");
        
        TextField streetField = new TextField(paymentPoint != null && paymentPoint.getAddress() != null ? 
            paymentPoint.getAddress().getStreet() : "");
        streetField.setPromptText("Sokak");
        
        TextField districtField = new TextField(paymentPoint != null && paymentPoint.getAddress() != null ? 
            paymentPoint.getAddress().getDistrict() : "");
        districtField.setPromptText("İlçe");
        
        TextField cityField = new TextField(paymentPoint != null && paymentPoint.getAddress() != null ? 
            paymentPoint.getAddress().getCity() : "");
        cityField.setPromptText("Şehir");
        
        TextField postalCodeField = new TextField(paymentPoint != null && paymentPoint.getAddress() != null ? 
            paymentPoint.getAddress().getPostalCode() : "");
        postalCodeField.setPromptText("Posta Kodu");
        
        TextField contactField = new TextField(paymentPoint != null ? paymentPoint.getContactNumber() : "");
        contactField.setPromptText("İletişim");
        
        TextField workingHoursField = new TextField(paymentPoint != null ? paymentPoint.getWorkingHours() : "");
        workingHoursField.setPromptText("Çalışma Saatleri (örn: 09:00-18:00)");
        
        TextArea descriptionArea = new TextArea(paymentPoint != null ? paymentPoint.getDescription() : "");
        descriptionArea.setPromptText("Açıklama");
        descriptionArea.setPrefRowCount(3);
        
        // Ödeme yöntemleri seçimi
        VBox paymentMethodsBox = new VBox(5);
        paymentMethodsBox.getChildren().add(new Label("Ödeme Yöntemleri:"));
        
        List<CheckBox> paymentMethodCheckboxes = new ArrayList<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            CheckBox checkBox = new CheckBox(method.getDisplayName());
            if (paymentPoint != null && paymentPoint.getPaymentMethods() != null) {
                checkBox.setSelected(paymentPoint.getPaymentMethods().contains(method));
            }
            paymentMethodCheckboxes.add(checkBox);
            paymentMethodsBox.getChildren().add(checkBox);
        }
        
        CheckBox activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setSelected(paymentPoint == null || paymentPoint.isActive());
        
        // Butonlar
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button("Kaydet");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            // Validation
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Hata", "İsim alanı boş bırakılamaz!");
                return;
            }
            
            try {
                Double.parseDouble(latitudeField.getText());
                Double.parseDouble(longitudeField.getText());
            } catch (NumberFormatException ex) {
                showAlert("Hata", "Enlem ve boylam değerleri geçerli sayı olmalıdır!");
                return;
            }
            
            List<String> selectedMethods = new ArrayList<>();
            for (int i = 0; i < paymentMethodCheckboxes.size(); i++) {
                if (paymentMethodCheckboxes.get(i).isSelected()) {
                    selectedMethods.add(PaymentMethod.values()[i].name());
                }
            }
            
            if (selectedMethods.isEmpty()) {
                showAlert("Hata", "En az bir ödeme yöntemi seçilmelidir!");
                return;
            }
            
            savePaymentPoint(paymentPoint, nameField.getText(), 
                Double.parseDouble(latitudeField.getText()),
                Double.parseDouble(longitudeField.getText()),
                streetField.getText(), districtField.getText(), cityField.getText(),
                postalCodeField.getText(), contactField.getText(),
                workingHoursField.getText(), selectedMethods,
                descriptionArea.getText(), activeCheckBox.isSelected());
            
            dialogStage.close();
        });
        
        Button cancelButton = new Button("İptal");
        cancelButton.setOnAction(e -> dialogStage.close());
        
        buttons.getChildren().addAll(saveButton, cancelButton);
        
        // Layout'a ekle
        layout.getChildren().addAll(
            new Label("İsim:"), nameField,
            new Label("Enlem:"), latitudeField,
            new Label("Boylam:"), longitudeField,
            new Label("Sokak:"), streetField,
            new Label("İlçe:"), districtField,
            new Label("Şehir:"), cityField,
            new Label("Posta Kodu:"), postalCodeField,
            new Label("İletişim:"), contactField,
            new Label("Çalışma Saatleri:"), workingHoursField,
            new Label("Açıklama:"), descriptionArea,
            paymentMethodsBox,
            activeCheckBox,
            buttons
        );
        
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        
        Scene scene = new Scene(scrollPane, 500, 700);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private void savePaymentPoint(PaymentPoint existingPoint, String name, Double latitude, Double longitude,
                                String street, String district, String city, String postalCode,
                                String contactNumber, String workingHours, List<String> paymentMethods,
                                String description, boolean active) {
        
        CompletableFuture.supplyAsync(() -> {
            try {
                if (existingPoint == null) {
                    // Yeni ödeme noktası oluştur
                    return ApiClientFX.createPaymentPoint(accessToken, name, latitude, longitude,
                        street, district, city, postalCode, contactNumber, workingHours,
                        paymentMethods, description, active);
                } else {
                    // Mevcut ödeme noktasını güncelle
                    return ApiClientFX.updatePaymentPoint(accessToken, existingPoint.getId(), name, latitude, longitude,
                        street, district, city, postalCode, contactNumber, workingHours,
                        paymentMethods, description, active);
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert("Hata", "Ödeme noktası kaydedilirken hata oluştu: " + e.getMessage());
                });
                return null;
            }
        }).thenAccept(response -> {
            if (response != null) {
                Platform.runLater(() -> {
                    showAlert("Başarılı", existingPoint == null ? "Ödeme noktası başarıyla oluşturuldu!" : "Ödeme noktası başarıyla güncellendi!");
                    loadPaymentPoints();
                });
            }
        });
    }
    
    private void deletePaymentPoint(PaymentPoint paymentPoint) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Silme Onayı");
        alert.setHeaderText("Ödeme noktasını silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz!");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        ApiClientFX.deletePaymentPoint(accessToken, paymentPoint.getId());
                        return null;
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            showAlert("Hata", "Ödeme noktası silinirken hata oluştu: " + e.getMessage());
                        });
                        return null;
                    }
                }).thenAccept(responseStr -> {
                    if (responseStr != null) {
                        Platform.runLater(() -> {
                            showAlert("Başarılı", "Ödeme noktası başarıyla silindi!");
                            loadPaymentPoints();
                        });
                    }
                });
            }
        });
    }
    
    private void togglePaymentPointStatus(PaymentPoint paymentPoint) {
        boolean newStatus = !paymentPoint.isActive();
        String statusText = newStatus ? "aktif" : "pasif";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Durum Değiştirme");
        alert.setHeaderText("Ödeme noktasının durumunu " + statusText + " yapmak istediğinizden emin misiniz?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        ApiClientFX.togglePaymentPointStatus(accessToken, paymentPoint.getId(), newStatus);
                        return null;
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            showAlert("Hata", "Ödeme noktası durumu değiştirilirken hata oluştu: " + e.getMessage());
                        });
                        return null;
                    }
                }).thenAccept(responseStr -> {
                    if (responseStr != null) {
                        Platform.runLater(() -> {
                            showAlert("Başarılı", "Ödeme noktası durumu başarıyla değiştirildi!");
                            loadPaymentPoints();
                        });
                    }
                });
            }
        });
    }
    
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: red; -fx-font-size: 12px;" : "-fx-text-fill: #666; -fx-font-size: 12px;");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyFilters() {
        // Filtreleme mantığı - önce tüm veriyi yükle sonra filtrele
        currentPage = 0;
        
        // Filtreleme parametrelerini kontrol et
        String searchText = searchField.getText();
        String selectedCity = cityFilter.getValue();
        PaymentMethod selectedPaymentMethod = paymentMethodFilter.getValue();
        boolean onlyActive = activeFilter.isSelected();
        
        // Eğer herhangi bir filtre aktifse, client-side filtreleme yap
        if ((searchText != null && !searchText.trim().isEmpty()) || 
            (selectedCity != null && !selectedCity.equals("Tümü")) ||
            selectedPaymentMethod != null || onlyActive) {
            
            // Filtrelenmiş veriyi hazırla
            List<PaymentPoint> filteredData = new ArrayList<>();
            
            for (PaymentPoint pp : paymentPointsData) {
                boolean matches = true;
                
                // Arama metni kontrolü
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String lowerSearchText = searchText.toLowerCase();
                    matches = matches && (
                        pp.getName().toLowerCase().contains(lowerSearchText) ||
                        (pp.getAddress() != null && pp.getAddress().getCity() != null && 
                         pp.getAddress().getCity().toLowerCase().contains(lowerSearchText)) ||
                        (pp.getAddress() != null && pp.getAddress().getDistrict() != null && 
                         pp.getAddress().getDistrict().toLowerCase().contains(lowerSearchText))
                    );
                }
                
                // Şehir filtresi
                if (selectedCity != null && !selectedCity.equals("Tümü")) {
                    matches = matches && (pp.getAddress() != null && 
                        selectedCity.equals(pp.getAddress().getCity()));
                }
                
                // Ödeme yöntemi filtresi
                if (selectedPaymentMethod != null) {
                    matches = matches && (pp.getPaymentMethods() != null && 
                        pp.getPaymentMethods().contains(selectedPaymentMethod));
                }
                
                // Aktif durum filtresi
                if (onlyActive) {
                    matches = matches && pp.isActive();
                }
                
                if (matches) {
                    filteredData.add(pp);
                }
            }
            
            // Tabloya filtrelenmiş veriyi uygula
            paymentPointsData.clear();
            paymentPointsData.addAll(filteredData);
            
            setStatus("Filtrelenmiş: " + filteredData.size() + " ödeme noktası", false);
        } else {
            // Filtre yoksa normal yükleme yap
            loadPaymentPoints();
        }
    }
}
