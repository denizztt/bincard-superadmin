package com.bincard.bincard_superadmin;

import com.bincard.bincard_superadmin.model.ActionType;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        
        // Temel aksiyonlar
        actionComboBox.getItems().add("Tümü");
        
        // ActionType enum değerlerinden kategorilere göre gruplandırılmış aksiyonlar
        addActionTypeGroup(actionComboBox, "🔐 GİRİŞ / GÜVENLİK", 
            ActionType.LOGIN, ActionType.LOGOUT, ActionType.RESET_PASSWORD, ActionType.CHANGE_PASSWORD);
            
        addActionTypeGroup(actionComboBox, "👤 KULLANICI HESABI", 
            ActionType.SIGN_UP, ActionType.UPDATE_PROFILE, ActionType.DELETE_USER, 
            ActionType.DEACTIVATE_ACCOUNT, ActionType.ACTIVATE_ACCOUNT);
            
        addActionTypeGroup(actionComboBox, "🛡️ YETKİLENDİRME / ADMIN", 
            ActionType.APPROVE_ADMIN, ActionType.BLOCK_USER, ActionType.UNBLOCK_USER, 
            ActionType.PROMOTE_TO_ADMIN, ActionType.DEMOTE_TO_USER);
            
        addActionTypeGroup(actionComboBox, "🚌 KART İŞLEMLERİ", 
            ActionType.ADD_BUS_CARD, ActionType.DELETE_BUS_CARD, ActionType.BUS_CARD_TOP_UP, 
            ActionType.BUS_CARD_TRANSFER);
            
        addActionTypeGroup(actionComboBox, "👛 CÜZDAN VE ÖDEME", 
            ActionType.CREATE_WALLET, ActionType.DELETE_WALLET, ActionType.WALLET_TOP_UP, 
            ActionType.WALLET_TRANSFER);
            
        addActionTypeGroup(actionComboBox, "📊 RAPOR VE ANALİZ", 
            ActionType.EXPORT_USER_DATA, ActionType.EXPORT_WALLET_HISTORY, 
            ActionType.EXPORT_LOGIN_HISTORY);
            
        addActionTypeGroup(actionComboBox, "⚙️ SİSTEM / GENEL", 
            ActionType.SYSTEM_MAINTENANCE_START, ActionType.SYSTEM_MAINTENANCE_END);
            
        actionComboBox.setValue("Tümü");
        actionComboBox.setPrefWidth(250); // Genişletildi çünkü daha uzun değerler var
        
        // ComboBox görünümünü özelleştir
        actionComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.startsWith("----")) {
                        // Kategori başlıkları
                        setDisable(true);
                        setStyle("-fx-font-weight: bold; -fx-background-color: #f0f0f0;");
                        setText(item.replace("----", ""));
                    } else {
                        setDisable(false);
                        setStyle("");
                        setText(item);
                    }
                }
            }
        });
        
        // ComboBox seçim görünümünü özelleştir
        actionComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Kategori başlığı değil, sadece değeri göster
                    setText(item.replace("----", ""));
                }
            }
        });
        
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
        
        // Filtreleri dikey yerleştir, daha iyi sığması için
        VBox dateFilters = new VBox(5);
        dateFilters.getChildren().addAll(
            fromLabel, fromDatePicker,
            toLabel, toDatePicker
        );
        
        VBox actionFilter = new VBox(5);
        actionFilter.getChildren().addAll(
            actionLabel, actionComboBox
        );
        
        VBox buttons = new VBox(5);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0)); // Üstten biraz boşluk bırak
        buttons.getChildren().addAll(filterButton, clearButton);
        
        filterBox.getChildren().addAll(dateFilters, actionFilter, buttons);
        
        return filterBox;
    }
    
    /**
     * ComboBox'a kategori başlığı ve aksiyonları ekler
     */
    private void addActionTypeGroup(ComboBox<String> comboBox, String groupTitle, ActionType... actions) {
        comboBox.getItems().add("----" + groupTitle);
        
        for (ActionType action : actions) {
            comboBox.getItems().add(action.name());
        }
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
        
        TableColumn<AuditLog, String> actionColumn = new TableColumn<>("Aksiyon");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("displayAction"));
        actionColumn.setPrefWidth(180);
        
        TableColumn<AuditLog, String> descriptionColumn = new TableColumn<>("Açıklama");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(250);
        
        TableColumn<AuditLog, String> timestampColumn = new TableColumn<>("Zaman");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setPrefWidth(150);
        
        TableColumn<AuditLog, String> ipColumn = new TableColumn<>("IP Adresi");
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipColumn.setPrefWidth(120);
        
        TableColumn<AuditLog, String> deviceInfoColumn = new TableColumn<>("Cihaz Bilgisi");
        deviceInfoColumn.setCellValueFactory(new PropertyValueFactory<>("deviceInfo"));
        deviceInfoColumn.setPrefWidth(180);
        
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(actionColumn);
        tableView.getColumns().add(descriptionColumn);
        tableView.getColumns().add(timestampColumn);
        tableView.getColumns().add(ipColumn);
        tableView.getColumns().add(deviceInfoColumn);
        
        // Satır renklendirme
        tableView.setRowFactory(tv -> {
            TableRow<AuditLog> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldLog, newLog) -> {
                if (newLog == null) {
                    row.setStyle("");
                } else {
                    String action = newLog.getAction();
                    if (action == null) {
                        row.setStyle("");
                        return;
                    }
                    
                    // ActionType'ı kontrol et
                    ActionType actionType = ActionType.fromString(action);
                    if (actionType == null) {
                        // Eski verileri hala destekle
                        switch (action) {
                            case "DELETE":
                                row.setStyle("-fx-background-color: #ffe6e6;"); // Açık kırmızı
                                break;
                            case "CREATE":
                                row.setStyle("-fx-background-color: #e6ffe6;"); // Açık yeşil
                                break;
                            case "LOGIN":
                                row.setStyle("-fx-background-color: #e6f3ff;"); // Açık mavi
                                break;
                            case "ERROR":
                                row.setStyle("-fx-background-color: #ffcccc;"); // Kırmızı
                                break;
                            default:
                                row.setStyle("");
                        }
                    } else {
                        // Yeni ActionType enum'a göre renklendir
                        if (action.contains("DELETE") || action.contains("REJECT") || 
                            action.contains("BLOCK") || action.contains("DEACTIVATE")) {
                            row.setStyle("-fx-background-color: #ffe6e6;"); // Açık kırmızı - Silme/İptal işlemleri
                        } 
                        else if (action.contains("CREATE") || action.contains("ADD") || 
                                action.contains("SIGN_UP") || action.contains("ENABLE")) {
                            row.setStyle("-fx-background-color: #e6ffe6;"); // Açık yeşil - Ekleme/Oluşturma işlemleri
                        }
                        else if (action.contains("LOGIN") || action.contains("VERIFY")) {
                            row.setStyle("-fx-background-color: #e6f3ff;"); // Açık mavi - Giriş/Doğrulama işlemleri
                        }
                        else if (action.contains("UPDATE") || action.contains("CHANGE") || 
                                action.contains("EDIT")) {
                            row.setStyle("-fx-background-color: #fff8e1;"); // Açık sarı - Güncelleme işlemleri
                        }
                        else if (action.contains("EXPORT") || action.contains("REPORT")) {
                            row.setStyle("-fx-background-color: #e1f5fe;"); // Açık turkuaz - Rapor işlemleri
                        }
                        else if (action.contains("APPROVE") || action.contains("ACTIVATE") || 
                                action.contains("PROMOTE")) {
                            row.setStyle("-fx-background-color: #e8f5e9;"); // Koyu yeşil - Onay/Aktivasyon işlemleri
                        }
                        else if (action.contains("SYSTEM")) {
                            row.setStyle("-fx-background-color: #ede7f6;"); // Mor - Sistem işlemleri
                        }
                        else {
                            row.setStyle(""); // Diğer işlemler için varsayılan renk
                        }
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
        
        CompletableFuture.<ObservableList<AuditLog>>supplyAsync(() -> {
            try {
                String fromDate = fromDatePicker.getValue() != null ? 
                    fromDatePicker.getValue().toString() : null;
                String toDate = toDatePicker.getValue() != null ? 
                    toDatePicker.getValue().toString() : null;
                    
                // Seçilen aksiyonu kontrol et (kategori başlığı olmadığından emin ol)
                String selectedAction = actionComboBox.getValue();
                String action = null;
                
                if (selectedAction != null && !selectedAction.equals("Tümü") && !selectedAction.startsWith("----")) {
                    action = selectedAction; // API'ye gönderilecek aksiyon değeri
                    System.out.println("Seçilen aksiyon filtresi: " + action);
                }
                
                System.out.println("Audit logs isteniyor: fromDate=" + fromDate + ", toDate=" + toDate + ", action=" + action);
                
                // API çağrısı yapılıyor
                try {
                    String response = ApiClientFX.getAuditLogs(accessToken, fromDate, toDate, action);
                    
                    if (response == null) {
                        System.err.println("API yanıtı boş!");
                        return FXCollections.<AuditLog>observableArrayList(); // Boş liste döndür
                    }
                    
                    System.out.println("Audit logs API yanıtı alındı, uzunluk: " + response.length());
                    
                    ObservableList<AuditLog> result = parseAuditLogsResponse(response);
                    return result;
                } catch (Exception e) {
                    System.err.println("API çağrısı sırasında hata: " + e.getMessage());
                    e.printStackTrace();
                    return FXCollections.<AuditLog>observableArrayList(); // Hata durumunda boş liste döndür
                }
            } catch (Exception e) {
                System.err.println("Audit logs yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                return FXCollections.<AuditLog>observableArrayList(); // Hata durumunda boş liste döndür
            }
        }).thenAccept(auditLogs -> {
            // UI thread'inde çalış
            Platform.runLater(() -> {
                if (auditLogs != null && !auditLogs.isEmpty()) {
                    tableView.setItems(auditLogs);
                    statusLabel.setText("Son güncelleme: " + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")) +
                        " (" + auditLogs.size() + " kayıt)");
                } else {
                    tableView.setItems(FXCollections.observableArrayList()); // Tabloyu temizle
                    statusLabel.setText("Gösterilecek denetim kaydı bulunamadı veya API erişiminde sorun oluştu.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                Throwable cause = e.getCause();
                String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
                
                System.err.println("Audit logs alınırken hata: " + errorMessage);
                statusLabel.setText("Hata: " + (errorMessage != null ? errorMessage : "Bilinmeyen hata"));
                tableView.setItems(FXCollections.observableArrayList()); // Tabloyu temizle
                
                showErrorAlert("Denetim Kayıtları Yüklenemedi", 
                    "Denetim kayıtları yüklenirken bir hata oluştu: " + 
                    (errorMessage != null ? errorMessage : "Sunucuya bağlanılamıyor. Lütfen internet bağlantınızı ve sunucunun çalışır durumda olduğunu kontrol edin."));
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
            System.out.println("Parsing API response: " + 
                              (jsonResponse != null ? 
                               (jsonResponse.length() > 100 ? jsonResponse.substring(0, 100) + "..." : jsonResponse) 
                               : "null"));
            
            // Boş veya null yanıt kontrolü
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.err.println("Boş veya null API yanıtı");
                return auditLogs;
            }
            
            // Backend'den gelen format: {"success": true, "data": [...], "message": "..."}
            if (jsonResponse.contains("\"data\":[")) {
                System.out.println("Data array bulundu");
                
                String dataSection = jsonResponse.split("\"data\":")[1];
                if (dataSection.startsWith("[")) {
                    dataSection = dataSection.substring(1);
                    int endIndex = dataSection.lastIndexOf("]");
                    if (endIndex > 0) {
                        dataSection = dataSection.substring(0, endIndex);
                        System.out.println("Data bölümü çıkarıldı, uzunluk: " + dataSection.length());
                    } else {
                        System.err.println("Data array'in sonu bulunamadı");
                    }
                    
                    // Veri varsa parse et
                    if (!dataSection.trim().isEmpty()) {
                        // Her bir audit log objesini parse et
                        String[] logObjects = dataSection.split("\\},\\s*\\{");
                        System.out.println(logObjects.length + " adet log objesi bulundu");
                        
                        for (int i = 0; i < logObjects.length; i++) {
                            String logStr = logObjects[i];
                            // İlk eleman ve son eleman için özel işlem
                            if (i == 0 && !logStr.startsWith("{")) {
                                logStr = "{" + logStr;
                            }
                            if (i == logObjects.length - 1 && !logStr.endsWith("}")) {
                                logStr = logStr + "}";
                            }
                            
                            try {
                                AuditLog log = parseAuditLogObject(logStr);
                                if (log != null) {
                                    auditLogs.add(log);
                                }
                            } catch (Exception e) {
                                System.err.println("Log objesi parse hatası: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("Veri bölümü boş");
                    }
                } else {
                    System.err.println("Data array formatı geçersiz");
                }
            } else {
                System.err.println("Yanıtta data array bulunamadı");
            }
        } catch (Exception e) {
            System.err.println("Audit logs JSON parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return auditLogs;
    }
    
    /**
     * Tek bir audit log objesini parse eder
     * AuditLogDTO yapısına göre güncellenmiştir
     */
    private AuditLog parseAuditLogObject(String logStr) {
        try {
            System.out.println("Parsing log entry: " + (logStr.length() > 50 ? logStr.substring(0, 50) + "..." : logStr));
            
            String id = extractJsonValue(logStr, "id");
            String action = extractJsonValue(logStr, "action");
            String description = extractJsonValue(logStr, "description");
            String timestamp = extractJsonValue(logStr, "timestamp");
            String ipAddress = extractJsonValue(logStr, "ipAddress");
            String deviceInfo = extractJsonValue(logStr, "deviceInfo");
            
            // Debug bilgileri yazdır
            System.out.println("Extracted fields - ID: " + id + ", Action: " + action + 
                              ", Description: " + (description != null ? 
                                 (description.length() > 20 ? description.substring(0, 20) + "..." : description) : "null") +
                              ", Timestamp: " + timestamp);
            
            // Tarihi formatla
            String formattedTimestamp = formatTimestamp(timestamp);
            
            // Güvenli bir şekilde değerleri kontrol et
            id = id != null ? id : "0";
            action = action != null ? action : "UNKNOWN";
            description = description != null ? description : "";
            formattedTimestamp = formattedTimestamp != null ? formattedTimestamp : "N/A";
            ipAddress = ipAddress != null ? ipAddress : "";
            deviceInfo = deviceInfo != null ? deviceInfo : "";
            
            // Aksiyonu formatla
            String displayAction = formatActionForDisplay(action);
            
            return new AuditLog(id, action, displayAction, description, formattedTimestamp, ipAddress, deviceInfo);
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
     * Aksiyon tipini kullanıcı dostu bir formata dönüştürür
     * @param action API'den gelen aksiyon değeri
     * @return Formatlanmış aksiyon adı
     */
    private String formatActionForDisplay(String action) {
        if (action == null) return "BİLİNMİYOR";
        
        // ActionType enum'da var mı kontrol et
        ActionType actionType = ActionType.fromString(action);
        if (actionType != null) {
            return actionType.getDisplayName();
        }
        
        // Enum'da yoksa basit formatlama yap
        return action.replace("_", " ");
    }
    
    /**
     * Audit Log verilerini tutar
     * AuditLogDTO yapısına göre güncellenmiştir
     */
    public static class AuditLog {
        private String id;
        private String action;           // API'den gelen aksiyon kodu (LOGIN, LOGOUT vb.)
        private String displayAction;    // Kullanıcı dostu formatlanmış aksiyon (🔐 Giriş)
        private String description;
        private String timestamp;
        private String ipAddress;
        private String deviceInfo;
        
        public AuditLog(String id, String action, String displayAction, String description, 
                       String timestamp, String ipAddress, String deviceInfo) {
            this.id = id;
            this.action = action;
            this.displayAction = displayAction;
            this.description = description;
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
            this.deviceInfo = deviceInfo;
        }
        
        // Eski constructor (geriye dönük uyumluluk için)
        public AuditLog(String id, String action, String description, 
                       String timestamp, String ipAddress, String deviceInfo) {
            this(id, action, action, description, timestamp, ipAddress, deviceInfo);
        }
        
        // Getters
        public String getId() { return id; }
        public String getAction() { return action; }
        public String getDisplayAction() { return displayAction; }
        public String getDetails() { return description; } // Backward compatibility için
        public String getDescription() { return description; }
        public String getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
        public String getDeviceInfo() { return deviceInfo; }
        public String getUsername() { return ""; } // Backward compatibility için
        public String getResource() { return ""; } // Backward compatibility için
        
        // Setters
        public void setId(String id) { this.id = id; }
        public void setAction(String action) { this.action = action; }
        public void setDisplayAction(String displayAction) { this.displayAction = displayAction; }
        public void setDescription(String description) { this.description = description; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    }
}
