package com.bincard.bincard_superadmin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class NewsPage extends SuperadminPageBase {

    // Haber verileri için iç sınıf
    // AdminNewsDTO modeline uygun iç sınıf
    public static class News {
        private Long id;
        private String title;
        private String content;
        private String image;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private boolean active;
        private String platform;
        private String priority;
        private String type;
        private int viewCount;
        private int likeCount;
        private boolean allowFeedback;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public News(Long id, String title, String content, String image, 
                   LocalDateTime startDate, LocalDateTime endDate, boolean active, 
                   String platform, String priority, String type, int viewCount, 
                   int likeCount, boolean allowFeedback, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.image = image;
            this.startDate = startDate;
            this.endDate = endDate;
            this.active = active;
            this.platform = platform;
            this.priority = priority;
            this.type = type;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.allowFeedback = allowFeedback;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getImage() { return image; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public String getStartDateString() {
            return startDate != null ? startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }
        public String getEndDateString() {
            return endDate != null ? endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }
        public boolean isActive() { return active; }
        public String getStatus() { return active ? "Aktif" : "Pasif"; }
        public String getPlatform() { return platform; }
        public String getPriority() { return priority; }
        public String getType() { return type; }
        public int getViewCount() { return viewCount; }
        public int getLikeCount() { return likeCount; }
        public boolean isAllowFeedback() { return allowFeedback; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public String getCreatedAtString() {
            return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }
        public String getUpdatedAtString() {
            return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }
        
        // JSON yanıtından News nesnesi oluşturmak için factory metodu
        public static News fromJson(String jsonObject) {
            // JSON parsing işlemi (basit bir yaklaşım)
            try {
                String id = extractJsonValue(jsonObject, "id");
                String title = extractJsonValue(jsonObject, "title");
                String content = extractJsonValue(jsonObject, "content");
                String image = extractJsonValue(jsonObject, "image");
                String startDateStr = extractJsonValue(jsonObject, "startDate");
                String endDateStr = extractJsonValue(jsonObject, "endDate");
                String activeStr = extractJsonValue(jsonObject, "active");
                String platform = extractJsonValue(jsonObject, "platform");
                String priority = extractJsonValue(jsonObject, "priority");
                String type = extractJsonValue(jsonObject, "type");
                String viewCountStr = extractJsonValue(jsonObject, "viewCount");
                String likeCountStr = extractJsonValue(jsonObject, "likeCount");
                String allowFeedbackStr = extractJsonValue(jsonObject, "allowFeedback");
                String createdAtStr = extractJsonValue(jsonObject, "createdAt");
                String updatedAtStr = extractJsonValue(jsonObject, "updatedAt");
                
                System.out.println("Tarih değerleri: startDate=" + startDateStr + ", endDate=" + endDateStr + 
                                  ", createdAt=" + createdAtStr + ", updatedAt=" + updatedAtStr);
                
                // Daha esnek tarih-saat formatı işleme
                LocalDateTime startDate = parseDateTimeFlexible(startDateStr);
                LocalDateTime endDate = parseDateTimeFlexible(endDateStr);
                LocalDateTime createdAt = parseDateTimeFlexible(createdAtStr);
                LocalDateTime updatedAt = parseDateTimeFlexible(updatedAtStr);
                
                return new News(
                    id != null ? Long.parseLong(id) : null,
                    title,
                    content,
                    image,
                    startDate,
                    endDate,
                    activeStr != null ? Boolean.parseBoolean(activeStr) : false,
                    platform,
                    priority,
                    type,
                    viewCountStr != null ? Integer.parseInt(viewCountStr) : 0,
                    likeCountStr != null ? Integer.parseInt(likeCountStr) : 0,
                    allowFeedbackStr != null ? Boolean.parseBoolean(allowFeedbackStr) : false,
                    createdAt,
                    updatedAt
                );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        // Farklı formatlardaki tarih-saat değerlerini işleyen yardımcı metod
        private static LocalDateTime parseDateTimeFlexible(String dateTimeStr) {
            if (dateTimeStr == null || dateTimeStr.isEmpty() || dateTimeStr.equals("null")) {
                return null;
            }
            
            try {
                // ISO_DATE_TIME formatını kullan
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // Eğer ISO format başarısız olursa, diğer formatları dene
                try {
                    // Z ile biten formatlar için
                    if (dateTimeStr.endsWith("Z")) {
                        dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 1);
                        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
                    }
                    
                    // Milisaniye ve mikrosaniye formatlarını destekle
                    if (dateTimeStr.length() > 19) {
                        // Saniye formatına düşür
                        return LocalDateTime.parse(dateTimeStr.substring(0, 19));
                    }
                    
                    // Diğer formatları dene
                    return LocalDateTime.parse(dateTimeStr);
                } catch (Exception ex) {
                    System.err.println("Tarih-saat ayrıştırma hatası: " + dateTimeStr + " - " + ex.getMessage());
                    return null; // Hata durumunda null döndür
                }
            }
        }
        
        private static String extractJsonValue(String json, String key) {
            if (json == null) return null;
            
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            
            startIndex += searchKey.length();
            
            // Değer null mu?
            if (json.substring(startIndex).trim().startsWith("null")) {
                return null;
            }
            
            // Değer bir string mi?
            if (json.charAt(startIndex) == '"') {
                startIndex++; // Başlangıç tırnağını atla
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex == -1) return null;
                return json.substring(startIndex, endIndex);
            } 
            // Değer bir sayı, boolean veya null mu?
            else {
                int endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
                if (endIndex == -1) return null;
                String value = json.substring(startIndex, endIndex).trim();
                return "null".equals(value) ? null : value;
            }
        }
    }

    private TableView<News> newsTable;
    private List<News> newsList;
    private ComboBox<String> platformFilter;
    private ComboBox<String> typeFilter;
    private ComboBox<String> priorityFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private HostServices hostServices;

    public NewsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken, HostServices hostServices) {
        super(stage, accessToken, refreshToken, "Haberler");
        this.newsList = new ArrayList<>();
        this.hostServices = hostServices;
    }

    @Override
    protected Node createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);

        // Üst kısım - Arama ve filtreler
        Node topControls = createTopControls();

        // Tablo
        newsTable = createNewsTable();
        VBox.setVgrow(newsTable, Priority.ALWAYS);

        // Alt kısım - CRUD butonları
        HBox bottomControls = createBottomControls(newsTable);

        content.getChildren().addAll(topControls, newsTable, bottomControls);
        
        // Verileri yükle
        loadNewsData();
        
        return content;
    }

    private Node createTopControls() {
        VBox controlsContainer = new VBox(10);
        
        // Üst satır - Arama ve temel filtreler
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Arama alanı
        TextField searchField = new TextField();
        searchField.setPromptText("Haber ara (Başlık, İçerik...)");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px;");

        Button searchButton = new Button("Ara");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchButton.setOnAction(e -> searchNews(searchField.getText()));

        // Platform filtresi
        Label platformLabel = new Label("Platform:");
        platformLabel.setStyle("-fx-font-size: 14px;");

        platformFilter = new ComboBox<>();
        platformFilter.getItems().addAll("Tümü", "WEB", "MOBILE", "DESKTOP", "TABLET", "KIOSK", "ALL");
        platformFilter.setValue("Tümü");
        platformFilter.setStyle("-fx-font-size: 14px;");
        platformFilter.setOnAction(e -> filterNews());

        // Haber tipi filtresi
        Label typeLabel = new Label("Kategori:");
        typeLabel.setStyle("-fx-font-size: 14px;");

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tümü", "DUYURU", "KAMPANYA", "BAKIM", "BILGILENDIRME", 
                                    "GUNCELLEME", "UYARI", "ETKINLIK");
        typeFilter.setValue("Tümü");
        typeFilter.setStyle("-fx-font-size: 14px;");
        typeFilter.setOnAction(e -> filterNews());

        // Yenile butonu
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadNewsData());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(searchField, searchButton, platformLabel, platformFilter, 
                                   typeLabel, typeFilter, spacer, refreshButton);
        
        // Alt satır - Tarih ve öncelik filtreleri
        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        // Tarih aralığı filtreleri
        Label dateRangeLabel = new Label("Tarih Aralığı:");
        dateRangeLabel.setStyle("-fx-font-size: 14px;");
        
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        startDatePicker.setPromptText("Başlangıç Tarihi");
        startDatePicker.setStyle("-fx-font-size: 14px;");
        
        Label toLabel = new Label("-");
        
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPromptText("Bitiş Tarihi");
        endDatePicker.setStyle("-fx-font-size: 14px;");
        
        Button dateFilterButton = new Button("Tarihe Göre Filtrele");
        dateFilterButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        dateFilterButton.setOnAction(e -> filterByDateRange());
        
        // Öncelik filtresi
        Label priorityLabel = new Label("Öncelik:");
        priorityLabel.setStyle("-fx-font-size: 14px;");
        
        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("Tümü", "COK_DUSUK", "DUSUK", "NORMAL", 
                                        "ORTA_YUKSEK", "YUKSEK", "COK_YUKSEK", "KRITIK");
        priorityFilter.setValue("Tümü");
        priorityFilter.setStyle("-fx-font-size: 14px;");
        priorityFilter.setOnAction(e -> filterNews());
        
        HBox spacer2 = new HBox();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        bottomRow.getChildren().addAll(dateRangeLabel, startDatePicker, toLabel, endDatePicker, 
                                      dateFilterButton, priorityLabel, priorityFilter, spacer2);
        
        controlsContainer.getChildren().addAll(topRow, bottomRow);
        return controlsContainer;
    }
    
    private void filterByDateRange() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Lütfen başlangıç ve bitiş tarihlerini seçin.");
            return;
        }
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (endDate.isBefore(startDate)) {
            showAlert("Bitiş tarihi başlangıç tarihinden önce olamaz.");
            return;
        }
        
        // Tarih aralığına göre filtreleme (gerçek uygulamada API çağrısı olacak)
        List<News> filteredList = new ArrayList<>();
        
        for (News news : newsList) {
            LocalDate newsDate = news.getCreatedAt().toLocalDate();
            if ((newsDate.isEqual(startDate) || newsDate.isAfter(startDate)) && 
                (newsDate.isEqual(endDate) || newsDate.isBefore(endDate))) {
                filteredList.add(news);
            }
        }
        
        newsTable.getItems().clear();
        newsTable.getItems().addAll(filteredList);
    }

    private TableView<News> createNewsTable() {
        TableView<News> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14px;");

        // Sütunlar
        TableColumn<News, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);

        TableColumn<News, String> titleColumn = new TableColumn<>("Başlık");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(200);

        TableColumn<News, String> dateColumn = new TableColumn<>("Oluşturma Tarihi");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtString"));
        dateColumn.setPrefWidth(120);

        TableColumn<News, String> startDateColumn = new TableColumn<>("Başlangıç");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDateString"));
        startDateColumn.setPrefWidth(120);

        TableColumn<News, String> endDateColumn = new TableColumn<>("Bitiş");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDateString"));
        endDateColumn.setPrefWidth(120);

        TableColumn<News, String> typeColumn = new TableColumn<>("Kategori");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(100);

        TableColumn<News, String> platformColumn = new TableColumn<>("Platform");
        platformColumn.setCellValueFactory(new PropertyValueFactory<>("platform"));
        platformColumn.setPrefWidth(100);

        TableColumn<News, String> priorityColumn = new TableColumn<>("Öncelik");
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setPrefWidth(100);

        TableColumn<News, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);

        TableColumn<News, Integer> viewsColumn = new TableColumn<>("Görüntülenme");
        viewsColumn.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
        viewsColumn.setPrefWidth(100);

        TableColumn<News, Integer> likesColumn = new TableColumn<>("Beğeni");
        likesColumn.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        likesColumn.setPrefWidth(60);

        table.getColumns().addAll(idColumn, titleColumn, dateColumn, typeColumn, 
                                 platformColumn, priorityColumn, statusColumn, 
                                 viewsColumn, likesColumn);

        // Çift tıklama ile detay görüntüleme
        table.setRowFactory(tv -> {
            TableRow<News> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    News news = row.getItem();
                    showNewsDetailDialog(news);
                }
            });
            return row;
        });

        return table;
    }

    private HBox createBottomControls(TableView<News> table) {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button addButton = new Button("Yeni Haber Ekle");
        addButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddNewsDialog());

        Button editButton = new Button("Düzenle");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(e -> {
            News selectedNews = table.getSelectionModel().getSelectedItem();
            if (selectedNews != null) {
                showEditNewsDialog(selectedNews);
            } else {
                showAlert("Lütfen düzenlemek için bir haber seçin.");
            }
        });

        Button deactivateButton = new Button("Pasif Yap");
        deactivateButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deactivateButton.setOnAction(e -> {
            News selectedNews = table.getSelectionModel().getSelectedItem();
            if (selectedNews != null) {
                if (selectedNews.isActive()) {
                    showDeleteConfirmation(selectedNews);
                } else {
                    showAlert("Seçilen haber zaten pasif durumda.");
                }
            } else {
                showAlert("Lütfen pasif yapmak için bir haber seçin.");
            }
        });

        Button statsButton = new Button("İstatistikler");
        statsButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        statsButton.setOnAction(e -> showNewsStatistics());

        controls.getChildren().addAll(addButton, editButton, deactivateButton, statsButton);
        return controls;
    }

    private void loadNewsData() {
        // Örnek veri (gerçek uygulamada API'den gelecek)
        // API entegrasyonu yapılana kadar örnek verilerle çalışacak
        if (newsList == null) {
            newsList = new ArrayList<>();
        }
        newsList.clear();
        
        try {
            // API'den haberleri çek
            String selectedPlatform = platformFilter.getValue();
            String platform = "Tümü".equals(selectedPlatform) ? null : selectedPlatform;
            
            // Status bilgisi güncelle
            System.out.println("Haberler yükleniyor...");
            
            String response = ApiClientFX.getAllNews(accessToken, platform);
            System.out.println("API Yanıtı: " + response);
            
            // API yanıtının formatını tespit etmek için detaylı log ekle
            if (response != null) {
                System.out.println("API yanıtı uzunluğu: " + response.length());
                System.out.println("API yanıtı ilk 10 karakter: " + response.substring(0, Math.min(10, response.length())));
                System.out.println("API yanıtı son 10 karakter: " + response.substring(Math.max(0, response.length() - 10)));
                System.out.println("data alanı içeriyor mu: " + response.contains("\"data\""));
                System.out.println("Dizi olarak başlıyor mu: " + response.trim().startsWith("["));
                System.out.println("Dizi olarak bitiyor mu: " + response.trim().endsWith("]"));
            }
            
            // JSON yanıtını işle
            if (response != null && !response.isEmpty()) {
                // API artık doğrudan JSON dizisi döndürebilir veya "data" alanında gönderebilir
                String dataArray;
                
                // Önce "data" alanını kontrol et
                int dataStart = response.indexOf("\"data\":[");
                if (dataStart != -1) {
                    // "data" alanı bulundu, içerisindeki diziyi çıkar
                    dataStart += 8; // "data":[ uzunluğu
                    int dataEnd = response.lastIndexOf("]");
                    if (dataEnd > dataStart) {
                        dataArray = response.substring(dataStart, dataEnd);
                    } else {
                        System.err.println("JSON veri dizisi bulunamadı veya hatalı format");
                        return;
                    }
                } else if (response.trim().startsWith("[") && response.trim().endsWith("]")) {
                    // Doğrudan dizi döndürülmüş
                    dataArray = response.substring(1, response.length() - 1);
                } else {
                    System.err.println("Tanınmayan JSON formatı, ne 'data' alanı ne de dizi formatında");
                    return;
                }
                
                // Her bir haber nesnesini işle
                int startIndex = 0;
                int braceCount = 0;
                
                for (int i = 0; i < dataArray.length(); i++) {
                    char c = dataArray.charAt(i);
                    
                    if (c == '{') {
                        if (braceCount == 0) {
                            startIndex = i;
                        }
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            // Bir nesne tamamlandı
                            String jsonObject = dataArray.substring(startIndex, i + 1);
                            News news = News.fromJson(jsonObject);
                            if (news != null) {
                                newsList.add(news);
                            }
                        }
                    }
                }
            } else {
                System.err.println("API yanıtı boş veya null");
            }
            
            // Tabloya ekle
            newsTable.getItems().clear();
            newsTable.getItems().addAll(newsList);            } catch (Exception e) {
            System.err.println("Haberler API'si mevcut değil veya hata oluştu: " + e.getMessage());
            e.printStackTrace();
            
            // Hata durumunda sessizce örnek verilerle devam et
            System.out.println("API'den veri alınamadı, örnek verilerle devam ediliyor...");
            
            // Eğer boş dizi dönerse veya hata alınırsa haber listesini temizle
            newsList.clear();
            createSampleNews();
            
            // Tabloya örnek verileri ekle
            newsTable.getItems().clear();
            newsTable.getItems().addAll(newsList);
        }
    }
    
    // Örnek haberler oluşturan yardımcı metod
    private void createSampleNews() {
        LocalDateTime now = LocalDateTime.now();
        
        // Örnek haber 1
        newsList.add(new News(
            1L, 
            "Yeni Otobüs Hatları", 
            "Şehrimizde yeni otobüs hatları hizmete girdi. Yeni hatlar sayesinde şehrin doğu bölgelerine ulaşım daha kolay hale gelecek.",
            "https://example.com/image1.jpg", 
            now.minusDays(2), // startDate
            now.plusMonths(1), // endDate
            true, // active
            "WEB", // platform
            "YUKSEK", // priority
            "DUYURU", // type
            120, // viewCount
            15, // likeCount
            true, // allowFeedback
            now.minusDays(2), // createdAt
            now.minusDays(2) // updatedAt
        ));
        
        // Örnek haber 2
        newsList.add(new News(
            2L, 
            "Kart Bakiye Yükleme Noktaları", 
            "Yeni kart bakiye yükleme noktaları eklendi. Artık şehrin 25 farklı noktasından kart bakiyenizi yükleyebilirsiniz.",
            "https://example.com/image2.jpg", 
            now.minusDays(5), // startDate
            now.plusDays(25), // endDate
            true, // active
            "MOBILE", // platform
            "NORMAL", // priority
            "BILGILENDIRME", // type
            85, // viewCount
            8, // likeCount
            true, // allowFeedback
            now.minusDays(5), // createdAt
            now.minusDays(4) // updatedAt
        ));
        
        // Tabloyu güncelle
        newsTable.getItems().clear();
        newsTable.getItems().addAll(newsList);
    }

    private void searchNews(String query) {
        if (query == null || query.trim().isEmpty()) {
            newsTable.getItems().clear();
            newsTable.getItems().addAll(newsList);
            return;
        }
        
        query = query.toLowerCase();
        List<News> filteredList = new ArrayList<>();
        
        for (News news : newsList) {
            if (news.getTitle().toLowerCase().contains(query) || 
                news.getContent().toLowerCase().contains(query)) {
                filteredList.add(news);
            }
        }
        
        newsTable.getItems().clear();
        newsTable.getItems().addAll(filteredList);
    }

    private void filterNews() {
        String selectedPlatform = platformFilter.getValue();
        String selectedType = typeFilter.getValue();
        String selectedPriority = priorityFilter.getValue();
        
        List<News> filteredList = new ArrayList<>();
        
        for (News news : newsList) {
            boolean platformMatch = "Tümü".equals(selectedPlatform) || 
                                   news.getPlatform().equals(selectedPlatform);
            boolean typeMatch = "Tümü".equals(selectedType) || 
                               news.getType().equals(selectedType);
            boolean priorityMatch = "Tümü".equals(selectedPriority) || 
                               news.getPriority().equals(selectedPriority);
            
            if (platformMatch && typeMatch && priorityMatch) {
                filteredList.add(news);
            }
        }
        
        newsTable.getItems().clear();
        newsTable.getItems().addAll(filteredList);
    }

    private void showAddNewsDialog() {
        Dialog<News> dialog = new Dialog<>();
        dialog.setTitle("Yeni Haber Ekle");
        dialog.setHeaderText("Haber bilgilerini doldurun");

        // "Ekle" ve "İptal" butonları
        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Form alanları
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Başlık
        TextField titleField = new TextField();
        titleField.setPromptText("Haber Başlığı");
        grid.add(new Label("Başlık:"), 0, 0);
        grid.add(titleField, 1, 0, 3, 1);

        // İçerik
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Haber İçeriği");
        contentArea.setPrefRowCount(5);
        grid.add(new Label("İçerik:"), 0, 1);
        grid.add(contentArea, 1, 1, 3, 1);

        // Görsel
        HBox imageBox = new HBox(10);
        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Resim Yolu");
        imagePathField.setPrefWidth(300);
        Button browseButton = new Button("Gözat");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Haber Görseli Seç");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        imageBox.getChildren().addAll(imagePathField, browseButton);
        grid.add(new Label("Görsel:"), 0, 2);
        grid.add(imageBox, 1, 2, 3, 1);

        // Kategori
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("DUYURU", "KAMPANYA", "BAKIM", "BILGILENDIRME", 
                                   "GUNCELLEME", "UYARI", "ETKINLIK");
        typeCombo.setValue("DUYURU");
        typeCombo.setPromptText("Haber Kategorisi");
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(typeCombo, 1, 3);

        // Platform
        ComboBox<String> platformCombo = new ComboBox<>();
        platformCombo.getItems().addAll("WEB", "MOBILE", "DESKTOP", "TABLET", "KIOSK", "ALL");
        platformCombo.setValue("WEB");
        platformCombo.setPromptText("Platform");
        grid.add(new Label("Platform:"), 2, 3);
        grid.add(platformCombo, 3, 3);

        // Öncelik
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("COK_DUSUK", "DUSUK", "NORMAL", 
                                       "ORTA_YUKSEK", "YUKSEK", "COK_YUKSEK", "KRITIK");
        priorityCombo.setValue("NORMAL");
        priorityCombo.setPromptText("Öncelik");
        grid.add(new Label("Öncelik:"), 0, 4);
        grid.add(priorityCombo, 1, 4);

        // Tarih alanları
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPromptText("Başlangıç Tarihi");
        grid.add(new Label("Başlangıç:"), 0, 5);
        grid.add(startDatePicker, 1, 5);
        
        // Başlangıç saati için ComboBox'lar
        HBox startTimeBox = new HBox(5);
        ComboBox<String> startHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            startHourCombo.getItems().add(String.format("%02d", i));
        }
        startHourCombo.setValue("00");
        
        ComboBox<String> startMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            startMinuteCombo.getItems().add(String.format("%02d", i));
        }
        startMinuteCombo.setValue("00");
        
        startTimeBox.getChildren().addAll(
            startHourCombo, 
            new Label(":"), 
            startMinuteCombo, 
            new Label("(saat:dakika)")
        );
        grid.add(startTimeBox, 2, 5);

        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusMonths(1));
        endDatePicker.setPromptText("Bitiş Tarihi");
        grid.add(new Label("Bitiş:"), 0, 6);
        grid.add(endDatePicker, 1, 6);
        
        // Bitiş saati için ComboBox'lar
        HBox endTimeBox = new HBox(5);
        ComboBox<String> endHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            endHourCombo.getItems().add(String.format("%02d", i));
        }
        endHourCombo.setValue("23");
        
        ComboBox<String> endMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            endMinuteCombo.getItems().add(String.format("%02d", i));
        }
        endMinuteCombo.setValue("59");
        
        endTimeBox.getChildren().addAll(
            endHourCombo, 
            new Label(":"), 
            endMinuteCombo, 
            new Label("(saat:dakika)")
        );
        grid.add(endTimeBox, 2, 6);

        // Seçenekler
        CheckBox activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setSelected(true);
        grid.add(activeCheckBox, 1, 7);

        CheckBox allowFeedbackCheckBox = new CheckBox("Geri Bildirime İzin Ver");
        allowFeedbackCheckBox.setSelected(true);
        grid.add(allowFeedbackCheckBox, 3, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(600);

        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
                    showAlert("Başlık ve içerik alanlarını doldurunuz.");
                    return null;
                }

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = startDatePicker.getValue() != null ? 
                    startDatePicker.getValue().atTime(
                        Integer.parseInt(startHourCombo.getValue()),
                        Integer.parseInt(startMinuteCombo.getValue()), 
                        0) : now;
                LocalDateTime endDate = endDatePicker.getValue() != null ? 
                    endDatePicker.getValue().atTime(
                        Integer.parseInt(endHourCombo.getValue()),
                        Integer.parseInt(endMinuteCombo.getValue()), 
                        0) : now.plusMonths(1);

                return new News(
                    (long) (newsList.size() + 1), // Gerçek uygulamada backend tarafından atanacak
                    titleField.getText(),
                    contentArea.getText(),
                    imagePathField.getText(),
                    startDate,
                    endDate,
                    activeCheckBox.isSelected(),
                    platformCombo.getValue(),
                    priorityCombo.getValue(),
                    typeCombo.getValue(),
                    0, // Yeni haber olduğu için görüntülenme sayısı 0
                    0, // Yeni haber olduğu için beğeni sayısı 0
                    allowFeedbackCheckBox.isSelected(),
                    now, // Oluşturma tarihi
                    now  // Güncelleme tarihi
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(news -> {
            // API'ye gönder
            try {
                // Görsel dosyasını oku (varsa)
                byte[] imageData = null;
                String imageName = "";
                if (news.getImage() != null && !news.getImage().isEmpty()) {
                    File imageFile = new File(news.getImage());
                    if (imageFile.exists()) {
                        imageName = imageFile.getName();
                        imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    }
                }
                
                // API çağrısı yap
                String response = ApiClientFX.createNews(
                    accessToken,
                    news.getTitle(),
                    news.getContent(),
                    imageData,
                    imageName,
                    news.getStartDate(),
                    news.getEndDate(),
                    news.getPlatform(),
                    news.getPriority(),
                    news.getType(),
                    news.isAllowFeedback()
                );
                
                // Başarılı yanıt alındıysa listeye ekle
                newsList.add(news);
                newsTable.getItems().add(news);
                showAlert("Haber başarıyla eklendi: " + news.getTitle());
                
                // Haberleri yeniden yükle (sunucudan güncel verileri almak için)
                loadNewsData();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Haber eklenirken bir hata oluştu: " + e.getMessage());
            }
        });
    }

    private void showEditNewsDialog(News news) {
        Dialog<News> dialog = new Dialog<>();
        dialog.setTitle("Haber Düzenle");
        dialog.setHeaderText("Haber bilgilerini güncelleyin");

        // "Güncelle" ve "İptal" butonları
        ButtonType updateButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Form alanları
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Başlık
        TextField titleField = new TextField(news.getTitle());
        titleField.setPromptText("Haber Başlığı");
        grid.add(new Label("Başlık:"), 0, 0);
        grid.add(titleField, 1, 0, 3, 1);

        // İçerik
        TextArea contentArea = new TextArea(news.getContent());
        contentArea.setPromptText("Haber İçeriği");
        contentArea.setPrefRowCount(5);
        grid.add(new Label("İçerik:"), 0, 1);
        grid.add(contentArea, 1, 1, 3, 1);

        // Görsel
        HBox imageBox = new HBox(10);
        TextField imagePathField = new TextField(news.getImage());
        imagePathField.setPromptText("Resim Yolu");
        imagePathField.setPrefWidth(300);
        Button browseButton = new Button("Gözat");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Haber Görseli Seç");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        imageBox.getChildren().addAll(imagePathField, browseButton);
        grid.add(new Label("Görsel:"), 0, 2);
        grid.add(imageBox, 1, 2, 3, 1);

        // Kategori
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("DUYURU", "KAMPANYA", "BAKIM", "BILGILENDIRME", 
                                   "GUNCELLEME", "UYARI", "ETKINLIK");
        typeCombo.setValue(news.getType());
        typeCombo.setPromptText("Haber Kategorisi");
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(typeCombo, 1, 3);

        // Platform
        ComboBox<String> platformCombo = new ComboBox<>();
        platformCombo.getItems().addAll("WEB", "MOBILE", "DESKTOP", "TABLET", "KIOSK", "ALL");
        platformCombo.setValue(news.getPlatform());
        platformCombo.setPromptText("Platform");
        grid.add(new Label("Platform:"), 2, 3);
        grid.add(platformCombo, 3, 3);

        // Öncelik
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("COK_DUSUK", "DUSUK", "NORMAL", 
                                       "ORTA_YUKSEK", "YUKSEK", "COK_YUKSEK", "KRITIK");
        priorityCombo.setValue(news.getPriority());
        priorityCombo.setPromptText("Öncelik");
        grid.add(new Label("Öncelik:"), 0, 4);
        grid.add(priorityCombo, 1, 4);

        // Tarih alanları
        DatePicker startDatePicker = new DatePicker(news.getStartDate() != null ? 
                                                   news.getStartDate().toLocalDate() : LocalDate.now());
        startDatePicker.setPromptText("Başlangıç Tarihi");
        grid.add(new Label("Başlangıç:"), 0, 5);
        grid.add(startDatePicker, 1, 5);
        
        // Başlangıç saati için ComboBox'lar
        HBox startTimeBox = new HBox(5);
        ComboBox<String> startHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            startHourCombo.getItems().add(String.format("%02d", i));
        }
        startHourCombo.setValue(news.getStartDate() != null ? 
                               String.format("%02d", news.getStartDate().getHour()) : "00");
        
        ComboBox<String> startMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            startMinuteCombo.getItems().add(String.format("%02d", i));
        }
        // En yakın 5'in katı dakikayı seçelim
        int startMinute = news.getStartDate() != null ? news.getStartDate().getMinute() : 0;
        startMinute = (startMinute / 5) * 5; // En yakın 5'in katına yuvarla
        startMinuteCombo.setValue(String.format("%02d", startMinute));
        
        startTimeBox.getChildren().addAll(
            startHourCombo, 
            new Label(":"), 
            startMinuteCombo, 
            new Label("(saat:dakika)")
        );
        grid.add(startTimeBox, 2, 5);

        DatePicker endDatePicker = new DatePicker(news.getEndDate() != null ? 
                                                 news.getEndDate().toLocalDate() : LocalDate.now().plusMonths(1));
        endDatePicker.setPromptText("Bitiş Tarihi");
        grid.add(new Label("Bitiş:"), 0, 6);
        grid.add(endDatePicker, 1, 6);
        
        // Bitiş saati için ComboBox'lar
        HBox endTimeBox = new HBox(5);
        ComboBox<String> endHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            endHourCombo.getItems().add(String.format("%02d", i));
        }
        endHourCombo.setValue(news.getEndDate() != null ? 
                             String.format("%02d", news.getEndDate().getHour()) : "23");
        
        ComboBox<String> endMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            endMinuteCombo.getItems().add(String.format("%02d", i));
        }
        // En yakın 5'in katı dakikayı seçelim
        int endMinute = news.getEndDate() != null ? news.getEndDate().getMinute() : 59;
        endMinute = (endMinute / 5) * 5; // En yakın 5'in katına yuvarla
        endMinuteCombo.setValue(String.format("%02d", endMinute));
        
        endTimeBox.getChildren().addAll(
            endHourCombo, 
            new Label(":"), 
            endMinuteCombo, 
            new Label("(saat:dakika)")
        );
        grid.add(endTimeBox, 2, 6);

        // Seçenekler
        CheckBox activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setSelected(news.isActive());
        grid.add(activeCheckBox, 1, 7);

        CheckBox allowFeedbackCheckBox = new CheckBox("Geri Bildirime İzin Ver");
        allowFeedbackCheckBox.setSelected(news.isAllowFeedback());
        grid.add(allowFeedbackCheckBox, 3, 7);

        // İstatistikler (salt okunur)
        Label viewsLabel = new Label("Görüntülenme: " + news.getViewCount());
        grid.add(viewsLabel, 1, 8);
        
        Label likesLabel = new Label("Beğeni: " + news.getLikeCount());
        grid.add(likesLabel, 3, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(600);

        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
                    showAlert("Başlık ve içerik alanlarını doldurunuz.");
                    return null;
                }

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = startDatePicker.getValue() != null ? 
                    startDatePicker.getValue().atTime(
                        Integer.parseInt(startHourCombo.getValue()),
                        Integer.parseInt(startMinuteCombo.getValue()), 
                        0) : news.getStartDate();
                LocalDateTime endDate = endDatePicker.getValue() != null ? 
                    endDatePicker.getValue().atTime(
                        Integer.parseInt(endHourCombo.getValue()),
                        Integer.parseInt(endMinuteCombo.getValue()), 
                        0) : news.getEndDate();

                return new News(
                    news.getId(),
                    titleField.getText(),
                    contentArea.getText(),
                    imagePathField.getText(),
                    startDate,
                    endDate,
                    activeCheckBox.isSelected(),
                    platformCombo.getValue(),
                    priorityCombo.getValue(),
                    typeCombo.getValue(),
                    news.getViewCount(),
                    news.getLikeCount(),
                    allowFeedbackCheckBox.isSelected(),
                    news.getCreatedAt(),
                    now // Güncelleme tarihi
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedNews -> {
            // API'ye gönder
            try {
                // Görsel dosyasını oku (varsa)
                byte[] imageData = null;
                String imageName = "";
                boolean imageChanged = false;
                
                if (updatedNews.getImage() != null && !updatedNews.getImage().isEmpty() && 
                    !updatedNews.getImage().equals(news.getImage())) {
                    File imageFile = new File(updatedNews.getImage());
                    if (imageFile.exists()) {
                        imageName = imageFile.getName();
                        imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                        imageChanged = true;
                    }
                }
                
                // Gönderilecek verileri konsola yazdır
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                String jsonData = "{\n" +
                    "  \"id\": " + updatedNews.getId() + ",\n" +
                    "  \"title\": \"" + updatedNews.getTitle() + "\",\n" +
                    "  \"content\": \"" + updatedNews.getContent().replace("\n", "\\n").replace("\"", "\\\"") + "\",\n" +
                    "  \"image\": " + (imageChanged ? "\"[Binary data: " + imageName + ", " + (imageData != null ? imageData.length : 0) + " bytes]\"" : "null") + ",\n" +
                    "  \"startDate\": \"" + (updatedNews.getStartDate() != null ? updatedNews.getStartDate().format(formatter) : "null") + "\",\n" +
                    "  \"endDate\": \"" + (updatedNews.getEndDate() != null ? updatedNews.getEndDate().format(formatter) : "null") + "\",\n" +
                    "  \"platform\": \"" + updatedNews.getPlatform() + "\",\n" +
                    "  \"priority\": \"" + updatedNews.getPriority() + "\",\n" +
                    "  \"type\": \"" + updatedNews.getType() + "\",\n" +
                    "  \"allowFeedback\": " + updatedNews.isAllowFeedback() + ",\n" +
                    "  \"active\": " + updatedNews.isActive() + "\n" +
                    "}";
                
                System.out.println("===== HABER GÜNCELLEME İSTEĞİ =====");
                System.out.println(jsonData);
                System.out.println("===================================");
                
                // API çağrısı yap
                String response = ApiClientFX.updateNews(
                    accessToken,
                    updatedNews.getId(),
                    updatedNews.getTitle(),
                    updatedNews.getContent(),
                    imageChanged ? imageData : null,  // Sadece değişmişse gönder
                    imageChanged ? imageName : "",    // Sadece değişmişse gönder
                    updatedNews.getStartDate(),
                    updatedNews.getEndDate(),
                    updatedNews.getPlatform(),
                    updatedNews.getPriority(),
                    updatedNews.getType(),
                    updatedNews.isAllowFeedback(),
                    updatedNews.isActive()
                );
                
                System.out.println("===== HABER GÜNCELLEME YANITI =====");
                System.out.println(response);
                System.out.println("===================================");
                
                // Başarılı yanıt alındıysa listeyi güncelle
                int index = -1;
                for (int i = 0; i < newsList.size(); i++) {
                    if (newsList.get(i).getId().equals(updatedNews.getId())) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    // Görsel değişmediyse eski görsel yolunu koru
                    if (!imageChanged) {
                        updatedNews = new News(
                            updatedNews.getId(),
                            updatedNews.getTitle(),
                            updatedNews.getContent(),
                            news.getImage(),  // Eski görsel yolunu koru
                            updatedNews.getStartDate(),
                            updatedNews.getEndDate(),
                            updatedNews.isActive(),
                            updatedNews.getPlatform(),
                            updatedNews.getPriority(),
                            updatedNews.getType(),
                            updatedNews.getViewCount(),
                            updatedNews.getLikeCount(),
                            updatedNews.isAllowFeedback(),
                            updatedNews.getCreatedAt(),
                            LocalDateTime.now()
                        );
                    }
                    
                    newsList.set(index, updatedNews);
                    newsTable.getItems().clear();
                    newsTable.getItems().addAll(newsList);
                    
                    showAlert("Haber başarıyla güncellendi: " + updatedNews.getTitle());
                } else {
                    showAlert("Haber güncellendi ancak listede bulunamadı.");
                }
                
                // Haberleri yeniden yükle (sunucudan güncel verileri almak için)
                loadNewsData();
            } catch (IOException e) {
                e.printStackTrace();
                String errorMsg = e.getMessage();
                
                // Hata mesajını daha kullanıcı dostu hale getir
                if (errorMsg.contains("NewsIsNotActiveException")) {
                    showAlert("Bu haber pasif durumda olduğu için güncellenemez.");
                } else if (errorMsg.contains("PhotoSizeLargerException")) {
                    showAlert("Görsel boyutu çok büyük. Lütfen daha küçük bir görsel seçin.");
                } else {
                    showAlert("Haber güncellenirken bir hata oluştu: " + errorMsg);
                }
            }
        });
    }

    private void showNewsDetailDialog(News news) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Haber Detayı");
        dialog.setHeaderText(null);

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(900);
        dialog.getDialogPane().setPrefHeight(700);
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

        // "Kapat" butonu
        ButtonType closeButtonType = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Ana içerik konteynerı
        BorderPane mainContent = new BorderPane();
        mainContent.setPadding(new Insets(20));

        // Başlık alanı
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        Label titleLabel = new Label(news.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        titleLabel.setWrapText(true);
        
        // Durum etiketi
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = new Label(news.getStatus());
        statusLabel.setPadding(new Insets(3, 10, 3, 10));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                           "-fx-background-radius: 15px; -fx-background-color: " + 
                           (news.isActive() ? "#2ecc71" : "#e74c3c") + ";");
        
        // Kategori etiketi
        Label typeLabel = new Label(news.getType());
        typeLabel.setPadding(new Insets(3, 10, 3, 10));
        typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                         "-fx-background-radius: 15px; -fx-background-color: #3498db;");
        
        // Platform etiketi
        Label platformLabel = new Label(news.getPlatform());
        platformLabel.setPadding(new Insets(3, 10, 3, 10));
        platformLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                             "-fx-background-radius: 15px; -fx-background-color: #9b59b6;");
        
        // Öncelik etiketi
        Label priorityLabel = new Label(news.getPriority());
        priorityLabel.setPadding(new Insets(3, 10, 3, 10));
        priorityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                             "-fx-background-radius: 15px; -fx-background-color: #f39c12;");
        
        statusBox.getChildren().addAll(statusLabel, typeLabel, platformLabel, priorityLabel);
        
        // Tarih bilgileri
        HBox dateBox = new HBox(20);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        
        // Oluşturma tarihi
        VBox createdBox = createDateInfoBox("Oluşturulma", news.getCreatedAtString(), "#3498db");
        
        // Güncelleme tarihi
        VBox updatedBox = createDateInfoBox("Son Güncelleme", news.getUpdatedAtString(), "#2ecc71");
        
        // Başlangıç tarihi
        VBox startBox = createDateInfoBox("Başlangıç", news.getStartDateString(), "#f39c12");
        
        // Bitiş tarihi
        VBox endBox = createDateInfoBox("Bitiş", news.getEndDateString(), "#e74c3c");
        
        dateBox.getChildren().addAll(createdBox, updatedBox, startBox, endBox);
        
        headerBox.getChildren().addAll(titleLabel, statusBox, new Separator(), dateBox);
        
        // İstatistikler kartı
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        // Görüntülenme sayısı
        VBox viewsBox = createStatBox("Görüntülenme", String.valueOf(news.getViewCount()), "#3498db", "👁");
        
        // Beğeni sayısı
        VBox likesBox = createStatBox("Beğeni", String.valueOf(news.getLikeCount()), "#e74c3c", "❤");
        
        // Geri bildirim durumu
        VBox feedbackBox = createStatBox("Geri Bildirim", news.isAllowFeedback() ? "Açık" : "Kapalı", 
                                      "#2ecc71", news.isAllowFeedback() ? "✓" : "✗");
        
        statsBox.getChildren().addAll(viewsBox, likesBox, feedbackBox);
        
        // Ana içerik alanı
        TabPane contentTabPane = new TabPane();
        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // İçerik sekmesi
        Tab contentTab = new Tab("İçerik");
        
        // İçerik alanı
        TextArea contentArea = new TextArea(news.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(15);
        contentArea.setStyle("-fx-font-size: 14px; -fx-background-color: white;");
        
        // İçerik paneli
        VBox contentPanel = new VBox(10);
        contentPanel.setPadding(new Insets(15));
        contentPanel.getChildren().add(contentArea);
        
        contentTab.setContent(contentPanel);
        
        // Görsel sekmesi
        Tab imageTab = new Tab("Görsel");
        
        // Görsel içeriği
        VBox imageBox = new VBox(10);
        imageBox.setPadding(new Insets(15));
        imageBox.setAlignment(Pos.CENTER);
        
        if (news.getImage() != null && !news.getImage().isEmpty()) {
            try {
                System.out.println("Görsel yolu: " + news.getImage());
                
                // Görsel yükleme işlemi
                javafx.scene.image.Image image = null;
                final String imageSource;
                final String imageUrl;
                
                // Görsel URL'sini hazırla
                if (news.getImage().toLowerCase().startsWith("http")) {
                    // Doğrudan URL
                    imageUrl = news.getImage();
                    imageSource = "URL";
                } else {
                    // API sunucusundan görsel
                    String baseUrl = "http://localhost:8080"; // API sunucunuzun base URL'i
                    imageUrl = news.getImage().startsWith("/") ? 
                               baseUrl + news.getImage() : 
                               baseUrl + "/" + news.getImage();
                    imageSource = "API";
                }
                
                System.out.println("Görsel URL: " + imageUrl);
                
                // Görsel önizleme alanı
                BorderPane imagePreviewPane = new BorderPane();
                imagePreviewPane.setPrefWidth(600);
                imagePreviewPane.setPrefHeight(400);
                imagePreviewPane.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; " +
                                        "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 10px;");
                
                // Yükleniyor göstergesi
                Label loadingLabel = new Label("Görsel yükleniyor...");
                loadingLabel.setStyle("-fx-font-size: 14px;");
                ProgressBar loadingBar = new ProgressBar();
                loadingBar.setPrefWidth(200);
                VBox loadingBox = new VBox(10, loadingLabel, loadingBar);
                loadingBox.setAlignment(Pos.CENTER);
                imagePreviewPane.setCenter(loadingBox);
                
                // Görsel bilgi etiketi
                Label imageInfoLabel = new Label("Kaynak: " + imageSource + " - " + news.getImage());
                imageInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                imagePreviewPane.setBottom(imageInfoLabel);
                BorderPane.setAlignment(imageInfoLabel, Pos.CENTER);
                BorderPane.setMargin(imageInfoLabel, new Insets(10, 0, 0, 0));
                
                // Görsel yükleme işlemi için ayrı bir thread
                Thread imageLoadingThread = new Thread(() -> {
                    try {
                        // Görsel yükleme
                        javafx.scene.image.Image loadedImage = null;
                        
                        if (imageUrl.toLowerCase().startsWith("http")) {
                            // URL'den yükleme
                            loadedImage = new javafx.scene.image.Image(imageUrl, true); // background loading
                        } else {
                            // Yerel dosya
                            File imageFile = new File(imageUrl);
                            if (imageFile.exists() && imageFile.isFile()) {
                                loadedImage = new javafx.scene.image.Image(imageFile.toURI().toString());
                            }
                        }
                        
                        // Yüklenen görsel
                        final javafx.scene.image.Image finalImage = loadedImage;
                        
                        // UI thread'inde görüntüleme
                        javafx.application.Platform.runLater(() -> {
                            if (finalImage != null && !finalImage.isError()) {
                                // Görsel başarıyla yüklendi
                                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(finalImage);
                                imageView.setPreserveRatio(true);
                                
                                // Görsel boyutunu ayarla - daha büyük görsel için
                                if (finalImage.getWidth() > 600) {
                                    imageView.setFitWidth(600);
                                } else {
                                    imageView.setFitWidth(finalImage.getWidth());
                                }
                                
                                // Tam boyut görüntüleme için tıklama olayı ekle
                                imageView.setOnMouseClicked(mouseEvent -> {
                                    showFullSizeImage(finalImage, imageUrl);
                                });
                                // İmleç stilini el şekline değiştir (tıklanabilir görünüm)
                                imageView.setCursor(javafx.scene.Cursor.HAND);
                                
                                // Görsel çerçevesi
                                BorderPane imageFrame = new BorderPane(imageView);
                                imageFrame.setStyle("-fx-background-color: white; -fx-padding: 5px;");
                                
                                // Tam boyutta görüntüleme bilgisi ekle
                                Label viewFullSizeLabel = new Label("Tam boyutta görmek için görsele tıklayın");
                                viewFullSizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-style: italic;");
                                imageFrame.setBottom(viewFullSizeLabel);
                                BorderPane.setAlignment(viewFullSizeLabel, Pos.CENTER);
                                
                                // Görsel bilgisi güncelle
                                String dimensions = String.format("%.0fx%.0f", finalImage.getWidth(), finalImage.getHeight());
                                imageInfoLabel.setText("Kaynak: " + imageSource + " - " + dimensions + " - " + news.getImage());
                                
                                // Görsel panelini güncelle
                                imagePreviewPane.setCenter(imageFrame);
                                
                                System.out.println("Görsel başarıyla yüklendi: " + imageUrl);
                            } else {
                                // Görsel yüklenemedi
                                String errorMsg = "Görsel yüklenemedi";
                                System.err.println(errorMsg + ": " + imageUrl);
                                
                                // Hata mesajı
                                Label errorLabel = new Label(errorMsg);
                                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                                
                                // Tarayıcıda görüntüleme butonu
                                Button openBrowserButton = new Button("Tarayıcıda Görüntüle");
                                openBrowserButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                                openBrowserButton.setOnAction(e -> {
                                    try {
                                        hostServices.showDocument(imageUrl);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        showAlert("Tarayıcıda açılamadı: " + ex.getMessage());
                                    }
                                });
                                
                                // Hata paneli
                                VBox errorBox = new VBox(10, errorLabel, openBrowserButton);
                                errorBox.setAlignment(Pos.CENTER);
                                imagePreviewPane.setCenter(errorBox);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        
                        // UI thread'inde hata gösterimi
                        javafx.application.Platform.runLater(() -> {
                            Label errorLabel = new Label("Görsel yüklenirken hata oluştu");
                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                            
                            TextArea errorDetails = new TextArea(e.toString());
                            errorDetails.setEditable(false);
                            errorDetails.setPrefRowCount(3);
                            errorDetails.setWrapText(true);
                            
                            VBox errorBox = new VBox(10, errorLabel, errorDetails);
                            errorBox.setAlignment(Pos.CENTER);
                            imagePreviewPane.setCenter(errorBox);
                        });
                    }
                });
                
                // Görsel yükleme thread'ini başlat
                imageLoadingThread.setDaemon(true);
                imageLoadingThread.start();
                
                // Görsel panelini ekle
                imageBox.getChildren().add(imagePreviewPane);
                
            } catch (Exception e) {
                e.printStackTrace();
                Label errorLabel = new Label("Görsel işlenirken hata oluştu: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red;");
                imageBox.getChildren().add(errorLabel);
            }
        } else {
            // Görsel yok
            Label noImageLabel = new Label("Bu haberde görsel bulunmuyor");
            noImageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            
            // Görsel yok ikonu
            javafx.scene.layout.StackPane noImageIcon = new javafx.scene.layout.StackPane();
            noImageIcon.setMinSize(100, 100);
            noImageIcon.setMaxSize(100, 100);
            noImageIcon.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                               "-fx-border-width: 1px; -fx-border-radius: 50%;");
            
            Label iconLabel = new Label("?");
            iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #adb5bd;");
            noImageIcon.getChildren().add(iconLabel);
            
            VBox noImageBox = new VBox(15, noImageIcon, noImageLabel);
            noImageBox.setAlignment(Pos.CENTER);
            imageBox.getChildren().add(noImageBox);
        }
        
        imageTab.setContent(imageBox);
        
        // Sekmeleri ekle
        contentTabPane.getTabs().addAll(contentTab, imageTab);
        
        // İçeriği yerleştir
        VBox centerContent = new VBox(15);
        centerContent.getChildren().addAll(headerBox, statsBox, contentTabPane);
        mainContent.setCenter(centerContent);
        
        dialog.getDialogPane().setContent(mainContent);
        dialog.showAndWait();
    }
    
    // Tarih bilgi kutusu oluşturan yardımcı metot
    private VBox createDateInfoBox(String title, String date, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(titleLabel, dateLabel);
        return box;
    }
    
    // İstatistik kutusu oluşturan yardımcı metot
    private VBox createStatBox(String title, String value, String color, String icon) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: " + color + ";");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        box.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return box;
    }

    private void showDeleteConfirmation(News news) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Haberi Pasif Yap");
        alert.setHeaderText("Haber Pasif Yapma Onayı");
        alert.setContentText("\"" + news.getTitle() + "\" başlıklı haberi pasif duruma getirmek istediğinize emin misiniz? Bu işlem haberi tamamen silmez, sadece görünürlüğünü kapatır.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Soft delete işlemi yap
                softDeleteNews(news.getId());
            }
        });
    }

    private void softDeleteNews(Long newsId) {
        try {
            // API çağrısı yap
            String response = ApiClientFX.softDeleteNews(accessToken, newsId);
            
            // Başarılı yanıt alındıysa haberin aktiflik durumunu güncelle
            for (News news : newsList) {
                if (news.getId().equals(newsId)) {
                    // Haberi listeden silmek yerine aktiflik durumunu değiştir
                    News updatedNews = new News(
                        news.getId(),
                        news.getTitle(),
                        news.getContent(),
                        news.getImage(),
                        news.getStartDate(),
                        news.getEndDate(),
                        false, // Aktifliği kapat
                        news.getPlatform(),
                        news.getPriority(),
                        news.getType(),
                        news.getViewCount(),
                        news.getLikeCount(),
                        news.isAllowFeedback(),
                        news.getCreatedAt(),
                        LocalDateTime.now() // Güncelleme tarihi
                    );
                    
                    int index = newsList.indexOf(news);
                    if (index >= 0) {
                        newsList.set(index, updatedNews);
                        newsTable.getItems().clear();
                        newsTable.getItems().addAll(newsList);
                    }
                    
                    showAlert("Haber başarıyla pasif duruma getirildi: " + news.getTitle());
                    break;
                }
            }
            
            // Haberleri yeniden yükle (sunucudan güncel verileri almak için)
            loadNewsData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Haber pasif duruma getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    private void showNewsStatistics() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Haber İstatistikleri");
        dialog.setHeaderText(null);

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(600);
        // Stil dosyası varsa ekle
        try {
            if (getClass().getResource("style.css") != null) {
                dialog.getDialogPane().getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Stil dosyası yüklenemedi: " + e.getMessage());
        }
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

        // "Kapat" butonu
        ButtonType closeButtonType = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Ana içerik konteynerı
        BorderPane mainContent = new BorderPane();
        mainContent.setPadding(new Insets(20));

        // Başlık
        Label titleLabel = new Label("Haber İstatistikleri Paneli");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 20, 0));
        mainContent.setTop(titleBox);

        // Özet istatistikler için kart görünümü
        HBox summaryCards = new HBox(20);
        summaryCards.setAlignment(Pos.CENTER);

        // Toplam haber kartı
        VBox totalNewsCard = createStatCard("Toplam Haber", 
                                          String.valueOf(newsList.size()), 
                                          "#3498db");
        
        // Aktif haber kartı
        long activeCount = newsList.stream().filter(News::isActive).count();
        VBox activeNewsCard = createStatCard("Aktif Haber", 
                                           String.valueOf(activeCount), 
                                           "#2ecc71");
        
        // Pasif haber kartı
        VBox inactiveNewsCard = createStatCard("Pasif Haber", 
                                             String.valueOf(newsList.size() - activeCount), 
                                             "#e74c3c");
        
        // Toplam görüntülenme kartı
        int totalViews = newsList.stream().mapToInt(News::getViewCount).sum();
        VBox viewsCard = createStatCard("Toplam Görüntülenme", 
                                      String.format("%,d", totalViews), 
                                      "#9b59b6");
        
        // Toplam beğeni kartı
        int totalLikes = newsList.stream().mapToInt(News::getLikeCount).sum();
        VBox likesCard = createStatCard("Toplam Beğeni", 
                                      String.format("%,d", totalLikes), 
                                      "#f39c12");

        summaryCards.getChildren().addAll(totalNewsCard, activeNewsCard, inactiveNewsCard, 
                                         viewsCard, likesCard);

        // Ana içerik alanı - grafik ve tablolar
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Kategori dağılımı sekmesi
        Tab categoryTab = new Tab("Kategori Dağılımı");
        categoryTab.setContent(createDistributionTab("Kategori", 
                                                    collectCategoryData(),
                                                    "#3498db"));
        
        // Platform dağılımı sekmesi
        Tab platformTab = new Tab("Platform Dağılımı");
        platformTab.setContent(createDistributionTab("Platform", 
                                                    collectPlatformData(),
                                                    "#2ecc71"));
        
        // Öncelik dağılımı sekmesi
        Tab priorityTab = new Tab("Öncelik Dağılımı");
        priorityTab.setContent(createDistributionTab("Öncelik", 
                                                    collectPriorityData(),
                                                    "#e74c3c"));
        
        // En çok görüntülenen/beğenilen haberler sekmesi
        Tab topNewsTab = new Tab("En Popüler Haberler");
        topNewsTab.setContent(createTopNewsTab());
        
        tabPane.getTabs().addAll(categoryTab, platformTab, priorityTab, topNewsTab);
        
        // İçeriği yerleştir
        VBox centerContent = new VBox(20);
        centerContent.getChildren().addAll(summaryCards, tabPane);
        mainContent.setCenter(centerContent);
        
        dialog.getDialogPane().setContent(mainContent);
        dialog.showAndWait();
    }
    
    // İstatistik kartı oluşturan yardımcı metot
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    // Dağılım verilerini toplayan yardımcı metotlar
    private HashMap<String, Integer> collectCategoryData() {
        HashMap<String, Integer> data = new HashMap<>();
        for (String category : List.of("DUYURU", "KAMPANYA", "BAKIM", "BILGILENDIRME", 
                                      "GUNCELLEME", "UYARI", "ETKINLIK")) {
            int count = (int) newsList.stream()
                .filter(n -> n.getType().equals(category))
                .count();
            data.put(category, count);
        }
        return data;
    }
    
    private HashMap<String, Integer> collectPlatformData() {
        HashMap<String, Integer> data = new HashMap<>();
        for (String platform : List.of("WEB", "MOBILE", "DESKTOP", "TABLET", "KIOSK", "ALL")) {
            int count = (int) newsList.stream()
                .filter(n -> n.getPlatform().equals(platform))
                .count();
            data.put(platform, count);
        }
        return data;
    }
    
    private HashMap<String, Integer> collectPriorityData() {
        HashMap<String, Integer> data = new HashMap<>();
        for (String priority : List.of("COK_DUSUK", "DUSUK", "NORMAL", 
                                     "ORTA_YUKSEK", "YUKSEK", "COK_YUKSEK", "KRITIK")) {
            int count = (int) newsList.stream()
                .filter(n -> n.getPriority().equals(priority))
                .count();
            data.put(priority, count);
        }
        return data;
    }
    
    // Dağılım sekmesi oluşturan yardımcı metot
    private VBox createDistributionTab(String title, HashMap<String, Integer> data, String color) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Başlık
        Label titleLabel = new Label(title + " Dağılımı");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Maksimum değeri bul (progress bar'lar için)
        int maxValue = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        // Dağılım grafiği
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(10));
        chartBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        
        // Verileri sırala (değere göre azalan)
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(data.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            String key = entry.getKey();
            int value = entry.getValue();
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            // Etiket
            Label keyLabel = new Label(key);
            keyLabel.setPrefWidth(120);
            keyLabel.setStyle("-fx-font-size: 14px;");
            
            // Progress bar
            ProgressBar progressBar = new ProgressBar((double) value / maxValue);
            progressBar.setPrefWidth(300);
            progressBar.setStyle("-fx-accent: " + color + ";");
            HBox.setHgrow(progressBar, Priority.ALWAYS);
            
            // Değer
            Label valueLabel = new Label(String.valueOf(value));
            valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            valueLabel.setPrefWidth(50);
            valueLabel.setAlignment(Pos.CENTER_RIGHT);
            
            row.getChildren().addAll(keyLabel, progressBar, valueLabel);
            chartBox.getChildren().add(row);
        }
        
        content.getChildren().addAll(titleLabel, chartBox);
        return content;
    }
    
    // En popüler haberler sekmesini oluşturan yardımcı metot
    private VBox createTopNewsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // En çok görüntülenen haberler
        Label mostViewedTitle = new Label("En Çok Görüntülenen Haberler");
        mostViewedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<News> mostViewedTable = new TableView<>();
        mostViewedTable.setPrefHeight(200);
        
        TableColumn<News, String> titleCol1 = new TableColumn<>("Başlık");
        titleCol1.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol1.setPrefWidth(300);
        
        TableColumn<News, Integer> viewsCol = new TableColumn<>("Görüntülenme");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
        viewsCol.setPrefWidth(120);
        
        mostViewedTable.getColumns().addAll(titleCol1, viewsCol);
        
        // Verileri görüntülenme sayısına göre sırala
        List<News> mostViewed = newsList.stream()
            .sorted((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()))
            .limit(5)
            .collect(Collectors.toList());
        
        mostViewedTable.getItems().addAll(mostViewed);
        
        // En çok beğenilen haberler
        Label mostLikedTitle = new Label("En Çok Beğenilen Haberler");
        mostLikedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<News> mostLikedTable = new TableView<>();
        mostLikedTable.setPrefHeight(200);
        
        TableColumn<News, String> titleCol2 = new TableColumn<>("Başlık");
        titleCol2.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol2.setPrefWidth(300);
        
        TableColumn<News, Integer> likesCol = new TableColumn<>("Beğeni");
        likesCol.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        likesCol.setPrefWidth(120);
        
        mostLikedTable.getColumns().addAll(titleCol2, likesCol);
        
        // Verileri beğeni sayısına göre sırala
        List<News> mostLiked = newsList.stream()
            .sorted((a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()))
            .limit(5)
            .collect(Collectors.toList());
        
        mostLikedTable.getItems().addAll(mostLiked);
        
        content.getChildren().addAll(mostViewedTitle, mostViewedTable, 
                                    new Separator(), 
                                    mostLikedTitle, mostLikedTable);
        
        return content;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Görseli tam boyutta gösteren yardımcı metod
     */
    private void showFullSizeImage(Image image, String imageUrl) {
        // Yeni bir dialog oluştur
        Dialog<Void> fullSizeDialog = new Dialog<>();
        fullSizeDialog.setTitle("Tam Boyut Görüntüleyici");
        fullSizeDialog.setHeaderText(null);
        
        // Dialog boyutunu ekran boyutuna göre ayarla - ekranın %90'ı kadar
        Screen screen = Screen.getPrimary();
        double maxWidth = screen.getBounds().getWidth() * 0.9;
        double maxHeight = screen.getBounds().getHeight() * 0.9;
        
        fullSizeDialog.getDialogPane().setPrefWidth(Math.min(image.getWidth() + 40, maxWidth));
        fullSizeDialog.getDialogPane().setPrefHeight(Math.min(image.getHeight() + 100, maxHeight));
        
        // "Kapat" butonu
        ButtonType closeButtonType = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
        fullSizeDialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Görsel görüntüleyici
        ImageView fullImageView = new ImageView(image);
        fullImageView.setPreserveRatio(true);
        
        // Scrollable alan oluştur (görselin çok büyük olması durumunda)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(fullImageView);
        scrollPane.setPannable(true); // Fare ile kaydırma
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        // Zoom kontrolleri
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");
        Button resetZoomButton = new Button("Sıfırla");
        Button saveToDiskButton = new Button("Kaydet");
        
        // Boyut oranı
        double[] scaleValue = {1.0}; // Array kullanarak closure içinden değiştirebilme
        
        // Zoom event handlers
        zoomInButton.setOnAction(e -> {
            scaleValue[0] *= 1.2; // %20 büyüt
            updateImageScale(fullImageView, scaleValue[0]);
        });
        
        zoomOutButton.setOnAction(e -> {
            scaleValue[0] /= 1.2; // %20 küçült
            updateImageScale(fullImageView, scaleValue[0]);
        });
        
        resetZoomButton.setOnAction(e -> {
            scaleValue[0] = 1.0; // orijinal boyut
            updateImageScale(fullImageView, scaleValue[0]);
        });
        
        // Kaydetme butonu
        saveToDiskButton.setOnAction(e -> {
            saveImageToDisk(image, imageUrl);
        });
        
        // Buton stillerini ayarla
        String buttonStyle = "-fx-font-weight: bold; -fx-min-width: 40px;";
        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        resetZoomButton.setStyle("-fx-min-width: 80px;");
        saveToDiskButton.setStyle("-fx-min-width: 80px;");
        
        // Butonları bir araya getir
        HBox controlBox = new HBox(10, zoomOutButton, resetZoomButton, zoomInButton, saveToDiskButton);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10, 0, 10, 0));
        
        // Bilgi etiketi - görselin boyutu
        Label infoLabel = new Label(String.format("Orijinal boyut: %.0fx%.0f piksel", 
            image.getWidth(), image.getHeight()));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Tüm içeriği bir araya getir
        VBox mainContent = new VBox(10, scrollPane, controlBox, infoLabel);
        mainContent.setPadding(new Insets(10));
        
        fullSizeDialog.getDialogPane().setContent(mainContent);
        
        // Modal olmayan şekilde göster (arka planda gösterim)
        // Dialog'un stage'ini alıp modal özelliğini değiştir
        Stage stage = (Stage) fullSizeDialog.getDialogPane().getScene().getWindow();
        stage.setResizable(true);
        
        // Dialog'u göster
        fullSizeDialog.showAndWait();
    }
    
    /**
     * Görselin ölçeklemesini günceller
     */
    private void updateImageScale(ImageView imageView, double scale) {
        // Ölçeği güncelle
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
    }
    
    /**
     * Görseli diske kaydet
     */
    private void saveImageToDisk(Image image, String imageUrl) {
        // Dosya seçim dialogu oluştur
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Görseli Kaydet");
        
        // Varsayılan dosya adı
        String filename = "image";
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // URL'den dosya adını çıkar
            int lastSlash = imageUrl.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
                filename = imageUrl.substring(lastSlash + 1);
                // URL parametrelerini temizle
                int queryIndex = filename.indexOf('?');
                if (queryIndex > 0) {
                    filename = filename.substring(0, queryIndex);
                }
            }
        }
        
        fileChooser.setInitialFileName(filename);
        
        // Filtreleri ayarla
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Dosyası", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Dosyası", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Tüm Dosyalar", "*.*")
        );
        
        // Dosya seçim dialogunu göster
        Window window = null; // Dialog'un sahibi yok
        File file = fileChooser.showSaveDialog(window);
        
        if (file != null) {
            try {
                // Görseli kaydet
                saveToFile(image, file);
                showAlert("Görsel başarıyla kaydedildi: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Görsel kaydedilirken hata oluştu: " + e.getMessage());
            }
        }
    }
    
    /**
     * JavaFX Image nesnesini dosyaya kaydetme yardımcı metodu
     */
    private static void saveToFile(Image image, File file) throws IOException {
        // Görsel formatını belirle
        String extension = getFileExtension(file.getName()).toLowerCase();
        
        // JavaFX'te görsel kaydetme işlemi için basit bir çözüm
        // Not: Bu temel bir implementasyon, daha gelişmiş özellikler için
        // SwingFXUtils.fromFXImage() ve ImageIO kullanılabilir
        
        // Şimdilik kullanıcıya bilgi verelim
        throw new IOException("Görsel kaydetme özelliği henüz desteklenmiyor. " +
                            "Lütfen görseli manuel olarak kaydedin.");
    }
    
    /**
     * Dosya uzantısını döndürür
     */
    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}