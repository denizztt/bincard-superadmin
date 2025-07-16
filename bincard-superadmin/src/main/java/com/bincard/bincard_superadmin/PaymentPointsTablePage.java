package com.bincard.bincard_superadmin;

import com.bincard.bincard_superadmin.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.Scene;

public class PaymentPointsTablePage extends SuperadminPageBase {

    // paymentPointsList erişimini public yap
    public List<PaymentPoint> paymentPointsList;
    private TableView<PaymentPoint> paymentPointsTable;
    private TextField searchField;
    private ComboBox<String> cityFilter;
    private ComboBox<String> paymentMethodFilter;
    private ComboBox<String> statusFilter;
    // private HostServices hostServices;
    
    // Sayfalama için değişkenler
    private int currentPage = 0;
    private final int pageSize = 10; // Sabit 10 kayıt per sayfa
    private int totalPages = 0;
    private int totalElements = 0;
    private Button previousButton;
    private Button nextButton;
    private Button firstPageButton;
    private Button lastPageButton;
    private Label pageInfoLabel;

    public PaymentPointsTablePage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktaları - Tablo Görünümü");
        // this.hostServices = hostServices;
        this.paymentPointsList = new ArrayList<>();
        
        System.out.println("PaymentPointsTablePage constructor başlıyor...");
        System.out.println("paymentPointsList başlangıç durumu: " + (paymentPointsList == null ? "null" : "empty list"));
        
        // Sayfalama bilgilerini başlat
        totalElements = 0;
        totalPages = 0;
        
        // API'den veri yüklemeye çalış
        loadPaymentPointsData();
        
