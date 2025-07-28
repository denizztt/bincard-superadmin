package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Transfer işlemleri görüntüleme ve Excel indirme sayfası
 */
public class WalletTransfersPage extends SuperadminPageBase {
    
    private TableView<TransferInfo> transfersTable;
    private List<TransferInfo> transfersList = new ArrayList<>();
    private Label totalTransfersLabel;
    private Label totalAmountLabel;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    
    public static class TransferInfo {
        private Long id;
        private String senderWiban;
        private String receiverWiban;
        private BigDecimal amount;
        private String currency;
        private String status;
        private LocalDateTime timestamp;
        private String description;
        
        public TransferInfo() {}
        
        // Getters
        public Long getId() { return id; }
        public String getSenderWiban() { return senderWiban; }
        public String getReceiverWiban() { return receiverWiban; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getDescription() { return description; }
        
        public String getFormattedAmount() {
            return String.format("%.2f %s", amount, currency != null ? currency : "TRY");
        }
        
        public String getFormattedTimestamp() {
            if (timestamp == null) return "N/A";
            return timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        
        public String getSenderWibanShort() {
            if (senderWiban == null) return "N/A";
            return senderWiban.length() > 15 ? senderWiban.substring(0, 15) + "..." : senderWiban;
        }
        
        public String getReceiverWibanShort() {
            if (receiverWiban == null) return "N/A";
            return receiverWiban.length() > 15 ? receiverWiban.substring(0, 15) + "..." : receiverWiban;
        }
        
        public String getDescriptionShort() {
            if (description == null) return "";
            return description.length() > 30 ? description.substring(0, 30) + "..." : description;
        }
        
        // Setters
        public void setId(Long id) { this.id = id; }
        public void setSenderWiban(String senderWiban) { this.senderWiban = senderWiban; }
        public void setReceiverWiban(String receiverWiban) { this.receiverWiban = receiverWiban; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public void setCurrency(String currency) { this.currency = currency; }
        public void setStatus(String status) { this.status = status; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setDescription(String description) { this.description = description; }
    }

    public WalletTransfersPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Transfer İşlemleri");
        this.transfersList = new ArrayList<>();
    }

    @Override
    protected Node createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        HBox titleContainer = createTitleContainer();
        
        // İstatistikler
        HBox statsContainer = createStatsContainer();
        
        // Arama ve filtreler
        VBox filtersContainer = createFiltersContainer();
        
        // Tablo
        VBox tableContainer = createTableContainer();
        
        // Butonlar
        HBox buttonsContainer = createButtonsContainer();

        mainContainer.getChildren().addAll(
            titleContainer, 
            statsContainer, 
            filtersContainer, 
            tableContainer,
            buttonsContainer
        );
        
        // Transfer verilerini API'den yükle
        loadTransfersFromApi();
        
        return mainContainer;
    }
    
    private HBox createTitleContainer() {
        HBox titleContainer = new HBox(15);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon titleIcon = new FontIcon(FontAwesomeSolid.EXCHANGE_ALT);
        titleIcon.setIconSize(28);
        titleIcon.setIconColor(Color.web("#2c3e50"));
        
        Label titleLabel = new Label("Transfer İşlemleri");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        titleContainer.getChildren().addAll(titleIcon, titleLabel);
        return titleContainer;
    }
    
    private HBox createStatsContainer() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        
        // Toplam transfer sayısı
        VBox totalTransfersCard = createStatCard("Toplam Transfer", "0", "#e74c3c");
        totalTransfersLabel = (Label) ((VBox) totalTransfersCard.getChildren().get(0)).getChildren().get(1);
        
        // Toplam tutar
        VBox totalAmountCard = createStatCard("Toplam Tutar", "0.00 TRY", "#f39c12");
        totalAmountLabel = (Label) ((VBox) totalAmountCard.getChildren().get(0)).getChildren().get(1);
        
        statsContainer.getChildren().addAll(totalTransfersCard, totalAmountCard);
        return statsContainer;
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 100);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
            "-fx-padding: 20;"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(titleLabel, valueLabel);
        
        card.getChildren().add(content);
        return card;
    }
    
    private VBox createFiltersContainer() {
        VBox filtersContainer = new VBox(15);
        filtersContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
            "-fx-padding: 20;"
        );
        
