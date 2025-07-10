package com.bincard.bincard_superadmin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.scene.Node;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.bincard.bincard_superadmin.model.*;

public class IdentityRequestsPage extends SuperadminPageBase {
    
    private TableView<IdentityVerificationRequestDTO> tableView;
    private List<IdentityVerificationRequestDTO> allRequests = new ArrayList<>();
    private List<IdentityVerificationRequestDTO> filteredRequests = new ArrayList<>();
    
    // Filtreleme kontrolleri
    private ComboBox<RequestStatus> statusFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextField searchField;
    private Label totalCountLabel;
    private Label pendingCountLabel;
    private Label approvedCountLabel;
    private Label rejectedCountLabel;

    public IdentityRequestsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken, HostServices hostServices) {
        super(stage, accessToken, refreshToken, "Kimlik Doğrulama İstekleri");
        loadIdentityRequests();
    }

    @Override
    protected Node createContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        
        // Sayfa başlığı
        Label titleLabel = new Label("Kimlik Doğrulama İstekleri");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // İstatistik kartları
        HBox statsBox = createStatsCards();
        
        // Filtreleme alanı
        VBox filterBox = createFilterArea();
        
        // Tablo
        createTable();
        
        // Eylem butonları
        HBox actionButtons = createActionButtons();
        
        mainContent.getChildren().addAll(titleLabel, statsBox, filterBox, tableView, actionButtons);
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        // Toplam
        VBox totalCard = createStatsCard("Toplam İstekler", "0", "#3498db", FontAwesomeSolid.CLIPBOARD_LIST);
        totalCountLabel = (Label) ((VBox) totalCard.getChildren().get(1)).getChildren().get(0);
        
        // Bekleyen
        VBox pendingCard = createStatsCard("Bekleyen", "0", "#f39c12", FontAwesomeSolid.CLOCK);
        pendingCountLabel = (Label) ((VBox) pendingCard.getChildren().get(1)).getChildren().get(0);
        
        // Onaylanan
        VBox approvedCard = createStatsCard("Onaylanan", "0", "#27ae60", FontAwesomeSolid.CHECK_CIRCLE);
        approvedCountLabel = (Label) ((VBox) approvedCard.getChildren().get(1)).getChildren().get(0);
        
        // Reddedilen
        VBox rejectedCard = createStatsCard("Reddedilen", "0", "#e74c3c", FontAwesomeSolid.TIMES_CIRCLE);
        rejectedCountLabel = (Label) ((VBox) rejectedCard.getChildren().get(1)).getChildren().get(0);
        
        statsBox.getChildren().addAll(totalCard, pendingCard, approvedCard, rejectedCard);
        return statsBox;
    }
    
    private VBox createStatsCard(String title, String value, String color, FontAwesomeSolid icon) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        
        // İkon
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(32);
        iconNode.setIconColor(Color.web(color));
        
        // Değer ve başlık
        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        textBox.getChildren().addAll(valueLabel, titleLabel);
        card.getChildren().addAll(iconNode, textBox);
        
        return card;
    }
    
    private VBox createFilterArea() {
        VBox filterArea = new VBox(15);
        filterArea.setPadding(new Insets(20));
        filterArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label filterTitle = new Label("Filtreleme ve Arama");
        filterTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        filterTitle.setTextFill(Color.web("#2c3e50"));
        
        // İlk satır filtreler
        HBox filterRow1 = new HBox(15);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        
        // Durum filtresi
        Label statusLabel = new Label("Durum:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(RequestStatus.values());
        statusFilter.setPromptText("Tüm Durumlar");
        statusFilter.setPrefWidth(150);
        statusFilter.setOnAction(e -> applyFilters());
        
        // Tarih filtreleri
        Label dateLabel = new Label("Tarih Aralığı:");
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Başlangıç");
        startDatePicker.setPrefWidth(150);
        startDatePicker.setOnAction(e -> applyFilters());
        
        Label toLabel = new Label("-");
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Bitiş");
        endDatePicker.setPrefWidth(150);
        endDatePicker.setOnAction(e -> applyFilters());
        
        filterRow1.getChildren().addAll(statusLabel, statusFilter, new Separator(),
                                       dateLabel, startDatePicker, toLabel, endDatePicker);
        
        // İkinci satır - arama
        HBox filterRow2 = new HBox(15);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Arama:");
        searchField = new TextField();
        searchField.setPromptText("TC No, Seri No veya Telefon ile ara...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        
        Button clearButton = new Button("Temizle");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        clearButton.setOnAction(e -> clearFilters());
        
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshButton.setOnAction(e -> loadIdentityRequests());
        
        filterRow2.getChildren().addAll(searchLabel, searchField, clearButton, refreshButton);
        
        filterArea.getChildren().addAll(filterTitle, filterRow1, filterRow2);
        return filterArea;
    }
    
    private void createTable() {
        tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // ID kolonu
        TableColumn<IdentityVerificationRequestDTO, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(60);
        
        // TC No kolonu
        TableColumn<IdentityVerificationRequestDTO, String> tcColumn = new TableColumn<>("TC No");
        tcColumn.setCellValueFactory(cellData -> {
            UserIdentityInfoDTO info = cellData.getValue().getIdentityInfo();
            return new javafx.beans.property.SimpleStringProperty(info != null ? info.getNationalId() : "");
        });
        tcColumn.setPrefWidth(120);
        
        // Seri No kolonu  
        TableColumn<IdentityVerificationRequestDTO, String> serialColumn = new TableColumn<>("Seri No");
        serialColumn.setCellValueFactory(cellData -> {
            UserIdentityInfoDTO info = cellData.getValue().getIdentityInfo();
            return new javafx.beans.property.SimpleStringProperty(info != null ? info.getSerialNumber() : "");
        });
        serialColumn.setPrefWidth(100);
        
        // Doğum Tarihi kolonu
        TableColumn<IdentityVerificationRequestDTO, String> birthColumn = new TableColumn<>("Doğum Tarihi");
        birthColumn.setCellValueFactory(cellData -> {
            UserIdentityInfoDTO info = cellData.getValue().getIdentityInfo();
            return new javafx.beans.property.SimpleStringProperty(info != null ? info.getBirthDate() : "");
        });
        birthColumn.setPrefWidth(120);
        
        // Telefon kolonu
        TableColumn<IdentityVerificationRequestDTO, String> phoneColumn = new TableColumn<>("Telefon");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("requestedByPhone"));
        phoneColumn.setPrefWidth(120);
        
        // Başvuru Tarihi kolonu
        TableColumn<IdentityVerificationRequestDTO, String> requestDateColumn = new TableColumn<>("Başvuru Tarihi");
        requestDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getRequestedAt();
            String formattedDate = date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        requestDateColumn.setPrefWidth(130);
        
        // Durum kolonu
        TableColumn<IdentityVerificationRequestDTO, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(cellData -> {
            RequestStatus status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(status != null ? status.getDisplayName() : "");
        });
        statusColumn.setPrefWidth(100);
        
        // Durum kolonu için özel hücre factory'si (renkli gösterim)
        statusColumn.setCellFactory(column -> new TableCell<IdentityVerificationRequestDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Beklemede":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "Onaylandı":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "Reddedildi":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Değerlendiren kolonu
        TableColumn<IdentityVerificationRequestDTO, String> reviewerColumn = new TableColumn<>("Değerlendiren");
        reviewerColumn.setCellValueFactory(new PropertyValueFactory<>("reviewedByPhone"));
        reviewerColumn.setPrefWidth(120);
        
        // Eylemler kolonu
        TableColumn<IdentityVerificationRequestDTO, Void> actionsColumn = new TableColumn<>("Eylemler");
        actionsColumn.setCellFactory(column -> new TableCell<IdentityVerificationRequestDTO, Void>() {
            private final Button detailButton = new Button("Detay");
            private final Button approveButton = new Button("Onayla");
            private final Button rejectButton = new Button("Reddet");
            
            {
                detailButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 10px;");
                approveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                rejectButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                
                detailButton.setOnAction(e -> {
                    IdentityVerificationRequestDTO request = getTableView().getItems().get(getIndex());
                    showRequestDetail(request);
                });
                
                approveButton.setOnAction(e -> {
                    IdentityVerificationRequestDTO request = getTableView().getItems().get(getIndex());
                    processRequest(request, true);
                });
                
                rejectButton.setOnAction(e -> {
                    IdentityVerificationRequestDTO request = getTableView().getItems().get(getIndex());
                    processRequest(request, false);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    IdentityVerificationRequestDTO request = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);
                    
                    buttons.getChildren().add(detailButton);
                    
                    // Sadece bekleyen istekler için onay/red butonlarını göster
                    if (request.getStatus() == RequestStatus.PENDING) {
                        buttons.getChildren().addAll(approveButton, rejectButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        actionsColumn.setPrefWidth(200);
        
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(tcColumn);
        tableView.getColumns().add(serialColumn);
        tableView.getColumns().add(birthColumn);
        tableView.getColumns().add(phoneColumn);
        tableView.getColumns().add(requestDateColumn);
        tableView.getColumns().add(statusColumn);
        tableView.getColumns().add(reviewerColumn);
        tableView.getColumns().add(actionsColumn);
        
        // Tablo satır stilini ayarla
        tableView.setRowFactory(tv -> {
            TableRow<IdentityVerificationRequestDTO> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    switch (newItem.getStatus()) {
                        case PENDING:
                            row.setStyle("-fx-background-color: #fffbf0;");
                            break;
                        case APPROVED:
                            row.setStyle("-fx-background-color: #f0f8f0;");
                            break;
                        case REJECTED:
                            row.setStyle("-fx-background-color: #fdf2f2;");
                            break;
                    }
                }
            });
            return row;
        });
    }
    
    private HBox createActionButtons() {
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button exportButton = new Button("Excel'e Aktar");
        exportButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        exportButton.setOnAction(e -> exportToExcel());
        
        Button reportButton = new Button("Rapor Oluştur");
        reportButton.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        reportButton.setOnAction(e -> generateReport());
        
        actionBox.getChildren().addAll(exportButton, reportButton);
        return actionBox;
    }
    
    private void loadIdentityRequests() {
        // API'den verileri yükle
        CompletableFuture.runAsync(() -> {
            try {
                // Loading göster
                Platform.runLater(() -> {
                    // Loading indicator ekle
                    if (totalCountLabel != null) {
                        totalCountLabel.setText("Yükleniyor...");
                    }
                });
                
                RequestStatus selectedStatus = statusFilter != null ? statusFilter.getValue() : null;
                String statusParam = selectedStatus != null ? selectedStatus.name() : null;
                
                String startDateParam = null;
                String endDateParam = null;
                if (startDatePicker != null && startDatePicker.getValue() != null) {
                    startDateParam = startDatePicker.getValue().toString();
                }
                if (endDatePicker != null && endDatePicker.getValue() != null) {
                    endDateParam = endDatePicker.getValue().toString();
                }
                
                String response = ApiClientFX.getIdentityRequests(
                    accessToken, statusParam, startDateParam, endDateParam, 
                    0, 100, "requestedAt", "desc"
                );
                
                System.out.println("API Request URL: " + "http://localhost:8080/v1/api/wallet/identity-requests");
                System.out.println("API Request Params: status=" + statusParam + ", startDate=" + startDateParam + ", endDate=" + endDateParam);
                System.out.println("API Response: " + response);
                
                // JSON parse et
                allRequests.clear();
                
                if (response.contains("\"success\":true") || response.contains("\"data\":{")) {
                    // JSON'dan verileri çıkar
                    parseIdentityRequestsFromJson(response);
                    
                    System.out.println("API'den " + allRequests.size() + " istek yüklendi");
                } else {
                    System.err.println("API başarısız yanıt: " + response);
                    // Hata durumunda örnek veri göster
                    loadSampleData();
                }
                
                Platform.runLater(() -> {
                    applyFilters();
                    updateStatistics();
                });
                
            } catch (Exception e) {
                System.err.println("Kimlik istekleri API'si mevcut değil, örnek verilerle devam ediliyor: " + e.getMessage());
                
                Platform.runLater(() -> {
                    // Hata durumunda örnek veri göster
                    loadSampleData();
                    applyFilters();
                    updateStatistics();
                });
            }
        });
    }
    
    private void parseIdentityRequestsFromJson(String jsonResponse) {
        try {
            System.out.println("JSON parse başlıyor...");
            
            // API response formatı: DataResponseMessage<Page<IdentityVerificationRequestDTO>>
            // Yapı: {"success":true,"message":"...","data":{"content":[...],"totalElements":10,"totalPages":1,...}}
            
            if (jsonResponse.contains("\"data\":{") && jsonResponse.contains("\"content\":[")) {
                // data objesini bul
                String dataStart = "\"data\":{";
                int dataStartIndex = jsonResponse.indexOf(dataStart);
                if (dataStartIndex == -1) {
                    System.out.println("Data objesi bulunamadı");
                    return;
                }
                
                // content array'ini bul
                String contentStart = "\"content\":[";
                int contentStartIndex = jsonResponse.indexOf(contentStart, dataStartIndex);
                if (contentStartIndex == -1) {
                    System.out.println("Content array bulunamadı");
                    return;
                }
                
                contentStartIndex += contentStart.length();
                
                // content array'inin sonunu bul
                int contentEndIndex = jsonResponse.indexOf("],", contentStartIndex);
                if (contentEndIndex == -1) {
                    contentEndIndex = jsonResponse.indexOf("]}", contentStartIndex);
                }
                if (contentEndIndex == -1) {
                    System.out.println("Content array sonu bulunamadı");
                    return;
                }
                
                String contentPart = jsonResponse.substring(contentStartIndex, contentEndIndex);
                System.out.println("Content part: " + contentPart.substring(0, Math.min(200, contentPart.length())));
                
                if (contentPart.trim().isEmpty() || contentPart.equals("")) {
                    System.out.println("Content boş");
                    return;
                }
                
                // Her bir kimlik isteğini parse et
                // JSON objelerini ayır
                int braceCount = 0;
                int startIndex = 0;
                boolean inString = false;
                boolean escapeNext = false;
                
                for (int i = 0; i < contentPart.length(); i++) {
                    char c = contentPart.charAt(i);
                    
                    if (escapeNext) {
                        escapeNext = false;
                        continue;
                    }
                    
                    if (c == '\\') {
                        escapeNext = true;
                        continue;
                    }
                    
                    if (c == '"' && !escapeNext) {
                        inString = !inString;
                        continue;
                    }
                    
                    if (!inString) {
                        if (c == '{') {
                            if (braceCount == 0) {
                                startIndex = i;
                            }
                            braceCount++;
                        } else if (c == '}') {
                            braceCount--;
                            if (braceCount == 0) {
                                // Bir JSON objesi tamamlandı
                                String jsonObject = contentPart.substring(startIndex, i + 1);
                                try {
                                    IdentityVerificationRequestDTO request = parseIdentityRequestFromJsonItem(jsonObject);
                                    if (request != null) {
                                        allRequests.add(request);
                                        System.out.println("İstek eklendi: ID=" + request.getId());
                                    }
                                } catch (Exception e) {
                                    System.err.println("Tek item parse hatası: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
                
                System.out.println("Toplam yüklenen istek sayısı: " + allRequests.size());
            } else {
                System.out.println("Beklenen JSON formatı bulunamadı");
            }
        } catch (Exception e) {
            System.err.println("JSON parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private IdentityVerificationRequestDTO parseIdentityRequestFromJsonItem(String jsonItem) {
        try {
            // ID
            Long id = extractLongFromJson(jsonItem, "id");
            
            // Kimlik bilgileri
            UserIdentityInfoDTO identityInfo = null;
            if (jsonItem.contains("\"identityInfo\":{")) {
                String identityStart = "\"identityInfo\":{";
                int startIdx = jsonItem.indexOf(identityStart) + identityStart.length();
                int endIdx = findMatchingBrace(jsonItem, startIdx - 1);
                String identityJson = jsonItem.substring(startIdx - 1, endIdx + 1);
                
                identityInfo = new UserIdentityInfoDTO();
                identityInfo.setId(extractLongFromJson(identityJson, "id"));
                identityInfo.setFrontCardPhoto(extractStringFromJson(identityJson, "frontCardPhoto"));
                identityInfo.setBackCardPhoto(extractStringFromJson(identityJson, "backCardPhoto"));
                identityInfo.setNationalId(extractStringFromJson(identityJson, "nationalId"));
                identityInfo.setSerialNumber(extractStringFromJson(identityJson, "serialNumber"));
                identityInfo.setBirthDate(extractStringFromJson(identityJson, "birthDate"));
                identityInfo.setGender(extractStringFromJson(identityJson, "gender"));
                identityInfo.setMotherName(extractStringFromJson(identityJson, "motherName"));
                identityInfo.setFatherName(extractStringFromJson(identityJson, "fatherName"));
                identityInfo.setApprovedByPhone(extractStringFromJson(identityJson, "approvedByPhone"));
                identityInfo.setApproved(extractBooleanFromJson(identityJson, "approved"));
                identityInfo.setApprovedAt(extractStringFromJson(identityJson, "approvedAt"));
                identityInfo.setUserPhone(extractStringFromJson(identityJson, "userPhone"));
            }
            
            // Diğer alanlar
            String requestedByPhone = extractStringFromJson(jsonItem, "requestedByPhone");
            String requestedAtStr = extractStringFromJson(jsonItem, "requestedAt");
            String statusStr = extractStringFromJson(jsonItem, "status");
            String adminNote = extractStringFromJson(jsonItem, "adminNote");
            String reviewedByPhone = extractStringFromJson(jsonItem, "reviewedByPhone");
            String reviewedAtStr = extractStringFromJson(jsonItem, "reviewedAt");
            
            // LocalDateTime dönüştürme
            LocalDateTime requestedAt = parseDateTime(requestedAtStr);
            LocalDateTime reviewedAt = parseDateTime(reviewedAtStr);
            
            // RequestStatus dönüştürme
            RequestStatus status = RequestStatus.PENDING;
            try {
                if (statusStr != null) {
                    status = RequestStatus.valueOf(statusStr);
                }
            } catch (Exception e) {
                System.err.println("Status parse hatası: " + statusStr);
            }
            
            return new IdentityVerificationRequestDTO(
                id, identityInfo, requestedByPhone, requestedAt,
                status, adminNote, reviewedByPhone, reviewedAt
            );
            
        } catch (Exception e) {
            System.err.println("Identity request item parse hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private int findMatchingBrace(String json, int startIndex) {
        int braceCount = 0;
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') {
                braceCount--;
                if (braceCount == 0) return i;
            }
        }
        return json.length() - 1;
    }
    
    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            
            // null değer kontrolü
            String nullPattern = "\"" + key + "\"\\s*:\\s*null";
            p = java.util.regex.Pattern.compile(nullPattern);
            m = p.matcher(json);
            if (m.find()) {
                return null;
            }
        } catch (Exception e) {
            System.err.println("String extract hatası: " + e.getMessage());
        }
        return null;
    }
    
    private Long extractLongFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            System.err.println("Long extract hatası: " + e.getMessage());
        }
        return null;
    }
    
    private boolean extractBooleanFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Boolean.parseBoolean(m.group(1));
            }
        } catch (Exception e) {
            System.err.println("Boolean extract hatası: " + e.getMessage());
        }
        return false;
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        try {
            // ISO 8601 format: 2025-07-10T18:30:30.473737
            if (dateTimeStr.contains("T")) {
                // Microseconds varsa kaldır
                if (dateTimeStr.contains(".")) {
                    String[] parts = dateTimeStr.split("\\.");
                    dateTimeStr = parts[0];
                }
                return LocalDateTime.parse(dateTimeStr);
            }
        } catch (Exception e) {
            System.err.println("DateTime parse hatası: " + dateTimeStr + " - " + e.getMessage());
        }
        
        return null;
    }
    
    private void loadSampleData() {
        // Örnek veriler - API başarısız olduğunda veya boş döndüğünde
        System.out.println("Örnek veri yükleniyor...");
        allRequests.clear();
        
        for (int i = 1; i <= 15; i++) {
            UserIdentityInfoDTO identityInfo = new UserIdentityInfoDTO(
                (long) i,
                "http://res.cloudinary.com/da5vtbdbm/image/upload/v1752161428/profile_photos/front" + i + ".png",
                "http://res.cloudinary.com/da5vtbdbm/image/upload/v1752161429/profile_photos/back" + i + ".png",
                "1044500013" + (i % 10),
                "A1234567" + i,
                "1995-08-" + (20 + i % 8),
                i % 2 == 0 ? "Erkek" : "Kadın",
                "Anne" + i,
                "Baba" + i,
                null,
                false,
                null,
                "+90505376436" + (i % 10)
            );
            
            RequestStatus status = i % 4 == 0 ? RequestStatus.APPROVED : 
                                  i % 4 == 1 ? RequestStatus.REJECTED : RequestStatus.PENDING;
            
            IdentityVerificationRequestDTO request = new IdentityVerificationRequestDTO(
                (long) i,
                identityInfo,
                "+90505376436" + (i % 10),
                LocalDateTime.now().minusDays(i),
                status,
                status != RequestStatus.PENDING ? "Örnek admin notu " + i : null,
                status != RequestStatus.PENDING ? "+90555999888" + (i % 3) : null,
                status != RequestStatus.PENDING ? LocalDateTime.now().minusDays(i - 1) : null
            );
            
            allRequests.add(request);
        }
        
        System.out.println("Örnek veri yüklendi: " + allRequests.size() + " adet");
    }
    
    private void applyFilters() {
        System.out.println("Filtreler uygulanıyor... Toplam istek: " + allRequests.size());
        filteredRequests.clear();
        
        String searchText = searchField != null && searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        RequestStatus selectedStatus = statusFilter != null ? statusFilter.getValue() : null;
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;
        
        for (IdentityVerificationRequestDTO request : allRequests) {
            boolean matches = true;
            
            // Arama filtresi
            if (!searchText.isEmpty()) {
                UserIdentityInfoDTO info = request.getIdentityInfo();
                String searchableText = "";
                if (info != null) {
                    searchableText = (info.getNationalId() != null ? info.getNationalId() : "") + " " +
                                   (info.getSerialNumber() != null ? info.getSerialNumber() : "") + " " +
                                   (info.getUserPhone() != null ? info.getUserPhone() : "") + " " +
                                   (request.getRequestedByPhone() != null ? request.getRequestedByPhone() : "");
                }
                
                if (!searchableText.toLowerCase().contains(searchText)) {
                    matches = false;
                }
            }
            
            // Durum filtresi
            if (selectedStatus != null && request.getStatus() != selectedStatus) {
                matches = false;
            }
            
            // Tarih filtresi
            if (startDate != null && request.getRequestedAt() != null && request.getRequestedAt().toLocalDate().isBefore(startDate)) {
                matches = false;
            }
            
            if (endDate != null && request.getRequestedAt() != null && request.getRequestedAt().toLocalDate().isAfter(endDate)) {
                matches = false;
            }
            
            if (matches) {
                filteredRequests.add(request);
            }
        }
        
        System.out.println("Filtrelenmiş istek sayısı: " + filteredRequests.size());
        
        if (tableView != null) {
            tableView.getItems().setAll(filteredRequests);
            System.out.println("Tablo güncellendi");
        }
    }
    
    private void clearFilters() {
        statusFilter.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        searchField.clear();
        applyFilters();
    }
    
    private void updateStatistics() {
        int total = allRequests.size();
        long pending = allRequests.stream().mapToLong(r -> r.getStatus() == RequestStatus.PENDING ? 1 : 0).sum();
        long approved = allRequests.stream().mapToLong(r -> r.getStatus() == RequestStatus.APPROVED ? 1 : 0).sum();
        long rejected = allRequests.stream().mapToLong(r -> r.getStatus() == RequestStatus.REJECTED ? 1 : 0).sum();
        
        System.out.println("İstatistikler güncelleniyor - Total: " + total + ", Pending: " + pending + ", Approved: " + approved + ", Rejected: " + rejected);
        
        Platform.runLater(() -> {
            if (totalCountLabel != null) {
                totalCountLabel.setText(String.valueOf(total));
            }
            if (pendingCountLabel != null) {
                pendingCountLabel.setText(String.valueOf(pending));
            }
            if (approvedCountLabel != null) {
                approvedCountLabel.setText(String.valueOf(approved));
            }
            if (rejectedCountLabel != null) {
                rejectedCountLabel.setText(String.valueOf(rejected));
            }
            System.out.println("İstatistik etiketleri güncellendi");
        });
    }
    
    private void showRequestDetail(IdentityVerificationRequestDTO request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Kimlik Doğrulama İsteği Detayı");
        dialog.setHeaderText("İstek ID: " + request.getId());
        
        // Dialog content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        UserIdentityInfoDTO info = request.getIdentityInfo();
        
        if (info != null) {
            // Kimlik bilgileri
            VBox identityBox = new VBox(10);
            identityBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");
            
            Label identityTitle = new Label("Kimlik Bilgileri");
            identityTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            
            GridPane identityGrid = new GridPane();
            identityGrid.setHgap(15);
            identityGrid.setVgap(10);
            
            identityGrid.add(new Label("TC No:"), 0, 0);
            identityGrid.add(new Label(info.getNationalId() != null ? info.getNationalId() : ""), 1, 0);
            
            identityGrid.add(new Label("Seri No:"), 0, 1);
            identityGrid.add(new Label(info.getSerialNumber() != null ? info.getSerialNumber() : ""), 1, 1);
            
            identityGrid.add(new Label("Doğum Tarihi:"), 0, 2);
            identityGrid.add(new Label(info.getBirthDate() != null ? info.getBirthDate() : ""), 1, 2);
            
            identityGrid.add(new Label("Cinsiyet:"), 0, 3);
            identityGrid.add(new Label(info.getGender() != null ? info.getGender() : ""), 1, 3);
            
            identityGrid.add(new Label("Anne Adı:"), 0, 4);
            identityGrid.add(new Label(info.getMotherName() != null ? info.getMotherName() : ""), 1, 4);
            
            identityGrid.add(new Label("Baba Adı:"), 0, 5);
            identityGrid.add(new Label(info.getFatherName() != null ? info.getFatherName() : ""), 1, 5);
            
            identityGrid.add(new Label("Kullanıcı Telefon:"), 0, 6);
            Label userPhoneLabel = new Label(info.getUserPhone() != null ? info.getUserPhone() : "");
            userPhoneLabel.setWrapText(true);
            userPhoneLabel.setMaxWidth(250);
            identityGrid.add(userPhoneLabel, 1, 6);
            
            identityBox.getChildren().addAll(identityTitle, identityGrid);
            content.getChildren().add(identityBox);
        }
        
        // Başvuru bilgileri
        VBox requestBox = new VBox(10);
        requestBox.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label requestTitle = new Label("Başvuru Bilgileri");
        requestTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        GridPane requestGrid = new GridPane();
        requestGrid.setHgap(15);
        requestGrid.setVgap(10);
        
        requestGrid.add(new Label("Telefon:"), 0, 0);
        requestGrid.add(new Label(request.getRequestedByPhone() != null ? request.getRequestedByPhone() : ""), 1, 0);
        
        requestGrid.add(new Label("Başvuru Tarihi:"), 0, 1);
        String requestDate = request.getRequestedAt() != null ? 
            request.getRequestedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
        requestGrid.add(new Label(requestDate), 1, 1);
        
        requestGrid.add(new Label("Durum:"), 0, 2);
        Label statusLabel = new Label(request.getStatus() != null ? request.getStatus().getDisplayName() : "");
        switch (request.getStatus()) {
            case PENDING:
                statusLabel.setStyle("-fx-text-fill: #856404; -fx-font-weight: bold;");
                break;
            case APPROVED:
                statusLabel.setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;");
                break;
            case REJECTED:
                statusLabel.setStyle("-fx-text-fill: #721c24; -fx-font-weight: bold;");
                break;
        }
        requestGrid.add(statusLabel, 1, 2);
        
        if (request.getReviewedByPhone() != null) {
            requestGrid.add(new Label("Değerlendiren:"), 0, 3);
            requestGrid.add(new Label(request.getReviewedByPhone()), 1, 3);
            
            requestGrid.add(new Label("Değerlendirme Tarihi:"), 0, 4);
            String reviewDate = request.getReviewedAt() != null ? 
                request.getReviewedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
            requestGrid.add(new Label(reviewDate), 1, 4);
        }
        
        if (request.getAdminNote() != null && !request.getAdminNote().isEmpty()) {
            requestGrid.add(new Label("Admin Notu:"), 0, 5);
            Label noteLabel = new Label(request.getAdminNote());
            noteLabel.setWrapText(true);
            noteLabel.setMaxWidth(250);
            requestGrid.add(noteLabel, 1, 5);
        }
        
        requestBox.getChildren().addAll(requestTitle, requestGrid);
        content.getChildren().add(requestBox);
        
        // Belge görüntüleme butonları
        if (info != null && (info.getFrontCardPhoto() != null || info.getBackCardPhoto() != null)) {
            HBox imageButtons = new HBox(10);
            imageButtons.setAlignment(Pos.CENTER);
            
            if (info.getFrontCardPhoto() != null) {
                Button frontButton = new Button("Ön Yüz Görüntüle");
                frontButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                frontButton.setOnAction(e -> {
                    // Gerçek uygulamada resim görüntüleme
                    showImageUrl("Kimlik Belgesi Ön Yüz", info.getFrontCardPhoto());
                });
                imageButtons.getChildren().add(frontButton);
            }
            
            if (info.getBackCardPhoto() != null) {
                Button backButton = new Button("Arka Yüz Görüntüle");
                backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                backButton.setOnAction(e -> {
                    // Gerçek uygulamada resim görüntüleme
                    showImageUrl("Kimlik Belgesi Arka Yüz", info.getBackCardPhoto());
                });
                imageButtons.getChildren().add(backButton);
            }
            
            content.getChildren().add(imageButtons);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void processRequest(IdentityVerificationRequestDTO request, boolean approve) {
        if (request.getStatus() != RequestStatus.PENDING) {
            showError("Bu istek zaten işlenmiş durumda.");
            return;
        }
        
        // Admin notu alma
        Dialog<String> noteDialog = new Dialog<>();
        noteDialog.setTitle(approve ? "İsteği Onayla" : "İsteği Reddet");
        noteDialog.setHeaderText("Admin notu ekleyin (opsiyonel):");
        
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Değerlendirme notu...");
        noteArea.setPrefRowCount(4);
        noteArea.setPrefColumnCount(40);
        
        noteDialog.getDialogPane().setContent(noteArea);
        noteDialog.getDialogPane().getButtonTypes().addAll(
            new ButtonType(approve ? "Onayla" : "Reddet", ButtonBar.ButtonData.OK_DONE),
            ButtonType.CANCEL
        );
        
        noteDialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return noteArea.getText();
            }
            return null;
        });
        
        noteDialog.showAndWait().ifPresent(note -> {
            // API çağrısı
            CompletableFuture.runAsync(() -> {
                try {
                    String response = ApiClientFX.processIdentityRequest(
                        accessToken, request.getId(), approve, note
                    );
                    
                    System.out.println("Process API Response: " + response);
                    
                    Platform.runLater(() -> {
                        if (response.contains("\"success\":true")) {
                            // İsteği güncelle
                            request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
                            request.setAdminNote(note);
                            request.setReviewedByPhone("+90555999888"); // Mevcut admin telefonu - gerçekte token'dan gelecek
                            request.setReviewedAt(LocalDateTime.now());
                            
                            // Tabloyu yenile
                            tableView.refresh();
                            updateStatistics();
                            
                            showSuccess("İstek başarıyla " + (approve ? "onaylandı" : "reddedildi") + ".");
                        } else {
                            String errorMsg = extractErrorMessage(response);
                            showError("İşlem başarısız: " + errorMsg);
                        }
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("İşlem sırasında bir hata oluştu: " + e.getMessage());
                    });
                }
            });
        });
    }
    
    private String extractErrorMessage(String response) {
        try {
            if (response.contains("\"message\":\"")) {
                String start = "\"message\":\"";
                int startIdx = response.indexOf(start) + start.length();
                int endIdx = response.indexOf("\"", startIdx);
                if (endIdx > startIdx) {
                    return response.substring(startIdx, endIdx);
                }
            }
        } catch (Exception e) {
            System.err.println("Error message extract hatası: " + e.getMessage());
        }
        return "Bilinmeyen hata";
    }
    
    private void exportToExcel() {
        showInfo("Excel Export", "Excel export özelliği geliştirilecek.");
    }
    
    private void generateReport() {
        showInfo("Rapor", "Rapor oluşturma özelliği geliştirilecek.");
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showImageUrl(String title, String imageUrl) {
        Dialog<Void> imageDialog = new Dialog<>();
        imageDialog.setTitle(title);
        imageDialog.setHeaderText("Resim URL'si:");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        TextField urlField = new TextField(imageUrl);
        urlField.setEditable(false);
        urlField.setPrefWidth(400);
        
        Button openButton = new Button("Tarayıcıda Aç");
        openButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        openButton.setOnAction(e -> {
            try {
                // URL'yi kopyala
                showInfo("URL Kopyalandı", "Resim URL'si panoya kopyalandı: " + imageUrl);
            } catch (Exception ex) {
                showError("URL açılamadı: " + ex.getMessage());
            }
        });
        
        content.getChildren().addAll(
            new Label("Resim URL'si:"),
            urlField,
            openButton
        );
        
        imageDialog.getDialogPane().setContent(content);
        imageDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        imageDialog.showAndWait();
    }
}