        System.out.println("PaymentPointsTablePage constructor tamamlandı.");
    }

    @Override
    protected Node createContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        Label titleLabel = new Label("Ödeme Noktaları Yönetimi");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Filtreler ve arama
        Node filtersSection = createFiltersSection();
        
        // Tablo
        paymentPointsTable = createPaymentPointsTable();
        
        // Sayfalama kontrolleri
        HBox paginationControls = createPaginationControls();
        
        // Alt kontroller
        HBox bottomControls = createBottomControls();

        mainContent.getChildren().addAll(titleLabel, filtersSection, paymentPointsTable, paginationControls, bottomControls);
        
        // Tablo verileri güncelle
        updateTableData();
        
        return new ScrollPane(mainContent);
    }

    private Node createFiltersSection() {
        VBox filtersBox = new VBox(15);
        filtersBox.setPadding(new Insets(15));
        filtersBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label filtersTitle = new Label("Filtreleme ve Arama");
        filtersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // Üst satır - Arama ve şehir filtresi
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Arama kutusu
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Arama:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        searchField = new TextField();
        searchField.setPromptText("Ödeme noktası adı ile ara...");
        searchField.setPrefWidth(250);
        searchField.setOnKeyReleased(e -> filterPaymentPoints());
        
        searchBox.getChildren().addAll(searchLabel, searchField);

        // Şehir filtresi
        VBox cityBox = new VBox(5);
        Label cityLabel = new Label("Şehir:");
        cityLabel.setStyle("-fx-font-weight: bold;");
        
        cityFilter = new ComboBox<>();
        cityFilter.getItems().addAll("Tümü", "İstanbul", "Ankara", "İzmir", "Bursa", "Antalya");
        cityFilter.setValue("Tümü");
        cityFilter.setPrefWidth(150);
        cityFilter.setOnAction(e -> filterPaymentPoints());
        
        cityBox.getChildren().addAll(cityLabel, cityFilter);

        topRow.getChildren().addAll(searchBox, cityBox);

        // Alt satır - Ödeme yöntemi ve durum filtreleri
        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // Ödeme yöntemi filtresi
        VBox paymentMethodBox = new VBox(5);
        Label paymentMethodLabel = new Label("Ödeme Yöntemi:");
        paymentMethodLabel.setStyle("-fx-font-weight: bold;");
        
        paymentMethodFilter = new ComboBox<>();
        paymentMethodFilter.getItems().addAll("Tümü", "CASH", "CREDIT_CARD", "DEBIT_CARD", "MOBILE_APP", "QR_CODE");
        paymentMethodFilter.setValue("Tümü");
        paymentMethodFilter.setPrefWidth(150);
        paymentMethodFilter.setOnAction(e -> filterPaymentPoints());
        
        paymentMethodBox.getChildren().addAll(paymentMethodLabel, paymentMethodFilter);

        // Durum filtresi
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Durum:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "Aktif", "Pasif");
        statusFilter.setValue("Tümü");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> filterPaymentPoints());
        
        statusBox.getChildren().addAll(statusLabel, statusFilter);

        // Temizle butonu
        Button clearFiltersButton = new Button("Filtreleri Temizle");
        clearFiltersButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5;");
        clearFiltersButton.setOnAction(e -> clearFilters());

        bottomRow.getChildren().addAll(paymentMethodBox, statusBox, clearFiltersButton);

        filtersBox.getChildren().addAll(filtersTitle, topRow, bottomRow);
        return filtersBox;
    }

    private TableView<PaymentPoint> createPaymentPointsTable() {
        TableView<PaymentPoint> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        // ID sütunu
        TableColumn<PaymentPoint, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(60);

        // Ad sütunu
        TableColumn<PaymentPoint, String> nameColumn = new TableColumn<>("Ödeme Noktası Adı");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        // Adres sütunu
        TableColumn<PaymentPoint, String> addressColumn = new TableColumn<>("Adres");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("fullAddress"));
        addressColumn.setPrefWidth(250);

        // Şehir sütunu
        TableColumn<PaymentPoint, String> cityColumn = new TableColumn<>("Şehir");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityColumn.setPrefWidth(100);

        // Telefon sütunu
        TableColumn<PaymentPoint, String> phoneColumn = new TableColumn<>("Telefon");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        phoneColumn.setPrefWidth(120);

        // Çalışma saatleri sütunu
        TableColumn<PaymentPoint, String> hoursColumn = new TableColumn<>("Çalışma Saatleri");
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        hoursColumn.setPrefWidth(130);

        // Ödeme yöntemleri sütunu
        TableColumn<PaymentPoint, String> paymentMethodsColumn = new TableColumn<>("Ödeme Yöntemleri");
        paymentMethodsColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethodsString"));
        paymentMethodsColumn.setPrefWidth(150);

        // Durum sütunu
        TableColumn<PaymentPoint, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        statusColumn.setPrefWidth(80);

        // Oluşturma tarihi sütunu
        TableColumn<PaymentPoint, String> createdDateColumn = new TableColumn<>("Oluşturma Tarihi");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtString"));
        createdDateColumn.setPrefWidth(150);

        // Sütunları ekle
        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(addressColumn);
        table.getColumns().add(cityColumn);
        table.getColumns().add(phoneColumn);
        table.getColumns().add(hoursColumn);
        table.getColumns().add(paymentMethodsColumn);
        table.getColumns().add(statusColumn);
        table.getColumns().add(createdDateColumn);

        // Satır çift tıklama olayı - detay görüntüleme
        table.setRowFactory(tv -> {
            TableRow<PaymentPoint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    PaymentPoint selectedPoint = row.getItem();
                    // Detaylı bilgi için API'den tek ödeme noktasını al
                    loadAndShowPaymentPointDetail(selectedPoint.getId());
                }
            });
            return row;
        });

        return table;
    }

    private HBox createBottomControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        Button addButton = new Button("Yeni Ödeme Noktası");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        addButton.setOnAction(e -> showAddPaymentPointDialog());

        Button editButton = new Button("Düzenle");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        editButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditPaymentPointDialog(selected);
            } else {
                showAlert("Lütfen düzenlemek için bir ödeme noktası seçin.");
            }
        });

        Button toggleStatusButton = new Button("Durumu Değiştir");
        toggleStatusButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5;");
        toggleStatusButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                togglePaymentPointStatus(selected);
            } else {
                showAlert("Lütfen durumunu değiştirmek için bir ödeme noktası seçin.");
            }
        });

        Button deleteButton = new Button("Sil");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> {
            PaymentPoint selected = paymentPointsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeleteConfirmation(selected);
            } else {
                showAlert("Lütfen silmek için bir ödeme noktası seçin.");
            }
        });

        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> loadPaymentPointsData());

        // Haritada Göster butonu
        Button mapButton = new Button("Haritada Göster");
        mapButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-background-radius: 5;");
        mapButton.setOnAction(e -> {
            javafx.application.Platform.runLater(() -> {
                PaymentPointsMapPage.showMap(stage, paymentPointsList);
            });
        });

        // Haritada Tümünü Göster butonu
        Button mapAllButton = new Button("Haritada Tümünü Göster");
        mapAllButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
        mapAllButton.setOnAction(e -> {
            javafx.application.Platform.runLater(() -> {
                PaymentPointsMapPage.showMap(stage, paymentPointsList);
            });
        });

        controls.getChildren().addAll(addButton, editButton, toggleStatusButton, deleteButton, refreshButton, mapButton, mapAllButton);
        return controls;
    }

    private HBox createPaginationControls() {
        HBox paginationBox = new HBox(15);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(10));
        paginationBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Önceki sayfa butonu
        previousButton = new Button("◀ Önceki");
        previousButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        previousButton.setOnAction(e -> goToPreviousPage());
        previousButton.setDisable(true); // Başlangıçta ilk sayfadayız

        // Sayfa bilgisi
        pageInfoLabel = new Label("Sayfa 1 (Veri yükleniyor...)");
        pageInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Sonraki sayfa butonu
        nextButton = new Button("Sonraki ▶");
        nextButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        nextButton.setOnAction(e -> goToNextPage());
        nextButton.setDisable(false); // Başlangıçta aktif, API'den veri gelince güncellenecek

        // İlk ve son sayfa butonları
        firstPageButton = new Button("◀◀ İlk");
        firstPageButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        firstPageButton.setOnAction(e -> goToFirstPage());
        firstPageButton.setDisable(true); // Başlangıçta ilk sayfadayız

        lastPageButton = new Button("Son ▶▶");
        lastPageButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        lastPageButton.setOnAction(e -> goToLastPage());
        lastPageButton.setDisable(false); // Başlangıçta aktif, API'den veri gelince güncellenecek

        paginationBox.getChildren().addAll(
            firstPageButton,
            previousButton,
            pageInfoLabel,
            nextButton,
            lastPageButton
        );

        updatePaginationButtons();
        return paginationBox;
    }

    private void goToFirstPage() {
        if (currentPage > 0) {
            currentPage = 0;
            loadPaymentPointsData();
        }
    }

    private void goToLastPage() {
        if (totalPages > 0 && currentPage < totalPages - 1) {
            currentPage = totalPages - 1;
            loadPaymentPointsData();
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadPaymentPointsData();
        }
    }

    private void goToNextPage() {
        currentPage++;
        loadPaymentPointsData();
    }

    private void updatePaginationButtons() {
        boolean isFirstPage = (currentPage <= 0);
        // Sonraki buton her zaman aktif
        if (firstPageButton != null) firstPageButton.setDisable(isFirstPage);
        if (previousButton != null) previousButton.setDisable(isFirstPage);
        if (nextButton != null) nextButton.setDisable(false);
        if (lastPageButton != null) lastPageButton.setDisable(false);

        int displayCurrentPage = currentPage + 1;
        String pageInfo;
        if (totalPages > 0) {
            pageInfo = String.format("Sayfa %d / %d (Toplam: %d kayıt, sayfa başına %d)", 
                    displayCurrentPage, totalPages, totalElements, pageSize);
        } else {
            pageInfo = String.format("Sayfa %d (Veri yükleniyor...)", displayCurrentPage);
        }
        if (pageInfoLabel != null) pageInfoLabel.setText(pageInfo);
    }

    private void loadPaymentPointsData() {
        // API'den ödeme noktalarını yükle
        try {
            System.out.println("\n🌐 DİNAMİK BACKEND API ÇAĞRISI:");
            System.out.println("   - Kullanıcının gördüğü sayfa: " + (currentPage + 1) + "/" + totalPages);
            System.out.println("   - Backend için sayfa parametresi: " + currentPage + " (0-tabanlı, doğrudan currentPage)");
            System.out.println("   - Sayfa boyutu: " + pageSize + " (sabit 10 kayıt)");
            System.out.println("   - Sıralama: name");
            System.out.println("   - Tam API endpoint: http://localhost:8080/v1/api/payment-point?page=" + currentPage + "&size=" + pageSize + "&sort=name");
            System.out.println("   - Token kullanılmıyor (Postman testine göre)");

            // Backend 0-tabanlı sayfa kullanıyor - currentPage'i doğrudan gönder
            String response = PaymentPointApiClient.getAllPaymentPoints(currentPage, pageSize, "name");

            System.out.println("✅ API yanıtı alındı, uzunluk: " + (response != null ? response.length() : "null"));

            // JSON yanıtını işle
            if (response != null && !response.isEmpty()) {
                parsePaymentPointsResponse(response);
                // API'den veri geldi, tabloya ekle
                updateTableData();
                // updatePaginationButtons() burada çağrılacak
                return;
            } else {
                System.err.println("❌ API yanıtı boş veya null");
            }
        } catch (Exception e) {
            System.err.println("❌ Backend API hatası: " + e.getMessage());
            e.printStackTrace();
        }

        // API çalışmıyorsa hata mesajı göster
        System.err.println("❌ Backend API'den veri alınamadı. Sunucunun çalıştığından ve doğru endpoint'e erişilebildiğinden emin olun.");
        showAlert("Ödeme noktaları yüklenirken hata oluştu.\n\nBackend sunucusu ile bağlantı kurulamadı.\nLütfen sunucunun çalıştığından emin olun.");
    }
    
    // loadSampleDataWithPagination metodu kaldırıldı - Backend'den gerçek veri geliyor
    
    private void loadAndShowPaymentPointDetail(Long paymentPointId) {
        try {
            // API'den detaylı bilgi al
            System.out.println("Ödeme noktası detayı yükleniyor: " + paymentPointId);
            String response = PaymentPointApiClient.getPaymentPointById(paymentPointId);
            
            if (response != null && !response.isEmpty()) {
                // JSON'dan payment point'i parse et
                PaymentPoint detailedPoint = parseDetailedPaymentPointFromResponse(response);
                if (detailedPoint != null) {
                    showPaymentPointDetailDialog(detailedPoint);
                } else {
                    showAlert("Ödeme noktası detayları yüklenemedi.");
                }
            } else {
                showAlert("Ödeme noktası bulunamadı.");
            }
        } catch (Exception e) {
            System.err.println("Ödeme noktası detayı yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ödeme noktası detayları yüklenirken hata oluştu: " + e.getMessage());
        }
    }
    
    private PaymentPoint parseDetailedPaymentPointFromResponse(String response) {
        try {
            // API response format: {"message":"...","data":{...},"success":true}
            System.out.println("Detaylı ödeme noktası parsing...");
            
            if (response.contains("\"success\":true") && response.contains("\"data\":{")) {
                // Data bölümünü bul
                String dataStart = "\"data\":{";
                int dataStartIndex = response.indexOf(dataStart);
                if (dataStartIndex != -1) {
                    // Data objesinin sonunu bul
                    int dataEndIndex = response.lastIndexOf("},\"success\"");
                    if (dataEndIndex != -1) {
                        String dataJson = response.substring(dataStartIndex + dataStart.length() - 1, dataEndIndex + 1);
                        return parsePaymentPointFromJson(dataJson);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Detaylı payment point parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // parsePaymentPointsResponse erişimini public yap
    public void parsePaymentPointsResponse(String response) {
        // JSON yanıtını parse et
        if (paymentPointsList == null) {
            paymentPointsList = new ArrayList<>();
        }
        
        try {
            System.out.println("🔍 API YANIT ANALİZİ BAŞLADI");
            System.out.println("Response uzunluğu: " + response.length());
            System.out.println("Response ilk 500 karakter: " + response.substring(0, Math.min(500, response.length())));
            System.out.println("Response son 200 karakter: " + response.substring(Math.max(0, response.length() - 200)));
            
            // Önce listeyi temizle
            paymentPointsList.clear();
            
            // Backend'den gelen yanıt formatını kontrol et
            if (response.contains("\"success\":true") && response.contains("\"data\":{")) {
                // Format: {"message":"...","data":{"content":[...],"totalElements":...,"totalPages":...},"success":true}
                System.out.println("✅ Standard API format bulundu");
                parseStandardApiResponse(response);
            } else if (response.trim().startsWith("[") && response.trim().endsWith("]")) {
                // Format: Direkt array: [{"id":1,...}, {"id":2,...}]
                System.out.println("✅ Direkt array format bulundu");
                parseDirectArrayResponse(response);
            } else if (response.contains("\"content\":[")) {
                // Format: {"content":[...],"totalElements":...,"totalPages":...}
                System.out.println("✅ Spring Page format bulundu");
                parseSpringPageResponse(response);
            } else {
                System.err.println("❌ Tanınmayan JSON formatı!");
                System.err.println("Response başlangıcı: " + response.substring(0, Math.min(100, response.length())));
                return;
            }
            
            System.out.println("📊 SAYFALAMA SONUÇLARI:");
            System.out.println("   - Toplam kayıt: " + totalElements);
            System.out.println("   - Toplam sayfa: " + totalPages);
            System.out.println("   - Bu sayfadaki kayıt: " + paymentPointsList.size());
            System.out.println("   - Mevcut sayfa: " + (currentPage + 1) + "/" + totalPages + " (kullanıcıya gösterilen)");
            System.out.println("   - currentPage: " + currentPage + " (0-tabanlı, backend ile eşleşen)");
            System.out.println("   - Sayfa boyutu: " + pageSize + " (sabit 10)");
            
            // Backend 0-tabanlı örneklerinize göre doğrulama
            if (currentPage == 0) {
                System.out.println("   ✅ İlk sayfa (currentPage=0, backend page=0) - up to " + pageSize + " kayıt bekleniyor");
            } else if (currentPage == 1) {
                System.out.println("   ✅ İkinci sayfa (currentPage=1, backend page=1) - kalan kayıt bekleniyor");
            }
            
            // Backend response format doğrulama
            if (totalElements > 0 && totalPages > 0) {
                int expectedRecordsThisPage = Math.min(pageSize, totalElements - (currentPage * pageSize));
                System.out.println("   📋 Bu sayfa için beklenen kayıt: " + expectedRecordsThisPage);
                if (paymentPointsList.size() == expectedRecordsThisPage) {
                    System.out.println("   ✅ Kayıt sayısı doğru!");
                } else {
                    System.out.println("   ⚠️ Kayıt sayısı uyuşmuyor! Beklenen: " + expectedRecordsThisPage + ", Alınan: " + paymentPointsList.size());
                }
            }
            
            // API'den çekilen konum verilerini konsola yazdır
            System.out.println("\n===== apiden çekilen konum verileri =====");
            for (PaymentPoint point : paymentPointsList) {
                Double lat = getPaymentPointLatitude(point);
                Double lng = getPaymentPointLongitude(point);
                System.out.printf(java.util.Locale.US, "- %s: lat=%.6f, lng=%.6f\n", 
                    point.getName(), 
                    lat != null ? lat : 0.0, 
                    lng != null ? lng : 0.0);
            }
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("❌ JSON parsing hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void parseStandardApiResponse(String response) {
        try {
            // {"message":"Ödeme noktaları başarıyla getirildi","data":{"content":[...],"pageNumber":0,"pageSize":10,"totalElements":6,"totalPages":2,"first":true,"last":false},"success":true}
            String dataStart = "\"data\":{";
            int dataStartIndex = response.indexOf(dataStart);
            if (dataStartIndex != -1) {
                // Sayfalama bilgilerini çıkar
                totalElements = extractIntFromJson(response, "totalElements");
                totalPages = extractIntFromJson(response, "totalPages");
                // int backendPageNumber = extractIntFromJson(response, "pageNumber"); // Backend'den 0-tabanlı
                // int backendPageSize = extractIntFromJson(response, "pageSize");

                // currentPage sadece butonlar ile değişsin, API'den gelen pageNumber ile güncellenmesin

                System.out.println("📊 Backend'den gelen sayfalama değerleri:");
                System.out.println("   - totalElements: " + totalElements);
                System.out.println("   - totalPages: " + totalPages);
                // System.out.println("   - pageNumber: " + backendPageNumber + " (backend'den 0-tabanlı)");
                // System.out.println("   - pageSize: " + backendPageSize);
                System.out.println("   - Frontend currentPage: " + currentPage + " (0-tabanlı)");
                System.out.println("   - first: " + response.contains("\"first\":true"));
                System.out.println("   - last: " + response.contains("\"last\":true"));

                // Content array'ini bul ve işle
                String contentStart = "\"content\":[";
                int contentStartIndex = response.indexOf(contentStart, dataStartIndex);
                if (contentStartIndex != -1) {
                    int contentEndIndex = findContentArrayEnd(response, contentStartIndex + contentStart.length());
                    if (contentEndIndex != -1) {
                        String contentPart = response.substring(contentStartIndex + contentStart.length(), contentEndIndex);
                        parsePaymentPointObjects(contentPart);
                    }
                }

                System.out.println("✅ Standard API formatı başarıyla parse edildi:");
                System.out.println("   - Parse edilen kayıt sayısı: " + paymentPointsList.size());
                System.out.println("   - Beklenen kayıt sayısı: " + Math.min(pageSize, totalElements - (currentPage * pageSize)));
            }
        } catch (Exception e) {
            System.err.println("Standard API response parse hatası: " + e.getMessage());
        }
    }
    
    private void parseSpringPageResponse(String response) {
        try {
            // {"content":[...],"totalElements":...,"totalPages":...}
            totalElements = extractIntFromJson(response, "totalElements");
            totalPages = extractIntFromJson(response, "totalPages");
            
            System.out.println("📊 Spring Page formatından sayfalama değerleri:");
            System.out.println("   - totalElements: " + totalElements);
            System.out.println("   - totalPages: " + totalPages);
            
            String contentStart = "\"content\":[";
            int contentStartIndex = response.indexOf(contentStart);
            if (contentStartIndex != -1) {
                int contentEndIndex = findContentArrayEnd(response, contentStartIndex + contentStart.length());
                if (contentEndIndex != -1) {
                    String contentPart = response.substring(contentStartIndex + contentStart.length(), contentEndIndex);
                    parsePaymentPointObjects(contentPart);
                }
            }
            
            System.out.println("✅ Spring Page formatı başarıyla parse edildi");
        } catch (Exception e) {
            System.err.println("Spring Page response parse hatası: " + e.getMessage());
        }
    }
    
    private void parseDirectArrayResponse(String response) {
        try {
            // [{"id":1,...}, {"id":2,...}]
            String arrayContent = response.substring(1, response.length() - 1);
            parsePaymentPointObjects(arrayContent);
            
            // Direkt array'de sayfalama bilgisi yok, sayfa mantığı uygula
            totalElements = paymentPointsList.size(); // Bu sayfadaki kayıt sayısı
            
            // Eğer sayfa başına gösterilecek kayıt sayısından az gelirse son sayfa
            if (paymentPointsList.size() < pageSize) {
                // Bu son sayfa, toplam sayıyı tahmin et
                totalElements = (currentPage * pageSize) + paymentPointsList.size();
                totalPages = currentPage + 1;
            } else {
                // Muhtemelen daha fazla sayfa var
                totalPages = Math.max(currentPage + 2, 2); // En az 2 sayfa varsay
            }
            
            System.out.println("⚠️ Direkt array formatında sayfalama bilgisi yok:");
            System.out.println("   - Bu sayfadaki kayıt: " + paymentPointsList.size());
            System.out.println("   - Tahmini totalElements: " + totalElements);
            System.out.println("   - Tahmini totalPages: " + totalPages);
            
        } catch (Exception e) {
            System.err.println("Direkt array response parse hatası: " + e.getMessage());
        }
    }
    
    
    private int findContentArrayEnd(String response, int startIndex) {
        int bracketCount = 0;
        boolean inString = false;
        boolean escapeNext = false;
        
        for (int i = startIndex; i < response.length(); i++) {
            char c = response.charAt(i);
            
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
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    if (bracketCount == 0) {
                        return i; // Ana array'in sonu
                    }
                    bracketCount--;
                }
            }
        }
        
        return -1;
    }
    
    private void parsePaymentPointObjects(String contentPart) {
        try {
            if (paymentPointsList == null) {
                paymentPointsList = new ArrayList<>();
            }
            
            System.out.println("parsePaymentPointObjects başladı, content uzunluğu: " + contentPart.length());
            
            // JSON objelerini ayır
            int braceCount = 0;
            int startIndex = 0;
            boolean inString = false;
            boolean escapeNext = false;
            int objectCount = 0;
            
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
                            objectCount++;
                            System.out.println("Obje " + objectCount + " parse ediliyor, uzunluk: " + jsonObject.length());
                            
                            try {
                                PaymentPoint paymentPoint = parsePaymentPointFromJson(jsonObject);
                                if (paymentPoint != null) {
                                    paymentPointsList.add(paymentPoint);
                                    System.out.println("Başarıyla eklendi: " + paymentPoint.getName());
                                } else {
                                    System.err.println("PaymentPoint null döndü");
                                }
                            } catch (Exception e) {
                                System.err.println("Tek payment point parse hatası: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            System.out.println("parsePaymentPointObjects tamamlandı. Toplam " + objectCount + " obje işlendi, " + paymentPointsList.size() + " başarılı.");
            
        } catch (Exception e) {
            System.err.println("Payment point objects parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private PaymentPoint parsePaymentPointFromJson(String jsonObject) {
        try {
            System.out.println("JSON parsing başladı...");
            
            // JSON'dan değerleri çıkar
            Long id = extractLongFromJson(jsonObject, "id");
            String name = extractStringFromJson(jsonObject, "name");
            String contactNumber = extractStringFromJson(jsonObject, "contactNumber");
            String workingHours = extractStringFromJson(jsonObject, "workingHours");
            String description = extractStringFromJson(jsonObject, "description");
            boolean active = extractBooleanFromJson(jsonObject, "active");
            
            System.out.println("Temel bilgiler - ID: " + id + ", Name: " + name + ", Active: " + active);
            
            // Location bilgilerini çıkar
            String locationJson = extractObjectFromJson(jsonObject, "location");
            double latitude = 0.0;
            double longitude = 0.0;
            if (locationJson != null) {
                latitude = extractDoubleFromJson(locationJson, "latitude");
                longitude = extractDoubleFromJson(locationJson, "longitude");
                System.out.println("Location: " + latitude + ", " + longitude);
            }
            
            // Address bilgilerini çıkar
            String addressJson = extractObjectFromJson(jsonObject, "address");
            String street = "";
            String district = "";
            String city = "";
            String postalCode = "";
            if (addressJson != null) {
                street = extractStringFromJson(addressJson, "street");
                district = extractStringFromJson(addressJson, "district");
                city = extractStringFromJson(addressJson, "city");
                postalCode = extractStringFromJson(addressJson, "postalCode");
                System.out.println("Address: " + street + ", " + district + ", " + city);
            }
            
            // Payment methods array'ini çıkar
            List<PaymentMethod> paymentMethods = new ArrayList<>();
            List<String> paymentMethodStrings = extractArrayFromJson(jsonObject, "paymentMethods");
            for (String method : paymentMethodStrings) {
                try {
                    PaymentMethod pm = PaymentMethod.valueOf(method.replace("\"", ""));
                    paymentMethods.add(pm);
                } catch (IllegalArgumentException e) {
                    System.err.println("Bilinmeyen payment method: " + method);
                }
            }
            
            // Photos array'ini çıkar
            List<PaymentPhoto> photos = new ArrayList<>();
            List<String> photoStrings = extractArrayFromJson(jsonObject, "photos");
            for (String photoJson : photoStrings) {
                if (!photoJson.trim().isEmpty()) {
                    Long photoId = extractLongFromJson(photoJson, "id");
                    String photoUrl = extractStringFromJson(photoJson, "url");
                    
                    PaymentPhoto photo = new PaymentPhoto();
                    photo.setId(photoId);
                    photo.setImageUrl(photoUrl);
                    photos.add(photo);
                }
            }
            
            // DateTime bilgilerini çıkar
            LocalDateTime createdAt = extractDateTimeFromJson(jsonObject, "createdAt");
            LocalDateTime lastUpdated = extractDateTimeFromJson(jsonObject, "lastUpdated");
            
            // Distance bilgisini çıkar
            Double distance = extractDoubleFromJsonNullable(jsonObject, "distance");
            
            // Location objesini oluştur
            Location location = new Location(latitude, longitude);
            
            // Address objesini oluştur
            Address address = new Address(street, district, city, postalCode);
            
            PaymentPoint point = new PaymentPoint(
                id, name, location, address, contactNumber, workingHours,
                paymentMethods, description, active, photos, createdAt, lastUpdated, distance
            );
            
            System.out.println("PaymentPoint başarıyla oluşturuldu: " + point.getName());
            return point;
            
        } catch (Exception e) {
            System.err.println("Payment point parse hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // JSON parsing helper methods (duplicated methods removed)
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
        return "";
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
        return 0L;
    }
    
    // Not used currently but may be needed for location data
    private double extractDoubleFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        } catch (Exception e) {
            System.err.println("Double extract hatası (" + key + "): " + e.getMessage());
        }
        return 0.0;
    }
    
    private Double extractDoubleFromJsonNullable(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*(null|[0-9.]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                String value = m.group(1);
                if ("null".equals(value)) {
                    return null;
                }
                return Double.parseDouble(value);
            }
        } catch (Exception e) {
            System.err.println("Double nullable extract hatası (" + key + "): " + e.getMessage());
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
            System.err.println("Boolean extract hatası (" + key + "): " + e.getMessage());
        }
        return false;
    }
    
    private String extractObjectFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\\{";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                int startIndex = m.end() - 1; // { karakterinden başla
                int braceCount = 1;
                int endIndex = startIndex + 1;
                
                while (endIndex < json.length() && braceCount > 0) {
                    char c = json.charAt(endIndex);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                    endIndex++;
                }
                
                return json.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Object extract hatası (" + key + "): " + e.getMessage());
        }
        return null;
    }
    
    private List<String> extractArrayFromJson(String json, String key) {
        List<String> result = new ArrayList<>();
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\\[([^\\]]+)\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                String arrayContent = m.group(1);
                // String array elemanlarını ayır
                String[] elements = arrayContent.split(",");
                for (String element : elements) {
                    String cleaned = element.trim().replaceAll("\"", "");
                    if (!cleaned.isEmpty()) {
                        result.add(cleaned);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Array extract hatası (" + key + "): " + e.getMessage());
        }
        return result;
    }
    
    private LocalDateTime extractDateTimeFromJson(String json, String key) {
        try {
            String dateStr = extractStringFromJson(json, key);
            if (dateStr != null && !dateStr.isEmpty()) {
                return LocalDateTime.parse(dateStr);
            }
        } catch (Exception e) {
            System.err.println("DateTime extract hatası (" + key + "): " + e.getMessage());
        }
        return LocalDateTime.now();
    }
    
    
    // Statik örnek veri metodu kaldırıldı - Backend'den gerçek veri geliyor

    private void updateTableData() {
        System.out.println("updateTableData() çağrıldı");
        System.out.println("paymentPointsTable: " + (paymentPointsTable == null ? "null" : "not null"));
        System.out.println("paymentPointsList: " + (paymentPointsList == null ? "null" : "size=" + paymentPointsList.size()));
        
        if (paymentPointsTable != null) {
            paymentPointsTable.getItems().clear();
            if (paymentPointsList != null) {
                paymentPointsTable.getItems().addAll(paymentPointsList);
                System.out.println("Tabloya " + paymentPointsList.size() + " öğe eklendi");
            }
        } else {
            System.out.println("Tablo henüz null - createContent() çağrılmamış olabilir");
        }
        
        // Sayfalama bilgilerini güncelle
        updatePaginationButtons();
    }

    private void filterPaymentPoints() {
        // Filtreleme yapıldığında ilk sayfaya dön
        currentPage = 0;
        
        // API'den filtrelenmiş verileri yükle (backend filtre desteği yoksa localde uygula)
        loadPaymentPointsData();
        
        // Eğer API çalışmıyorsa, local filtreleme yap
        if (paymentPointsList == null) {
            return;
        }
        
        String searchText = searchField.getText();
        String selectedCity = cityFilter.getValue();
        String selectedPaymentMethod = paymentMethodFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        List<PaymentPoint> filteredList = new ArrayList<>();

        for (PaymentPoint point : paymentPointsList) {
            boolean matchesSearch = searchText == null || searchText.trim().isEmpty() || 
                                  point.getName().toLowerCase().contains(searchText.toLowerCase());
            
            boolean matchesCity = "Tümü".equals(selectedCity) || 
                                getPaymentPointCity(point).equals(selectedCity);
            
            boolean matchesPaymentMethod = "Tümü".equals(selectedPaymentMethod) || 
                                         point.getPaymentMethods().stream()
                                             .anyMatch(pm -> pm.getDisplayName().equals(selectedPaymentMethod));
            
            boolean matchesStatus = "Tümü".equals(selectedStatus) || 
                                  (point.isActive() && "Aktif".equals(selectedStatus)) ||
                                  (!point.isActive() && "Pasif".equals(selectedStatus));

            if (matchesSearch && matchesCity && matchesPaymentMethod && matchesStatus) {
                filteredList.add(point);
            }
        }

        // Filtrelenmiş veriler için sayfalama hesapla
        totalElements = filteredList.size();
        totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        // Mevcut sayfa için veri dilimini al
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        List<PaymentPoint> currentPageData = new ArrayList<>();
        if (startIndex < totalElements) {
            currentPageData = filteredList.subList(startIndex, endIndex);
        }

        if (paymentPointsTable != null) {
            paymentPointsTable.getItems().clear();
            paymentPointsTable.getItems().addAll(currentPageData);
        }
        
        updatePaginationButtons();
    }

    private void clearFilters() {
        searchField.clear();
        cityFilter.setValue("Tümü");
        paymentMethodFilter.setValue("Tümü");
        statusFilter.setValue("Tümü");
        
        // Filtreler temizlendiğinde ilk sayfaya dön
        currentPage = 0;
        loadPaymentPointsData();
    }

    private void showPaymentPointDetailDialog(PaymentPoint point) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ödeme Noktası Detayları");
        dialog.setHeaderText(null);

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(500);

        // "Kapat" butonu
        ButtonType closeButtonType = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // İçerik
        VBox content = createPaymentPointDetailContent(point);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait();
    }

    private VBox createPaymentPointDetailContent(PaymentPoint point) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Başlık
        Label titleLabel = new Label(point.getName());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Bilgi kartları
        VBox infoCards = new VBox(10);
        
        infoCards.getChildren().addAll(
            createInfoCard("📍 Adres", getPaymentPointFullAddress(point)),
            createInfoCard("🏙️ Şehir", getPaymentPointCity(point)),
            createInfoCard("📞 Telefon", point.getContactNumber()),
            createInfoCard("🕒 Çalışma Saatleri", point.getWorkingHours()),
            createInfoCard("💳 Ödeme Yöntemleri", getPaymentPointPaymentMethodsString(point)),
            createInfoCard("📝 Açıklama", point.getDescription() != null ? point.getDescription() : "Açıklama yok"),
            createInfoCard("📊 Durum", getPaymentPointStatusString(point)),
            createInfoCard("🌍 Konum", getPaymentPointLocationString(point)),
            createInfoCard("📸 Fotoğraflar", getPaymentPointPhotosString(point)),
            createInfoCard("📅 Oluşturulma", getPaymentPointCreatedAtString(point)),
            createInfoCard("🔄 Son Güncelleme", getPaymentPointLastUpdatedString(point))
        );
        
        // Fotoğraflar varsa, fotoğraf detaylarını göster
        if (!point.getPhotos().isEmpty()) {
            VBox photosSection = createPhotosSection(point);
            infoCards.getChildren().add(photosSection);
        }

        content.getChildren().addAll(titleLabel, new Separator(), infoCards);
        return content;
    }

    private HBox createInfoCard(String label, String value) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);

        card.getChildren().addAll(labelLabel, valueLabel);
        return card;
    }
    
    private VBox createPhotosSection(PaymentPoint point) {
        VBox photosSection = new VBox(10);
        photosSection.setPadding(new Insets(10));
        photosSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        Label photosLabel = new Label("📸 Fotoğraflar (" + point.getPhotos().size() + ")");
        photosLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        if (point.getPhotos().isEmpty()) {
            Label noPhotosLabel = new Label("Bu ödeme noktası için henüz fotoğraf eklenmemiş.");
            noPhotosLabel.setStyle("-fx-text-fill: #6c757d;");
            photosSection.getChildren().addAll(photosLabel, noPhotosLabel);
        } else {
            // Fotoğraf URL'lerini listele
            VBox photosList = new VBox(5);
            for (int i = 0; i < point.getPhotos().size(); i++) {
                PaymentPhoto photo = point.getPhotos().get(i);
                HBox photoRow = new HBox(10);
                photoRow.setAlignment(Pos.CENTER_LEFT);
                
                Label photoIndexLabel = new Label((i + 1) + ".");
                photoIndexLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 30;");
                
                Label photoUrlLabel = new Label(photo.getImageUrl());
                photoUrlLabel.setWrapText(true);
                photoUrlLabel.setStyle("-fx-text-fill: #007bff;");
                
                // Fotoğraf görüntüleme butonu
                Button viewPhotoButton = new Button("Görüntüle");
                viewPhotoButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 8;");
                viewPhotoButton.setOnAction(e -> openPhotoViewer(photo.getImageUrl()));
                
                photoRow.getChildren().addAll(photoIndexLabel, photoUrlLabel, viewPhotoButton);
                photosList.getChildren().add(photoRow);
            }
            
            photosSection.getChildren().addAll(photosLabel, photosList);
        }
        
        return photosSection;
    }
    
    private void openPhotoViewer(String photoUrl) {
        try {
            // Fotoğraf görüntüleyici dialog
            Dialog<Void> photoDialog = new Dialog<>();
            photoDialog.setTitle("Fotoğraf Görüntüleyici");
            photoDialog.setHeaderText("Ödeme Noktası Fotoğrafı");
            
            // Dialog boyutunu ayarla
            photoDialog.getDialogPane().setPrefWidth(600);
            photoDialog.getDialogPane().setPrefHeight(500);
            
            // Kapat butonu
            ButtonType closeButtonType = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
            photoDialog.getDialogPane().getButtonTypes().add(closeButtonType);
            
            // İçerik
            VBox photoContent = new VBox(10);
            photoContent.setPadding(new Insets(20));
            photoContent.setAlignment(Pos.CENTER);
            
            Label urlLabel = new Label("Fotoğraf URL:");
            urlLabel.setStyle("-fx-font-weight: bold;");
            
            Label urlValue = new Label(photoUrl);
            urlValue.setWrapText(true);
            urlValue.setStyle("-fx-text-fill: #007bff;");
            
            // URL açma butonu
            Button openUrlButton = new Button("Tarayıcıda Aç");
            openUrlButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5;");
            openUrlButton.setOnAction(e -> {
                try {
                    // URL'yi kopyala - kullanıcı manuel olarak açabilir
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(photoUrl);
                    clipboard.setContent(content);
                    showAlert("Fotoğraf URL'si panoya kopyalandı. Tarayıcınızda açabilirsiniz.");
                } catch (Exception ex) {
                    showAlert("URL kopyalanamadı: " + ex.getMessage());
                }
            });
            
            photoContent.getChildren().addAll(urlLabel, urlValue, openUrlButton);
            photoDialog.getDialogPane().setContent(photoContent);
            
            photoDialog.showAndWait();
        } catch (Exception e) {
            showAlert("Fotoğraf görüntülenirken hata oluştu: " + e.getMessage());
        }
    }

    private void showEditPaymentPointDialog(PaymentPoint point) {
        Dialog<PaymentPointUpdateDTO> dialog = new Dialog<>();
        dialog.setTitle("Ödeme Noktası Düzenle");
        dialog.setHeaderText("Ödeme Noktası Bilgilerini Güncelleyin");

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(700);

        // Buton türleri
        ButtonType updateButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, cancelButtonType);

        // Form içeriği
        VBox form = createPaymentPointForm(point);
        dialog.getDialogPane().setContent(form);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return extractFormData(form);
            }
            return null;
        });

        // Dialog sonucu
        dialog.showAndWait().ifPresent(updateData -> {
            updatePaymentPoint(point.getId(), updateData);
        });
    }

    private void showAddPaymentPointDialog() {
        Dialog<PaymentPointUpdateDTO> dialog = new Dialog<>();
        dialog.setTitle("Yeni Ödeme Noktası Ekle");
        dialog.setHeaderText("Yeni Ödeme Noktası Bilgilerini Girin");

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(700);

        // Buton türleri
        ButtonType createButtonType = new ButtonType("Oluştur", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

        // Form içeriği - boş form
        VBox form = createPaymentPointForm(null);
        dialog.getDialogPane().setContent(form);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return extractFormData(form);
            }
            return null;
        });

        // Dialog sonucu
        dialog.showAndWait().ifPresent(createData -> {
            createPaymentPoint(createData);
        });
    }

    private VBox createPaymentPointForm(PaymentPoint existingPoint) {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        // Ödeme noktası adı
        Label nameLabel = new Label("Ödeme Noktası Adı *");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Ödeme noktası adını girin...");
        if (existingPoint != null) nameField.setText(existingPoint.getName());
        nameField.setUserData("name");

        // Adres bilgileri
        Label addressTitle = new Label("Adres Bilgileri");
        addressTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Sokak
        Label streetLabel = new Label("Sokak");
        TextField streetField = new TextField();
        streetField.setPromptText("Sokak adresini girin...");
        if (existingPoint != null) streetField.setText(getPaymentPointStreet(existingPoint));
        streetField.setUserData("street");

        // İlçe
        Label districtLabel = new Label("İlçe");
        TextField districtField = new TextField();
        districtField.setPromptText("İlçe adını girin...");
        if (existingPoint != null) districtField.setText(getPaymentPointDistrict(existingPoint));
        districtField.setUserData("district");

        // Şehir
        Label cityLabel = new Label("Şehir *");
        cityLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cityCombo = new ComboBox<>();
        cityCombo.getItems().addAll("İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Adana", "Gaziantep", "Konya", "Kayseri", "Mersin");
        cityCombo.setEditable(true);
        if (existingPoint != null) cityCombo.setValue(getPaymentPointCity(existingPoint));
        cityCombo.setUserData("city");

        // Posta kodu
        Label postalCodeLabel = new Label("Posta Kodu");
        TextField postalCodeField = new TextField();
        postalCodeField.setPromptText("Posta kodunu girin...");
        if (existingPoint != null) postalCodeField.setText(getPaymentPointPostalCode(existingPoint));
        postalCodeField.setUserData("postalCode");

        // İletişim bilgileri
        Label contactTitle = new Label("İletişim Bilgileri");
        contactTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Telefon
        Label phoneLabel = new Label("Telefon");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Telefon numarasını girin...");
        if (existingPoint != null) phoneField.setText(existingPoint.getContactNumber());
        phoneField.setUserData("contactNumber");

        // Çalışma saatleri
        Label hoursLabel = new Label("Çalışma Saatleri");
        TextField hoursField = new TextField();
        hoursField.setPromptText("Örn: 08:00 - 22:00");
        if (existingPoint != null) hoursField.setText(existingPoint.getWorkingHours());
        hoursField.setUserData("workingHours");

        // Ödeme yöntemleri
        Label paymentMethodsTitle = new Label("Ödeme Yöntemleri *");
        paymentMethodsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox paymentMethodsBox = new VBox(5);
        String[] methods = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "MOBILE_APP", "QR_CODE"};
        for (String method : methods) {
            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox(method);
            checkBox.setUserData(method);
            if (existingPoint != null && existingPoint.getPaymentMethods().stream()
                    .anyMatch(pm -> pm.name().equals(method))) {
                checkBox.setSelected(true);
            }
            paymentMethodsBox.getChildren().add(checkBox);
        }
        paymentMethodsBox.setUserData("paymentMethods");

        // Açıklama
        Label descriptionLabel = new Label("Açıklama");
        javafx.scene.control.TextArea descriptionArea = new javafx.scene.control.TextArea();
        descriptionArea.setPromptText("Ödeme noktası açıklamasını girin...");
        descriptionArea.setPrefRowCount(3);
        if (existingPoint != null) descriptionArea.setText(existingPoint.getDescription());
        descriptionArea.setUserData("description");

        // Konum bilgileri
        Label locationTitle = new Label("Konum Bilgileri");
        locationTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox locationBox = new HBox(10);
        
        // Enlem
        Label latLabel = new Label("Enlem");
        TextField latField = new TextField();
        latField.setPromptText("Enlem değeri...");
        if (existingPoint != null && getPaymentPointLatitude(existingPoint) != null && getPaymentPointLatitude(existingPoint) != 0.0) {
            latField.setText(String.valueOf(getPaymentPointLatitude(existingPoint)));
        }
        latField.setUserData("latitude");

        // Boylam
        Label lngLabel = new Label("Boylam");
        TextField lngField = new TextField();
        lngField.setPromptText("Boylam değeri...");
        if (existingPoint != null && getPaymentPointLongitude(existingPoint) != null && getPaymentPointLongitude(existingPoint) != 0.0) {
            lngField.setText(String.valueOf(getPaymentPointLongitude(existingPoint)));
        }
        lngField.setUserData("longitude");

        VBox latBox = new VBox(5);
        latBox.getChildren().addAll(latLabel, latField);
        
        VBox lngBox = new VBox(5);
        lngBox.getChildren().addAll(lngLabel, lngField);

        locationBox.getChildren().addAll(latBox, lngBox);

        // Durum
        javafx.scene.control.CheckBox activeCheckBox = new javafx.scene.control.CheckBox("Aktif");
        activeCheckBox.setStyle("-fx-font-weight: bold;");
        if (existingPoint != null) {
            activeCheckBox.setSelected(existingPoint.isActive());
        } else {
            activeCheckBox.setSelected(true); // Yeni ödeme noktaları varsayılan olarak aktif
        }
        activeCheckBox.setUserData("active");

        // Form elemanlarını ekle
        form.getChildren().addAll(
            nameLabel, nameField,
            new Separator(),
            addressTitle,
            streetLabel, streetField,
            districtLabel, districtField,
            cityLabel, cityCombo,
            postalCodeLabel, postalCodeField,
            new Separator(),
            contactTitle,
            phoneLabel, phoneField,
            hoursLabel, hoursField,
            new Separator(),
            paymentMethodsTitle, paymentMethodsBox,
            new Separator(),
            descriptionLabel, descriptionArea,
            new Separator(),
            locationTitle, locationBox,
            new Separator(),
            activeCheckBox
        );

        return form;
    }

    private PaymentPointUpdateDTO extractFormData(VBox form) {
        PaymentPointUpdateDTO dto = new PaymentPointUpdateDTO();
        
        // Form elemanlarını dolaş ve verileri çıkar
        for (Node node : form.getChildren()) {
            if (node instanceof TextField) {
                TextField field = (TextField) node;
                String userData = (String) field.getUserData();
                if (userData != null) {
                    switch (userData) {
                        case "name":
                            dto.setName(field.getText());
                            break;
                        case "street":
                            if (dto.getAddress() == null) dto.setAddress(new PaymentPointUpdateDTO.AddressDTO());
                            dto.getAddress().setStreet(field.getText());
                            break;
                        case "district":
                            if (dto.getAddress() == null) dto.setAddress(new PaymentPointUpdateDTO.AddressDTO());
                            dto.getAddress().setDistrict(field.getText());
                            break;
                        case "postalCode":
                            if (dto.getAddress() == null) dto.setAddress(new PaymentPointUpdateDTO.AddressDTO());
                            dto.getAddress().setPostalCode(field.getText());
                            break;
                        case "contactNumber":
                            dto.setContactNumber(field.getText());
                            break;
                        case "workingHours":
                            dto.setWorkingHours(field.getText());
                            break;
                        case "latitude":
                            if (!field.getText().isEmpty()) {
                                try {
                                    if (dto.getLocation() == null) dto.setLocation(new PaymentPointUpdateDTO.LocationDTO());
                                    dto.getLocation().setLatitude(Double.parseDouble(field.getText()));
                                } catch (NumberFormatException e) {
                                    // Geçersiz sayı, görmezden gel
                                }
                            }
                            break;
                        case "longitude":
                            if (!field.getText().isEmpty()) {
                                try {
                                    if (dto.getLocation() == null) dto.setLocation(new PaymentPointUpdateDTO.LocationDTO());
                                    dto.getLocation().setLongitude(Double.parseDouble(field.getText()));
                                } catch (NumberFormatException e) {
                                    // Geçersiz sayı, görmezden gel
                                }
                            }
                            break;
                    }
                }
            } else if (node instanceof ComboBox) {
                ComboBox<?> combo = (ComboBox<?>) node;
                if ("city".equals(combo.getUserData())) {
                    if (dto.getAddress() == null) dto.setAddress(new PaymentPointUpdateDTO.AddressDTO());
                    dto.getAddress().setCity((String) combo.getValue());
                }
            } else if (node instanceof javafx.scene.control.TextArea) {
                javafx.scene.control.TextArea area = (javafx.scene.control.TextArea) node;
                if ("description".equals(area.getUserData())) {
                    dto.setDescription(area.getText());
                }
            } else if (node instanceof javafx.scene.control.CheckBox) {
                javafx.scene.control.CheckBox checkBox = (javafx.scene.control.CheckBox) node;
                if ("active".equals(checkBox.getUserData())) {
                    dto.setActive(checkBox.isSelected());
                }
            } else if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                if ("paymentMethods".equals(vbox.getUserData())) {
                    List<String> selectedMethods = new ArrayList<>();
                    for (Node child : vbox.getChildren()) {
                        if (child instanceof javafx.scene.control.CheckBox) {
                            javafx.scene.control.CheckBox methodCheckBox = (javafx.scene.control.CheckBox) child;
                            if (methodCheckBox.isSelected()) {
                                selectedMethods.add((String) methodCheckBox.getUserData());
                            }
                        }
                    }
                    dto.setPaymentMethods(selectedMethods);
                }
            }
        }
        
        return dto;
    }

    private void updatePaymentPoint(Long id, PaymentPointUpdateDTO updateData) {
        try {
            System.out.println("Ödeme noktası güncelleniyor: " + id);
            
            String response = PaymentPointApiClient.updatePaymentPoint(id, updateData, accessToken);
            System.out.println("Update response: " + response);
            
            if (response != null && response.contains("\"success\":true")) {
                showAlert("✅ Ödeme noktası başarıyla güncellendi!");
                loadPaymentPointsData(); // Listeyi yenile
            } else {
                showAlert("❌ Güncelleme işlemi başarısız oldu.");
            }
            
        } catch (Exception e) {
            System.err.println("Güncelleme hatası: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Güncelleme işleminde hata oluştu: " + e.getMessage());
        }
    }

    private void createPaymentPoint(PaymentPointUpdateDTO createData) {
        try {
            System.out.println("Yeni ödeme noktası oluşturuluyor...");
            
            String response = PaymentPointApiClient.createPaymentPoint(createData, accessToken);
            System.out.println("Create response: " + response);
            
            if (response != null && response.contains("\"success\":true")) {
                showAlert("✅ Yeni ödeme noktası başarıyla oluşturuldu!");
                loadPaymentPointsData(); // Listeyi yenile
            } else {
                showAlert("❌ Oluşturma işlemi başarısız oldu.");
            }
            
        } catch (Exception e) {
            System.err.println("Oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Oluşturma işleminde hata oluştu: " + e.getMessage());
        }
    }

    private void togglePaymentPointStatus(PaymentPoint point) {
        try {
            // API çağrısı ile durum değiştirme
            System.out.println("Ödeme noktası durumu değiştiriliyor: " + point.getId() + " -> " + !point.isActive());
            
            String response = PaymentPointApiClient.updatePaymentPointStatus(
                point.getId(), 
                !point.isActive(), 
                accessToken
            );
            
            System.out.println("Status update response: " + response);
            
            if (response != null && response.contains("\"success\":true")) {
                // Başarılı olursa local güncelleme
                boolean newStatus = !point.isActive();
                point.setActive(newStatus);
                point.setLastUpdated(LocalDateTime.now());
                
                updateTableData();
                
                String message = newStatus ? "aktif" : "pasif";
                showAlert("✅ " + point.getName() + " ödeme noktası " + message + " duruma getirildi.");
            } else {
                showAlert("❌ Durum değiştirme işlemi başarısız oldu.");
            }
            
        } catch (Exception e) {
            System.err.println("Durum değiştirme hatası: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Durum değiştirme işleminde hata oluştu: " + e.getMessage());
        }
    }

    private void showDeleteConfirmation(PaymentPoint point) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ödeme Noktası Sil");
        alert.setHeaderText("Ödeme Noktası Silme Onayı");
        alert.setContentText("\"" + point.getName() + "\" ödeme noktasını silmek istediğinize emin misiniz?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                deletePaymentPoint(point);
            }
        });
    }

    private void deletePaymentPoint(PaymentPoint point) {
        try {
            // API çağrısı ile silme
            System.out.println("Ödeme noktası siliniyor: " + point.getId());
            
            String response = PaymentPointApiClient.deletePaymentPoint(
                point.getId(), 
                accessToken
            );
            
            System.out.println("Delete response: " + response);
            
            if (response != null && response.contains("\"success\":true")) {
                // Başarılı olursa local silme
                paymentPointsList.remove(point);
                updateTableData();
                
                showAlert("✅ " + point.getName() + " ödeme noktası başarıyla silindi.");
            } else {
                showAlert("❌ Silme işlemi başarısız oldu.");
            }
            
        } catch (Exception e) {
            System.err.println("Silme hatası: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Silme işleminde hata oluştu: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // PaymentPoint model sınıfından helper metotlar
    private PaymentPoint mapToPaymentPoint(String json) {
        try {
            Long id = extractLongFromJson(json, "id");
            String name = extractStringFromJson(json, "name");
            String description = extractStringFromJson(json, "description");
            String contactNumber = extractStringFromJson(json, "contactNumber");
            String workingHours = extractStringFromJson(json, "workingHours");
            boolean active = extractBooleanFromJson(json, "active");
            
            // Location bilgisini çıkar
            String locationJson = extractObjectFromJson(json, "location");
            Location location = null;
            if (locationJson != null && !locationJson.isEmpty()) {
                double latitude = extractDoubleFromJson(locationJson, "latitude");
                double longitude = extractDoubleFromJson(locationJson, "longitude");
                location = new Location(latitude, longitude);
            }
            
            // Address bilgisini çıkar
            String street = extractStringFromJson(json, "street");
            String district = extractStringFromJson(json, "district");
            String city = extractStringFromJson(json, "city");
            String postalCode = extractStringFromJson(json, "postalCode");
            Address address = new Address(street, district, city, postalCode);
            
            // Payment methods array'ini çıkar
            List<PaymentMethod> paymentMethods = new ArrayList<>();
            List<String> paymentMethodStrings = extractArrayFromJson(json, "paymentMethods");
            for (String method : paymentMethodStrings) {
                try {
                    PaymentMethod pm = PaymentMethod.valueOf(method.replace("\"", ""));
                    paymentMethods.add(pm);
                } catch (IllegalArgumentException e) {
                    System.err.println("Bilinmeyen payment method: " + method);
                }
            }
            
            // Photos array'ini çıkar
            List<PaymentPhoto> photos = new ArrayList<>();
            List<String> photoStrings = extractArrayFromJson(json, "photos");
            for (String photoJson : photoStrings) {
                if (!photoJson.trim().isEmpty()) {
                    Long photoId = extractLongFromJson(photoJson, "id");
                    String photoUrl = extractStringFromJson(photoJson, "url");
                    
                    PaymentPhoto photo = new PaymentPhoto();
                    photo.setId(photoId);
                    photo.setImageUrl(photoUrl);
                    photos.add(photo);
                }
            }
            
            // Tarih alanları
            LocalDateTime createdAt = extractDateTimeFromJson(json, "createdAt");
            LocalDateTime lastUpdated = extractDateTimeFromJson(json, "lastUpdated");
            
            // Distance bilgisi (isteğe bağlı)
            Double distance = extractDoubleFromJsonNullable(json, "distance");
            
            return new PaymentPoint(id, name, location, address, contactNumber, workingHours, 
                                    paymentMethods, description, active, photos, createdAt, lastUpdated, distance);
                                    
        } catch (Exception e) {
            System.err.println("PaymentPoint mapping hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper methods for PaymentPoint model
    private String getPaymentPointFullAddress(PaymentPoint point) {
        if (point.getAddress() != null) {
            return point.getAddress().getStreet() + ", " + point.getAddress().getDistrict() + 
                   ", " + point.getAddress().getCity() + " " + point.getAddress().getPostalCode();
        }
        return "";
    }
    
    private String getPaymentPointCity(PaymentPoint point) {
        return point.getAddress() != null ? point.getAddress().getCity() : "";
    }
    
    private String getPaymentPointPaymentMethodsString(PaymentPoint point) {
        if (point.getPaymentMethods() != null) {
            return point.getPaymentMethods().stream()
                    .map(PaymentMethod::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return "";
    }
    
    private String getPaymentPointStatusString(PaymentPoint point) {
        return point.isActive() ? "Aktif" : "Pasif";
    }
    
    private String getPaymentPointLocationString(PaymentPoint point) {
        if (point.getLocation() != null && point.getLocation().getLatitude() != null && 
            point.getLocation().getLongitude() != null) {
            return String.format("%.6f, %.6f", point.getLocation().getLatitude(), 
                                point.getLocation().getLongitude());
        }
        return "Konum bilgisi yok";
    }
    
    private String getPaymentPointPhotosString(PaymentPoint point) {
        if (point.getPhotos() == null || point.getPhotos().isEmpty()) {
            return "Fotoğraf yok";
        }
        return point.getPhotos().size() + " fotoğraf";
    }
    
    private String getPaymentPointCreatedAtString(PaymentPoint point) {
        return point.getCreatedAt() != null ? 
               point.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
    }
    
    private String getPaymentPointLastUpdatedString(PaymentPoint point) {
        return point.getLastUpdated() != null ? 
               point.getLastUpdated().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
    }
    
    private String getPaymentPointStreet(PaymentPoint point) {
        return point.getAddress() != null ? point.getAddress().getStreet() : "";
    }
    
    private String getPaymentPointDistrict(PaymentPoint point) {
        return point.getAddress() != null ? point.getAddress().getDistrict() : "";
    }
    
    private String getPaymentPointPostalCode(PaymentPoint point) {
        return point.getAddress() != null ? point.getAddress().getPostalCode() : "";
    }
    
    private Double getPaymentPointLatitude(PaymentPoint point) {
        return point.getLocation() != null ? point.getLocation().getLatitude() : null;
    }
    
    private Double getPaymentPointLongitude(PaymentPoint point) {
        return point.getLocation() != null ? point.getLocation().getLongitude() : null;
    }
    
    private int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            System.err.println("Int extract hatası (" + key + "): " + e.getMessage());
        }
        return 0;
    }
}
