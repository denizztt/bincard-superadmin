package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Audit Log (Denetim Kayıtları) sayfası
 */
public class AuditLogsPage extends SuperadminPageBase {
    
    private TableView<AuditLog> tableView;
    private Label statusLabel;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<String> actionComboBox;
    
    public AuditLogsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Denetim Kayıtları");
        loadAuditLogs();
    }
    
    @Override
    protected Node createContent() {
        VBox content = createMainContent();
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    /**
     * Ana içerik alanını oluşturur
     */
    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f8f9fa;");
        
        // Başlık ve filtreler
        VBox titleAndFilters = createTitleAndFilters();
        
        // Durum etiketi
        statusLabel = new Label("Denetim kayıtları yükleniyor...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.web("#7f8c8d"));
        
        // Tablo
        VBox tableContainer = createTableContainer();
        
        content.getChildren().addAll(titleAndFilters, statusLabel, tableContainer);
        
        return content;
    }
    
    /**
     * Başlık ve filtreleri oluşturur
     */
    private VBox createTitleAndFilters() {
        VBox container = new VBox(20);
        
        // Başlık satırı
        HBox titleBox = new HBox(20);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label pageTitle = new Label("Denetim Kayıtları");
        pageTitle.setFont(Font.font("Montserrat", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web("#2d3436"));
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Yenile butonu
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> loadAuditLogs());
        
        titleBox.getChildren().addAll(pageTitle, spacer, refreshButton);
        
        // Filtre satırı
        HBox filterBox = createFilterBox();
        
        container.getChildren().addAll(titleBox, filterBox);
        
        return container;
    }
    
    /**
     * Filtre kutusunu oluşturur
     */
    private HBox createFilterBox() {
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(15));
        filterBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Başlangıç tarihi
        Label fromLabel = new Label("Başlangıç:");
        fromLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        fromDatePicker = new DatePicker();
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        fromDatePicker.setPrefWidth(150);
        
        // Bitiş tarihi
        Label toLabel = new Label("Bitiş:");
        toLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        toDatePicker = new DatePicker();
        toDatePicker.setValue(LocalDate.now());
        toDatePicker.setPrefWidth(150);
        
        // Aksiyon filtresi
        Label actionLabel = new Label("Aksiyon:");
        actionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        actionComboBox = new ComboBox<>();
        actionComboBox.getItems().addAll(
            "Tümü", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", 
            "APPROVE", "REJECT", "VIEW", "EXPORT"
        );
        actionComboBox.setValue("Tümü");
        actionComboBox.setPrefWidth(120);
        
        // Filtre butonu
        Button filterButton = new Button("Filtrele");
        filterButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        filterButton.setOnAction(e -> loadAuditLogs());
        
        // Temizle butonu
        Button clearButton = new Button("Temizle");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> {
            fromDatePicker.setValue(LocalDate.now().minusDays(30));
            toDatePicker.setValue(LocalDate.now());
            actionComboBox.setValue("Tümü");
            loadAuditLogs();
        });
        
        filterBox.getChildren().addAll(
            fromLabel, fromDatePicker,
            toLabel, toDatePicker,
            actionLabel, actionComboBox,
            filterButton, clearButton
        );
        
        return filterBox;
    }
    
    /**
     * Tablo konteynırını oluşturur
     */
    private VBox createTableContainer() {
        VBox container = new VBox(10);
        
        // Tablo
        tableView = new TableView<>();
        tableView.setPrefHeight(500);
        tableView.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Sütunlar
        TableColumn<AuditLog, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        
        TableColumn<AuditLog, String> userColumn = new TableColumn<>("Kullanıcı");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userColumn.setPrefWidth(150);
        
        TableColumn<AuditLog, String> actionColumn = new TableColumn<>("Aksiyon");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.setPrefWidth(120);
        
        TableColumn<AuditLog, String> resourceColumn = new TableColumn<>("Kaynak");
        resourceColumn.setCellValueFactory(new PropertyValueFactory<>("resource"));
        resourceColumn.setPrefWidth(150);
        
        TableColumn<AuditLog, String> detailsColumn = new TableColumn<>("Detaylar");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setPrefWidth(250);
        
        TableColumn<AuditLog, String> timestampColumn = new TableColumn<>("Zaman");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setPrefWidth(150);
        
        TableColumn<AuditLog, String> ipColumn = new TableColumn<>("IP Adresi");
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipColumn.setPrefWidth(120);
        
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(userColumn);
        tableView.getColumns().add(actionColumn);
        tableView.getColumns().add(resourceColumn);
        tableView.getColumns().add(detailsColumn);
        tableView.getColumns().add(timestampColumn);
        tableView.getColumns().add(ipColumn);
        
        // Satır renklendirme
        tableView.setRowFactory(tv -> {
            TableRow<AuditLog> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldLog, newLog) -> {
                if (newLog == null) {
                    row.setStyle("");
                } else {
                    switch (newLog.getAction()) {
                        case "DELETE":
                            row.setStyle("-fx-background-color: #ffe6e6;");
                            break;
                        case "CREATE":
                            row.setStyle("-fx-background-color: #e6ffe6;");
                            break;
                        case "LOGIN":
                            row.setStyle("-fx-background-color: #e6f3ff;");
                            break;
                        case "ERROR":
                            row.setStyle("-fx-background-color: #ffcccc;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });
        
        container.getChildren().add(tableView);
        
        return container;
    }
    
    /**
     * API'dan audit log verilerini yükler
     */
    private void loadAuditLogs() {
        statusLabel.setText("Denetim kayıtları yükleniyor...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                String fromDate = fromDatePicker.getValue() != null ? 
                    fromDatePicker.getValue().toString() : null;
                String toDate = toDatePicker.getValue() != null ? 
                    toDatePicker.getValue().toString() : null;
                String action = actionComboBox.getValue() != null && 
                    !actionComboBox.getValue().equals("Tümü") ? 
                    actionComboBox.getValue() : null;
                
                String response = ApiClientFX.getAuditLogs(accessToken, fromDate, toDate, action);
                System.out.println("Audit logs API yanıtı: " + response);
                
                return parseAuditLogsResponse(response);
            } catch (Exception e) {
                System.err.println("Audit logs yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }).thenAccept(auditLogs -> {
            // UI thread'inde çalış
            Platform.runLater(() -> {
                if (auditLogs != null) {
                    tableView.setItems(auditLogs);
                    statusLabel.setText("Son güncelleme: " + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")) +
                        " (" + auditLogs.size() + " kayıt)");
                } else {
                    statusLabel.setText("Denetim kayıtları alınamadı");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Hata: " + e.getMessage());
                showErrorAlert("Denetim Kayıtları Yüklenemedi", 
                    "Denetim kayıtları yüklenirken bir hata oluştu: " + e.getMessage());
            });
            return null;
        });
    }
    
    /**
     * API'dan gelen audit logs response'unu parse eder
     */
    private ObservableList<AuditLog> parseAuditLogsResponse(String jsonResponse) {
        ObservableList<AuditLog> auditLogs = FXCollections.observableArrayList();
        
        try {
            // Backend'den gelen format: {"success": true, "data": [...], "message": "..."}
            if (jsonResponse.contains("\"data\":[")) {
                String dataSection = jsonResponse.split("\"data\":")[1];
                if (dataSection.startsWith("[")) {
                    dataSection = dataSection.substring(1);
                    int endIndex = dataSection.lastIndexOf("]");
                    if (endIndex > 0) {
                        dataSection = dataSection.substring(0, endIndex);
                    }
                    
                    // Her bir audit log objesini parse et
                    String[] logObjects = dataSection.split("\\},\\s*\\{");
                    
                    for (String logStr : logObjects) {
                        logStr = logStr.replace("{", "").replace("}", "");
                        
                        AuditLog log = parseAuditLogObject(logStr);
                        if (log != null) {
                            auditLogs.add(log);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Audit logs JSON parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return auditLogs;
    }
    
    /**
     * Tek bir audit log objesini parse eder
     */
    private AuditLog parseAuditLogObject(String logStr) {
        try {
            String id = extractJsonValue(logStr, "id");
            String username = extractJsonValue(logStr, "username");
            String action = extractJsonValue(logStr, "action");
            String resource = extractJsonValue(logStr, "resource");
            String details = extractJsonValue(logStr, "details");
            String timestamp = extractJsonValue(logStr, "timestamp");
            String ipAddress = extractJsonValue(logStr, "ipAddress");
            
            // Tarihi formatla
            String formattedTimestamp = formatTimestamp(timestamp);
            
            return new AuditLog(
                id != null ? id : "0",
                username != null ? username : "Bilinmiyor",
                action != null ? action : "UNKNOWN",
                resource != null ? resource : "",
                details != null ? details : "",
                formattedTimestamp,
                ipAddress != null ? ipAddress : ""
            );
        } catch (Exception e) {
            System.err.println("Audit log object parse hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON string'den değer çıkarır
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("JSON değer çıkarma hatası: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * ISO timestamp'i kullanıcı dostu formata çevirir
     */
    private String formatTimestamp(String isoTimestamp) {
        try {
            if (isoTimestamp != null && !isoTimestamp.isEmpty()) {
                // ISO formatını parse et ve Türkçe formata çevir
                LocalDateTime dateTime = LocalDateTime.parse(isoTimestamp.replace("Z", ""));
                return dateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss"));
            }
        } catch (Exception e) {
            System.err.println("Timestamp format hatası: " + e.getMessage());
        }
        return isoTimestamp;
    }
    
    /**
     * Hata alerti gösterir
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Audit Log verilerini tutar
     */
    public static class AuditLog {
        private String id;
        private String username;
        private String action;
        private String resource;
        private String details;
        private String timestamp;
        private String ipAddress;
        
        public AuditLog(String id, String username, String action, String resource, 
                       String details, String timestamp, String ipAddress) {
            this.id = id;
            this.username = username;
            this.action = action;
            this.resource = resource;
            this.details = details;
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
        }
        
        // Getters
        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getAction() { return action; }
        public String getResource() { return resource; }
        public String getDetails() { return details; }
        public String getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
        
        // Setters
        public void setId(String id) { this.id = id; }
        public void setUsername(String username) { this.username = username; }
        public void setAction(String action) { this.action = action; }
        public void setResource(String resource) { this.resource = resource; }
        public void setDetails(String details) { this.details = details; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }
}
