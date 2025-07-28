package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/**
 * Geri Bildirimler Sayfası
 * Tüm kullanıcı geri bildirimlerini listeleme, filtreleme ve görüntüleme
 */
public class FeedbackPage {
    
    private Stage stage;
    private TokenDTO accessToken;
    
    // UI Bileşenleri
    private TableView<Feedback> tableView;
    private ObservableList<Feedback> feedbackList;
    private ComboBox<String> typeFilter;
    private ComboBox<String> sourceFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label totalCountLabel;
    private Label statusLabel;
    private Pagination pagination;
    private ComboBox<String> sortComboBox;
    
    // Sayfalama
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalElements = 0;
    private int totalPages = 0;
    
    public FeedbackPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.feedbackList = FXCollections.observableArrayList();
        
        initializePage();
        loadFeedbacks();
    }
    
    private void initializePage() {
        // Ana sayfa düzeni
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Üst kısım - Başlık ve filtreler
        VBox topContainer = createTopSection();
        root.setTop(topContainer);
        
        // Orta kısım - Tablo
        VBox centerContainer = createCenterSection();
        root.setCenter(centerContainer);
        
        // Alt kısım - Sayfalama
        VBox bottomContainer = createBottomSection();
        root.setBottom(bottomContainer);
        
        // Sahne oluştur
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Geri Bildirimler - BinCard Superadmin");
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createTopSection() {
        VBox topContainer = new VBox(15);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Başlık
        Label titleLabel = new Label("📢 Geri Bildirimler");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Filtre paneli
        HBox filterPanel = createFilterPanel();
        
        // İstatistik paneli
        HBox statsPanel = createStatsPanel();
        
        topContainer.getChildren().addAll(titleLabel, filterPanel, statsPanel);
        return topContainer;
    }
    
    private HBox createFilterPanel() {
        HBox filterPanel = new HBox(15);
        filterPanel.setAlignment(Pos.CENTER_LEFT);
        filterPanel.setPadding(new Insets(15));
        filterPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Tip filtresi
        Label typeLabel = new Label("Tip:");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tümü", "COMPLAINT", "SUGGESTION", "COMPLIMENT", "BUG_REPORT", "FEATURE_REQUEST", "OTHER");
        typeFilter.setValue("Tümü");
        typeFilter.setPrefWidth(140);
        
        // Kaynak filtresi
        Label sourceLabel = new Label("Kaynak:");
        sourceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        sourceFilter = new ComboBox<>();
        sourceFilter.getItems().addAll("Tümü", "MOBILE_APP", "WEB_APP", "EMAIL", "PHONE", "OTHER");
        sourceFilter.setValue("Tümü");
        sourceFilter.setPrefWidth(120);
        
        // Tarih filtreleri
        Label startDateLabel = new Label("Başlangıç:");
        startDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        startDatePicker = new DatePicker();
        startDatePicker.setPrefWidth(130);
        
        Label endDateLabel = new Label("Bitiş:");
        endDateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        endDatePicker = new DatePicker();
        endDatePicker.setPrefWidth(130);
        
        // Sıralama
        Label sortLabel = new Label("Sıralama:");
        sortLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(
            "submittedAt,desc", 
            "submittedAt,asc", 
            "type,asc", 
            "source,asc"
        );
        sortComboBox.setValue("submittedAt,desc");
        sortComboBox.setPrefWidth(140);
        
        // Filtrele butonu
        Button filterButton = new Button("🔍 Filtrele");
        filterButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        filterButton.setOnAction(e -> {
            currentPage = 0;
            loadFeedbacks();
        });
        
        // Temizle butonu
        Button clearButton = new Button("🗑️ Temizle");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> {
            typeFilter.setValue("Tümü");
            sourceFilter.setValue("Tümü");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            sortComboBox.setValue("submittedAt,desc");
            currentPage = 0;
            loadFeedbacks();
        });
        
        filterPanel.getChildren().addAll(
            typeLabel, typeFilter,
            new Separator(),
            sourceLabel, sourceFilter,
            new Separator(),
            startDateLabel, startDatePicker,
            endDateLabel, endDatePicker,
            new Separator(),
            sortLabel, sortComboBox,
            new Separator(),
            filterButton, clearButton
        );
        
        return filterPanel;
    }
    
    private HBox createStatsPanel() {
        HBox statsPanel = new HBox(20);
        statsPanel.setAlignment(Pos.CENTER_LEFT);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        totalCountLabel = new Label("📊 Toplam: 0 geri bildirim");
        totalCountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        totalCountLabel.setTextFill(Color.web("#2c3e50"));
        
        statusLabel = new Label("🟢 Hazır");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#27ae60"));
        
        statsPanel.getChildren().addAll(totalCountLabel, new Separator(), statusLabel);
        return statsPanel;
    }
    
    private VBox createCenterSection() {
        VBox centerContainer = new VBox(10);
        
        // Tablo oluştur
        createTable();
        
        centerContainer.getChildren().add(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        return centerContainer;
    }
    
    private void createTable() {
        tableView = new TableView<>();
        tableView.setItems(feedbackList);
        tableView.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8;");
        
        // ID kolonu
        TableColumn<Feedback, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(60);
        idColumn.setStyle("-fx-alignment: CENTER;");
        
        // Tip kolonu
        TableColumn<Feedback, String> typeColumn = new TableColumn<>("Tip");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(120);
        typeColumn.setCellFactory(column -> new TableCell<Feedback, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(getTypeDisplayName(item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + getTypeColor(item) + ";");
                }
            }
        });
        
        // Konu kolonu
        TableColumn<Feedback, String> subjectColumn = new TableColumn<>("Konu");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        subjectColumn.setPrefWidth(200);
        
        // Kullanıcı kolonu
        TableColumn<Feedback, String> userColumn = new TableColumn<>("Kullanıcı");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userInfo"));
        userColumn.setPrefWidth(150);
        
        // Kaynak kolonu
        TableColumn<Feedback, String> sourceColumn = new TableColumn<>("Kaynak");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceColumn.setPrefWidth(100);
        sourceColumn.setCellFactory(column -> new TableCell<Feedback, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getSourceDisplayName(item));
                }
            }
        });
        
        // Tarih kolonu
        TableColumn<Feedback, String> dateColumn = new TableColumn<>("Gönderim Tarihi");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));
        dateColumn.setPrefWidth(150);
        
        // Mesaj kolonu (kısaltılmış)
        TableColumn<Feedback, String> messageColumn = new TableColumn<>("Mesaj");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("shortMessage"));
        messageColumn.setPrefWidth(300);
        
        // Aksiyon kolonu
        TableColumn<Feedback, Void> actionColumn = new TableColumn<>("İşlemler");
        actionColumn.setPrefWidth(100);
        actionColumn.setCellFactory(column -> new TableCell<Feedback, Void>() {
            private final Button detailButton = new Button("📝");
            
            {
                detailButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 5;");
                detailButton.setTooltip(new Tooltip("Detayları Görüntüle"));
                detailButton.setOnAction(e -> {
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    showFeedbackDetail(feedback);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
        });
        
        tableView.getColumns().addAll(idColumn, typeColumn, subjectColumn, userColumn, sourceColumn, dateColumn, messageColumn, actionColumn);
        
        // Satır seçimi için çift tıklama
        tableView.setRowFactory(tv -> {
            TableRow<Feedback> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showFeedbackDetail(row.getItem());
                }
            });
            return row;
        });
    }
    
    private VBox createBottomSection() {
        VBox bottomContainer = new VBox(10);
        bottomContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Sayfalama
        pagination = new Pagination();
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(10);
        pagination.setPageFactory(this::createPage);
        
        bottomContainer.getChildren().add(pagination);
        return bottomContainer;
    }
    
    private Node createPage(Integer pageIndex) {
        currentPage = pageIndex;
        loadFeedbacks();
        return new Label(); // Pagination için dummy node
    }
    
    private void loadFeedbacks() {
        statusLabel.setText("🔄 Yükleniyor...");
        statusLabel.setTextFill(Color.web("#f39c12"));
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String type = typeFilter.getValue();
                String source = sourceFilter.getValue();
                String startDate = startDatePicker.getValue() != null ? 
                    startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
                String endDate = endDatePicker.getValue() != null ? 
                    endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
                String sort = sortComboBox.getValue();
                
                return FeedbackApiClient.getAllFeedbacks(accessToken, currentPage, pageSize, sort, type, source, startDate, endDate);
            }
        };
        
        task.setOnSucceeded(e -> {
            String response = task.getValue();
            Platform.runLater(() -> processFeedbackResponse(response));
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("❌ Yükleme hatası");
                statusLabel.setTextFill(Color.web("#e74c3c"));
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setHeaderText("Geri bildirimler yüklenemedi");
                alert.setContentText("API'den veri alınamadı. Lütfen tekrar deneyin.");
                alert.showAndWait();
            });
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void processFeedbackResponse(String response) {
        try {
            // Basit JSON parsing - "success" alanını kontrol et
            boolean success = response.contains("\"success\":true") || !response.contains("\"success\":false");
            
            if (success) {
                // content array'ini çıkar
                int contentStart = response.indexOf("\"content\":[");
                if (contentStart != -1) {
                    contentStart += 11; // "content":[" 'den sonra
                    int contentEnd = response.indexOf("]", contentStart);
                    if (contentEnd != -1) {
                        String contentStr = response.substring(contentStart, contentEnd);
                        
                        // totalElements değerini çıkar
                        String totalElementsStr = extractJsonValue(response, "totalElements");
                        totalElements = totalElementsStr != null ? Integer.parseInt(totalElementsStr) : 0;
                        
                        // totalPages değerini çıkar
                        String totalPagesStr = extractJsonValue(response, "totalPages");
                        totalPages = totalPagesStr != null ? Integer.parseInt(totalPagesStr) : 1;
                        
                        feedbackList.clear();
                        
                        // Feedback objelerini parse et
                        if (!contentStr.trim().isEmpty()) {
                            String[] feedbackItems = contentStr.split("\\},\\{");
                            for (String item : feedbackItems) {
                                if (!item.trim().isEmpty()) {
                                    // Her item'ı temizle ve { } ekle
                                    if (!item.startsWith("{")) item = "{" + item;
                                    if (!item.endsWith("}")) item = item + "}";
                                    
                                    Feedback feedback = new Feedback(item);
                                    feedbackList.add(feedback);
                                }
                            }
                        }
                        
                        // UI güncellemeleri
                        totalCountLabel.setText("📊 Toplam: " + totalElements + " geri bildirim");
                        statusLabel.setText("🟢 " + feedbackList.size() + " kayıt yüklendi");
                        statusLabel.setTextFill(Color.web("#27ae60"));
                        
                        // Sayfalama güncelle
                        pagination.setPageCount(Math.max(1, totalPages));
                        pagination.setCurrentPageIndex(currentPage);
                    }
                }
            } else {
                String errorMessage = extractJsonValue(response, "message");
                if (errorMessage == null) errorMessage = "Bilinmeyen hata";
                statusLabel.setText("❌ " + errorMessage);
                statusLabel.setTextFill(Color.web("#e74c3c"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Response parse hatası: " + e.getMessage());
            statusLabel.setText("❌ Veri işleme hatası");
            statusLabel.setTextFill(Color.web("#e74c3c"));
        }
    }
    
    private void showFeedbackDetail(Feedback feedback) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Geri Bildirim Detayı");
        dialog.setHeaderText("Geri Bildirim #" + feedback.getId());
        
        // Detay içeriği
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            createDetailRow("📋 Tip:", getTypeDisplayName(feedback.getType())),
            createDetailRow("📝 Konu:", feedback.getSubject()),
            createDetailRow("👤 Kullanıcı:", feedback.getUserInfo()),
            createDetailRow("📱 Kaynak:", getSourceDisplayName(feedback.getSource())),
            createDetailRow("📅 Tarih:", feedback.getSubmittedAt()),
            new Separator(),
            createDetailRow("💬 Mesaj:", feedback.getMessage())
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(500, 400);
        scrollPane.setFitToWidth(true);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
    
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        
        Label labelLabel = new Label(label);
        labelLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        labelLabel.setPrefWidth(80);
        
        Label valueLabel = new Label(value != null ? value : "N/A");
        valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        valueLabel.setWrapText(true);
        
        row.getChildren().addAll(labelLabel, valueLabel);
        return row;
    }
    
    private String getTypeDisplayName(String type) {
        switch (type) {
            case "COMPLAINT": return "Şikayet";
            case "SUGGESTION": return "Öneri";
            case "COMPLIMENT": return "Takdir";
            case "BUG_REPORT": return "Hata Bildirimi";
            case "FEATURE_REQUEST": return "Özellik Talebi";
            case "OTHER": return "Diğer";
            default: return type;
        }
    }
    
    private String getTypeColor(String type) {
        switch (type) {
            case "COMPLAINT": return "#e74c3c";
            case "SUGGESTION": return "#3498db";
            case "COMPLIMENT": return "#27ae60";
            case "BUG_REPORT": return "#f39c12";
            case "FEATURE_REQUEST": return "#9b59b6";
            default: return "#7f8c8d";
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            
            // Boşlukları atla
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            
            // String değer mi kontrol et
            if (startIndex < json.length() && json.charAt(startIndex) == '"') {
                startIndex++; // " karakterini atla
                int endIndex = json.indexOf('"', startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            } else {
                // Sayısal değer
                int endIndex = startIndex;
                while (endIndex < json.length() && 
                       (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.' || json.charAt(endIndex) == '-')) {
                    endIndex++;
                }
                if (endIndex > startIndex) {
                    return json.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }
    
    private String getSourceDisplayName(String source) {
        switch (source) {
            case "MOBILE_APP": return "Mobil Uygulama";
            case "WEB_APP": return "Web Uygulaması";
            case "EMAIL": return "E-posta";
            case "PHONE": return "Telefon";
            case "OTHER": return "Diğer";
            default: return source;
        }
    }
    
    /**
     * Feedback model sınıfı
     */
    public static class Feedback {
        private final SimpleStringProperty id;
        private final SimpleStringProperty type;
        private final SimpleStringProperty subject;
        private final SimpleStringProperty message;
        private final SimpleStringProperty shortMessage;
        private final SimpleStringProperty userInfo;
        private final SimpleStringProperty source;
        private final SimpleStringProperty submittedAt;
        
        public Feedback(String jsonStr) {
            this.id = new SimpleStringProperty(extractValue(jsonStr, "id", "0"));
            this.type = new SimpleStringProperty(extractValue(jsonStr, "type", ""));
            this.subject = new SimpleStringProperty(extractValue(jsonStr, "subject", ""));
            
            String fullMessage = extractValue(jsonStr, "message", "");
            this.message = new SimpleStringProperty(fullMessage);
            
            // Kısa mesaj (150 karakter)
            String shortMsg = fullMessage.length() > 150 ? 
                fullMessage.substring(0, 150) + "..." : fullMessage;
            this.shortMessage = new SimpleStringProperty(shortMsg);
            
            this.source = new SimpleStringProperty(extractValue(jsonStr, "source", ""));
            this.submittedAt = new SimpleStringProperty(extractValue(jsonStr, "submittedAt", ""));
            
            // Kullanıcı bilgisi oluştur
            String userEmail = extractValue(jsonStr, "userEmail", "");
            String userName = extractValue(jsonStr, "userName", "");
            String userInfoStr = userName.isEmpty() ? userEmail : userName + " (" + userEmail + ")";
            this.userInfo = new SimpleStringProperty(userInfoStr);
        }
        
        private String extractValue(String json, String key, String defaultValue) {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                
                // Boşlukları atla
                while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                    startIndex++;
                }
                
                // String değer mi kontrol et
                if (startIndex < json.length() && json.charAt(startIndex) == '"') {
                    startIndex++; // " karakterini atla
                    int endIndex = json.indexOf('"', startIndex);
                    if (endIndex != -1) {
                        return json.substring(startIndex, endIndex);
                    }
                } else {
                    // Sayısal değer
                    int endIndex = startIndex;
                    while (endIndex < json.length() && 
                           (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.' || json.charAt(endIndex) == '-')) {
                        endIndex++;
                    }
                    if (endIndex > startIndex) {
                        return json.substring(startIndex, endIndex);
                    }
                }
            }
            return defaultValue;
        }
        
        // Getter metodları
        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }
        
        public String getType() { return type.get(); }
        public SimpleStringProperty typeProperty() { return type; }
        
        public String getSubject() { return subject.get(); }
        public SimpleStringProperty subjectProperty() { return subject; }
        
        public String getMessage() { return message.get(); }
        public SimpleStringProperty messageProperty() { return message; }
        
        public String getShortMessage() { return shortMessage.get(); }
        public SimpleStringProperty shortMessageProperty() { return shortMessage; }
        
        public String getUserInfo() { return userInfo.get(); }
        public SimpleStringProperty userInfoProperty() { return userInfo; }
        
        public String getSource() { return source.get(); }
        public SimpleStringProperty sourceProperty() { return source; }
        
        public String getSubmittedAt() { return submittedAt.get(); }
        public SimpleStringProperty submittedAtProperty() { return submittedAt; }
    }
}