        Label filtersTitle = new Label("Filtreler");
        filtersTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        filtersTitle.setTextFill(Color.web("#2c3e50"));
        
        // İlk satır - Arama ve durum
        HBox firstRow = new HBox(15);
        firstRow.setAlignment(Pos.CENTER_LEFT);
        
        // Arama alanı
        searchField = new TextField();
        searchField.setPromptText("Transfer ID veya WIBAN ile ara...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-font-size: 14px;");
        
        // Durum filtresi
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "SUCCESS", "PENDING", "FAILED", "CANCELLED");
        statusFilter.setValue("Tümü");
        statusFilter.setPrefWidth(120);
        
        // Arama butonu
        Button searchButton = new Button("Ara");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchButton.setOnAction(e -> filterTransfers());
        
        firstRow.getChildren().addAll(
            new Label("Arama:"), searchField,
            new Label("Durum:"), statusFilter,
            searchButton
        );
        
        // İkinci satır - Tarih aralığı
        HBox secondRow = new HBox(15);
        secondRow.setAlignment(Pos.CENTER_LEFT);
        
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Başlangıç Tarihi");
        startDatePicker.setPrefWidth(150);
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Bitiş Tarihi");
        endDatePicker.setPrefWidth(150);
        
        Button dateFilterButton = new Button("Tarihe Göre Filtrele");
        dateFilterButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        dateFilterButton.setOnAction(e -> filterByDateRange());
        
        Button clearFiltersButton = new Button("Filtreleri Temizle");
        clearFiltersButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        clearFiltersButton.setOnAction(e -> clearFilters());
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        secondRow.getChildren().addAll(
            new Label("Tarih Aralığı:"), startDatePicker,
            new Label("-"), endDatePicker,
            dateFilterButton, spacer, clearFiltersButton
        );
        
        filtersContainer.getChildren().addAll(filtersTitle, firstRow, secondRow);
        return filtersContainer;
    }
    
    private VBox createTableContainer() {
        VBox tableContainer = new VBox(10);
        
        transfersTable = new TableView<>();
        transfersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transfersTable.setStyle("-fx-font-size: 13px;");
        transfersTable.setPrefHeight(400);
        
        // Sütunlar
        TableColumn<TransferInfo, Long> idColumn = new TableColumn<>("Transfer ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);
        
        TableColumn<TransferInfo, String> senderColumn = new TableColumn<>("Gönderen WIBAN");
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("senderWibanShort"));
        senderColumn.setPrefWidth(150);
        
        TableColumn<TransferInfo, String> receiverColumn = new TableColumn<>("Alıcı WIBAN");
        receiverColumn.setCellValueFactory(new PropertyValueFactory<>("receiverWibanShort"));
        receiverColumn.setPrefWidth(150);
        
        TableColumn<TransferInfo, String> amountColumn = new TableColumn<>("Tutar");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        amountColumn.setPrefWidth(100);
        
        TableColumn<TransferInfo, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(100);
        
        TableColumn<TransferInfo, String> timestampColumn = new TableColumn<>("Tarih/Saat");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        timestampColumn.setPrefWidth(120);
        
        TableColumn<TransferInfo, String> descriptionColumn = new TableColumn<>("Açıklama");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descriptionShort"));
        descriptionColumn.setPrefWidth(200);
        
        transfersTable.getColumns().add(idColumn);
        transfersTable.getColumns().add(senderColumn);
        transfersTable.getColumns().add(receiverColumn);
        transfersTable.getColumns().add(amountColumn);
        transfersTable.getColumns().add(statusColumn);
        transfersTable.getColumns().add(timestampColumn);
        transfersTable.getColumns().add(descriptionColumn);
        
        VBox.setVgrow(transfersTable, Priority.ALWAYS);
        tableContainer.getChildren().add(transfersTable);
        
        return tableContainer;
    }
    
    private HBox createButtonsContainer() {
        HBox buttonsContainer = new HBox(15);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(20, 0, 0, 0));
        
        Button excelButton = new Button("Excel Olarak İndir");
        excelButton.setPrefWidth(200);
        excelButton.setPrefHeight(40);
        excelButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        excelButton.setStyle(
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        
        FontIcon excelIcon = new FontIcon(FontAwesomeSolid.FILE_EXCEL);
        excelIcon.setIconSize(16);
        excelIcon.setIconColor(Color.WHITE);
        excelButton.setGraphic(excelIcon);
        
        excelButton.setOnAction(e -> downloadExcel());
        
        Button refreshButton = new Button("Yenile");
        refreshButton.setPrefWidth(120);
        refreshButton.setPrefHeight(40);
        refreshButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        refreshButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC_ALT);
        refreshIcon.setIconSize(16);
        refreshIcon.setIconColor(Color.WHITE);
        refreshButton.setGraphic(refreshIcon);
        
        refreshButton.setOnAction(e -> loadTransfersFromApi());
        
        buttonsContainer.getChildren().addAll(excelButton, refreshButton);
        return buttonsContainer;
    }
    
    private void loadTransfersFromApi() {
        // API'den transfer verilerini yükle
        CompletableFuture.runAsync(() -> {
            try {
                // Loading göster
                Platform.runLater(() -> {
                    if (totalTransfersLabel != null) {
                        totalTransfersLabel.setText("Yükleniyor...");
                    }
                    if (totalAmountLabel != null) {
                        totalAmountLabel.setText("Yükleniyor...");
                    }
                });
                
                String statusParam = statusFilter != null ? statusFilter.getValue() : null;
                String startDateParam = null;
                String endDateParam = null;
                if (startDatePicker != null && startDatePicker.getValue() != null) {
                    startDateParam = startDatePicker.getValue().toString();
                }
                if (endDatePicker != null && endDatePicker.getValue() != null) {
                    endDateParam = endDatePicker.getValue().toString();
                }
                
                String response = WalletApiClient.getWalletTransfers(
                    accessToken, statusParam, startDateParam, endDateParam, 
                    0, 100, "timestamp", "desc"
                );
                
                System.out.println("Transfer API Request URL: " + "http://localhost:8080/v1/api/wallet/transfers");
                System.out.println("Transfer API Request Params: status=" + statusParam + ", startDate=" + startDateParam + ", endDate=" + endDateParam);
                System.out.println("Transfer API Response: " + response);
                
                // JSON parse et
                if (transfersList == null) {
                    transfersList = new ArrayList<>();
                }
                transfersList.clear();
                
                if (response.contains("\"success\":true") || response.contains("\"data\":{")) {
                    // JSON'dan verileri çıkar
                    parseTransfersFromJson(response);
                    
                    System.out.println("API'den " + transfersList.size() + " transfer yüklendi");
                } else {
                    System.err.println("API'den transfer verileri alınamadı veya boş yanıt döndü");
                    
                    // API başarısız ise hata mesajı göster
                    Platform.runLater(() -> {
                        showAlert("Uyarı", "Transfer verileri yüklenemedi. Backend API'si çalışıyor olmalı.");
                    });
                }
                
                Platform.runLater(() -> {
                    updateTable();
                    updateStats();
                });
                
            } catch (Exception e) {
                System.err.println("Transfer verileri yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    showAlert("Hata", "Transfer verileri yüklenirken hata oluştu: " + e.getMessage());
                    // Hata durumunda boş liste göster
                    if (transfersList == null) {
                        transfersList = new ArrayList<>();
                    }
                    transfersList.clear();
                    updateTable();
                    updateStats();
                });
            }
        });
    }
    
    private void updateTable() {
        Platform.runLater(() -> {
            transfersTable.getItems().clear();
            transfersTable.getItems().addAll(transfersList);
        });
    }
    
    private void parseTransfersFromJson(String response) {
        try {
            // API response formatı: {"success":true,"data":{"content":[...],"totalElements":x,...},"message":"..."}
            if (response.contains("\"content\":[")) {
                String contentStart = "\"content\":[";
                int startIndex = response.indexOf(contentStart);
                if (startIndex != -1) {
                    startIndex += contentStart.length();
                    int endIndex = findArrayEnd(response, startIndex);
                    if (endIndex != -1) {
                        String contentArray = response.substring(startIndex, endIndex);
                        parseTransferObjects(contentArray);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Transfer JSON parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int findArrayEnd(String json, int startIndex) {
        int brackets = 1;
        int index = startIndex;
        
        while (index < json.length() && brackets > 0) {
            char c = json.charAt(index);
            if (c == '[') brackets++;
            else if (c == ']') brackets--;
            index++;
        }
        
        return brackets == 0 ? index - 1 : -1;
    }
    
    private void parseTransferObjects(String contentArray) {
        try {
            // Transfer objelerini ayır ve parse et
            String[] objects = contentArray.split("(?<=}),(?=\\s*\\{)");
            
            for (String objStr : objects) {
                objStr = objStr.trim();
                if (objStr.startsWith("{") && objStr.endsWith("}")) {
                    TransferInfo transfer = parseTransferFromJson(objStr);
                    if (transfer != null) {
                        transfersList.add(transfer);
                    }
                }
            }
            
            System.out.println("✅ " + transfersList.size() + " transfer başarıyla parse edildi");
        } catch (Exception e) {
            System.err.println("Transfer objelerini parse ederken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private TransferInfo parseTransferFromJson(String jsonObject) {
        try {
            TransferInfo transfer = new TransferInfo();
            
            // JSON alanlarını çıkar
            transfer.setId(extractLongFromJson(jsonObject, "id"));
            transfer.setSenderWiban(extractStringFromJson(jsonObject, "senderWiban"));
            transfer.setReceiverWiban(extractStringFromJson(jsonObject, "receiverWiban"));
            
            String amountStr = extractStringFromJson(jsonObject, "amount");
            if (amountStr != null) {
                transfer.setAmount(new BigDecimal(amountStr));
            }
            
            transfer.setCurrency(extractStringFromJson(jsonObject, "currency"));
            transfer.setStatus(extractStringFromJson(jsonObject, "status"));
            transfer.setDescription(extractStringFromJson(jsonObject, "description"));
            
            String timestampStr = extractStringFromJson(jsonObject, "timestamp");
            if (timestampStr != null) {
                try {
                    // ISO timestamp parse etmeyi dene
                    transfer.setTimestamp(LocalDateTime.parse(timestampStr.replace("Z", "")));
                } catch (Exception e) {
                    System.err.println("Timestamp parse hatası: " + timestampStr);
                }
            }
            
            return transfer;
        } catch (Exception e) {
            System.err.println("Transfer objesi parse hatası: " + e.getMessage());
            return null;
        }
    }
    
    // JSON parsing utility methods
    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("String extract hatası (" + key + "): " + e.getMessage());
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
            System.err.println("Long extract hatası (" + key + "): " + e.getMessage());
        }
        return null;
    }
    
    private void updateStats() {
        Platform.runLater(() -> {
            totalTransfersLabel.setText(String.valueOf(transfersList.size()));
            
            BigDecimal totalAmount = transfersList.stream()
                .map(TransferInfo::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            totalAmountLabel.setText(String.format("%.2f TRY", totalAmount));
        });
    }
    
    private void filterTransfers() {
        // Basit filtreleme implementasyonu
        showAlert("Bilgi", "Arama özelliği yakında eklenecek.");
    }
    
    private void filterByDateRange() {
        showAlert("Bilgi", "Tarih filtreleme özelliği yakında eklenecek.");
    }
    
    private void clearFilters() {
        searchField.clear();
        statusFilter.setValue("Tümü");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadTransfersFromApi();
    }
    
    private void downloadExcel() {
        try {
            // Excel dosyasını API'den indir
            byte[] excelData = ApiClientFX.downloadTransferExcel(accessToken);
            
            if (excelData != null && excelData.length > 0) {
                // Dosya kaydetme dialog'u
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Excel Dosyasını Kaydet");
                fileChooser.setInitialFileName("transfer_raporu_" + 
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()) + ".xlsx");
                
                FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
                fileChooser.getExtensionFilters().add(excelFilter);
                
                File saveFile = fileChooser.showSaveDialog(stage);
                
                if (saveFile != null) {
                    // Dosyayı kaydet
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        fos.write(excelData);
                    }
                    
                    showAlert("Başarılı", "Excel dosyası başarıyla kaydedildi:\n" + saveFile.getAbsolutePath());
                }
            } else {
                showAlert("Hata", "Excel dosyası indirilemedi. Veri bulunamadı.");
            }
            
        } catch (IOException e) {
            showAlert("Hata", "Excel indirme işlemi sırasında hata oluştu:\n" + e.getMessage());
        } catch (Exception e) {
            showAlert("Hata", "Beklenmeyen hata:\n" + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            if (title.equals("Hata")) {
                alert.setAlertType(Alert.AlertType.ERROR);
            }
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
