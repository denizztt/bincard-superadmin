package com.bincard.bincard_superadmin;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AdminApprovalsPage extends SuperadminPageBase {

    private TableView<AdminRequest> tableView;
    private Label statusLabel;

    // Admin onay talebi sınıfı
    public static class AdminRequest {
        private String id;         // AdminApprovalRequest ID (onay/red endpoint'i için kullanılır)
        private String adminId;    // Admin kullanıcısının ID'si (adminin kendi ID'si)
        private String name;
        private String email;
        private String phone;
        private String requestDate;
        private String status;

        public AdminRequest(String id, String adminId, String name, String email, String phone, String requestDate, String status) {
            this.id = id;
            this.adminId = adminId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.requestDate = requestDate;
            this.status = status;
        }

        public String getId() { return id; }
        public String getAdminId() { return adminId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRequestDate() { return requestDate; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }

    public AdminApprovalsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken, HostServices hostServices) {
        super(stage, accessToken, refreshToken, "Admin Onay İstekleri");
        // We don't need the hostServices in this class anymore
    }

    @Override
    protected Node createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Başlık ve açıklama
        Label titleLabel = new Label("Admin Onay İstekleri");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2d3436"));
        
        Label descriptionLabel = new Label("Admin uygulamasından gelen kayıt isteklerini buradan onaylayabilir veya reddedebilirsiniz.");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        descriptionLabel.setTextFill(Color.web("#636e72"));
        
        // Durum etiketi
        statusLabel = new Label("Admin onay istekleri yükleniyor...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.web("#636e72"));
        
        // Butonlar
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #4e54c8; -fx-text-fill: white; -fx-font-size: 14px;");
        refreshButton.setOnAction(e -> loadAdminRequests());
        
        buttonBar.getChildren().add(refreshButton);
        
        // Tablo oluştur
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // ID kolonu
        TableColumn<AdminRequest, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(70);
        
        // İsim kolonu
        TableColumn<AdminRequest, String> nameColumn = new TableColumn<>("İsim");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setMinWidth(120);
        
        // Email kolonu
        TableColumn<AdminRequest, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setMinWidth(180);
        
        // Telefon kolonu
        TableColumn<AdminRequest, String> phoneColumn = new TableColumn<>("Telefon");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setMinWidth(120);
        
        // Tarih kolonu
        TableColumn<AdminRequest, String> dateColumn = new TableColumn<>("Talep Tarihi");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateColumn.setMinWidth(150);
        
        // Durum kolonu
        TableColumn<AdminRequest, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setMinWidth(100);
        
        // İşlem kolonu
        TableColumn<AdminRequest, Void> actionColumn = new TableColumn<>("İşlem");
        actionColumn.setMinWidth(200);
        
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("Onayla");
            private final Button rejectButton = new Button("Reddet");
            
            {
                // Onaylama butonu
                approveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                approveButton.setOnAction(event -> {
                    AdminRequest request = getTableView().getItems().get(getIndex());
                    approveAdminRequest(request);
                });
                
                // Reddetme butonu
                rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                rejectButton.setOnAction(event -> {
                    AdminRequest request = getTableView().getItems().get(getIndex());
                    rejectAdminRequest(request);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AdminRequest request = getTableView().getItems().get(getIndex());
                    HBox buttonBox = new HBox(5);
                    buttonBox.setAlignment(Pos.CENTER);
                    
                    // İsteğin durumuna göre butonları göster/gizle
                    if (request.getStatus().equals("Beklemede")) {
                        buttonBox.getChildren().addAll(approveButton, rejectButton);
                    } else {
                        Label statusInfo = new Label(request.getStatus());
                        if (request.getStatus().equals("Onaylandı")) {
                            statusInfo.setTextFill(Color.web("#2ecc71"));
                        } else {
                            statusInfo.setTextFill(Color.web("#e74c3c"));
                        }
                        buttonBox.getChildren().add(statusInfo);
                    }
                    
                    setGraphic(buttonBox);
                }
            }
        });
        
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(emailColumn);
        tableView.getColumns().add(phoneColumn);
        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(statusColumn);
        tableView.getColumns().add(actionColumn);
        
        // Demo veri yükle
        loadAdminRequests();
        
        content.getChildren().addAll(titleLabel, descriptionLabel, buttonBar, tableView, statusLabel);
        return content;
    }
    
    private void loadAdminRequests() {
        statusLabel.setText("Admin onay istekleri yükleniyor...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                // Gerçek API'dan verileri çek
                String response = ApiClientFX.getPendingAdminRequests(accessToken, 0, 50);
                return parseAdminRequestsResponse(response);
            } catch (Exception e) {
                System.err.println("Admin istekleri yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<AdminRequest>();
            }
        }).thenAccept(requests -> {
            Platform.runLater(() -> {
                ObservableList<AdminRequest> requestList = FXCollections.observableArrayList(requests);
                tableView.setItems(requestList);
                
                if (requests.isEmpty()) {
                    statusLabel.setText("Bekleyen admin onay isteği bulunmamaktadır.");
                } else {
                    statusLabel.setText(requests.size() + " adet bekleyen admin onay isteği bulundu.");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                statusLabel.setText("Veriler yüklenirken bir hata oluştu: " + throwable.getMessage());
                System.err.println("Veri yükleme hatası: " + throwable.getMessage());
                throwable.printStackTrace();
            });
            return null;
        });
    }
    
    /**
     * API'dan gelen JSON response'u AdminRequest listesine çevirir
     */
    private java.util.List<AdminRequest> parseAdminRequestsResponse(String jsonResponse) {
        java.util.List<AdminRequest> requests = new ArrayList<>();
        
        try {
            // JSON response'u parse et
            // DataResponseMessage<List<AdminApprovalRequest>> formatı bekleniyor
            
            System.out.println("Parsing response: " + jsonResponse);
            
            // Önce "data" array'ini bul
            int dataStartIdx = jsonResponse.indexOf("\"data\":[");
            if (dataStartIdx >= 0) {
                dataStartIdx += 7; // "data":[ uzunluğu
                
                // Tüm data array'ini al
                int bracketCount = 1;
                int dataEndIdx = dataStartIdx;
                
                for (int i = dataStartIdx + 1; i < jsonResponse.length(); i++) {
                    char c = jsonResponse.charAt(i);
                    if (c == '[') bracketCount++;
                    else if (c == ']') {
                        bracketCount--;
                        if (bracketCount == 0) {
                            dataEndIdx = i;
                            break;
                        }
                    }
                }
                
                // Geçerli bir data array'i bulundu mu?
                if (dataEndIdx > dataStartIdx) {
                    String dataArray = jsonResponse.substring(dataStartIdx, dataEndIdx + 1);
                    
                    // Her bir JSON nesnesini bul
                    int objStartIdx = 0;
                    while (objStartIdx < dataArray.length()) {
                        // Bir JSON nesnesi bul
                        int curlyStart = dataArray.indexOf('{', objStartIdx);
                        if (curlyStart == -1) break;
                        
                        // Nesnenin kapanış parantezini bul
                        int curlyCount = 1;
                        int curlyEnd = -1;
                        
                        for (int i = curlyStart + 1; i < dataArray.length(); i++) {
                            char c = dataArray.charAt(i);
                            if (c == '{') curlyCount++;
                            else if (c == '}') {
                                curlyCount--;
                                if (curlyCount == 0) {
                                    curlyEnd = i;
                                    break;
                                }
                            }
                        }
                        
                        if (curlyEnd > curlyStart) {
                            // Tam bir JSON nesnesi bulundu
                            String requestJson = dataArray.substring(curlyStart, curlyEnd + 1);
                            AdminRequest request = parseAdminRequestObject(requestJson);
                            if (request != null) {
                                requests.add(request);
                            }
                            
                            // Sonraki nesneden devam et
                            objStartIdx = curlyEnd + 1;
                        } else {
                            // Nesne kapanmamış, çık
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JSON parse hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return requests;
    }
    
    /**
     * Tek bir admin request objesini parse eder
     * 
     * AdminApprovalRequest yapısı:
     * {
     *   "id": number,
     *   "admin": { "id": number, "name": string, "email": string, ... },
     *   "approvedBy": object | null,
     *   "approvedAt": string | null,
     *   "requestedAt": string,
     *   "updateAt": string | null,
     *   "createdAt": string,
     *   "status": string,
     *   "note": string | null
     * }
     */
    private AdminRequest parseAdminRequestObject(String requestStr) {
        try {
            System.out.println("Parsing request object: " + requestStr);
            
            // Ana request bilgileri - adminApprovalRequest ID'si (onay/red için bu ID kullanılmalı)
            String id = extractJsonValue(requestStr, "id");
            String requestedAt = extractJsonValue(requestStr, "requestedAt");
            String status = extractJsonValue(requestStr, "status");
            
            // Admin bilgilerini al
            String adminSection = null;
            int adminStartIdx = requestStr.indexOf("\"admin\":{");
            if (adminStartIdx >= 0) {
                int adminEndIdx = findMatchingBrace(requestStr, adminStartIdx + 8);
                if (adminEndIdx > 0) {
                    adminSection = requestStr.substring(adminStartIdx + 8, adminEndIdx + 1);
                }
            }
            
            // Admin bilgileri
            String name = null;
            String surname = null;
            String email = null;
            String telephone = null;
            String adminId = null;
            
            if (adminSection != null) {
                // Admin ID'sini al
                adminId = extractJsonValue(adminSection, "id");
                System.out.println("Parsed Admin ID: " + adminId);
                
                // UserNumber (telefon numarası) doğrudan admin nesnesi içinde
                telephone = extractJsonValue(adminSection, "userNumber");
                
                // profileInfo nesnesini bul
                int profileInfoStartIdx = adminSection.indexOf("\"profileInfo\":{");
                if (profileInfoStartIdx >= 0) {
                    int profileInfoEndIdx = findMatchingBrace(adminSection, profileInfoStartIdx + 14);
                    if (profileInfoEndIdx > 0) {
                        String profileInfoSection = adminSection.substring(profileInfoStartIdx + 14, profileInfoEndIdx + 1);
                        name = extractJsonValue(profileInfoSection, "name");
                        surname = extractJsonValue(profileInfoSection, "surname");
                        email = extractJsonValue(profileInfoSection, "email");
                    }
                }
            }
            
            // API'den gelen status değerini kontrol et ve uygun formata dönüştür
            String displayStatus = "Beklemede";
            if (status != null) {
                if (status.equalsIgnoreCase("APPROVED")) {
                    displayStatus = "Onaylandı";
                } else if (status.equalsIgnoreCase("REJECTED")) {
                    displayStatus = "Reddedildi";
                } else if (status.equalsIgnoreCase("PENDING")) {
                    displayStatus = "Beklemede";
                }
            }
            
            // Tam adı oluştur
            String fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");
            fullName = fullName.trim();
            if (fullName.isEmpty()) {
                fullName = "İsimsiz Admin #" + id;
            }
            
            // Tarihi formatla
            String formattedDate = formatDate(requestedAt != null ? requestedAt : extractJsonValue(requestStr, "createdAt"));
            
            return new AdminRequest(
                id != null ? id : "0",
                adminId != null ? adminId : "0",
                fullName, 
                email != null ? email : "", 
                telephone != null ? telephone : "", 
                formattedDate, 
                displayStatus
            );
        } catch (Exception e) {
            System.err.println("Admin request parse hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * JSON içinde başlangıç indexinden itibaren eşleşen parantezi bulur
     * @param json JSON string
     * @param startIdx { karakterinin index'i
     * @return } karakterinin index'i, bulamazsa -1
     */
    private int findMatchingBrace(String json, int startIdx) {
        if (startIdx >= json.length() || json.charAt(startIdx) != '{') {
            return -1;
        }
        
        int count = 1;
        for (int i = startIdx + 1; i < json.length(); i++) {
            if (json.charAt(i) == '{') {
                count++;
            } else if (json.charAt(i) == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * JSON string'den değer çıkarır
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        
        // Değer bir string mi?
        if (startIndex < json.length() && json.charAt(startIndex) == '"') {
            startIndex++; // " karakterini atla
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        } else {
            // Sayısal değer
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) endIndex = json.length();
            String value = json.substring(startIndex, endIndex).trim();
            return value;
        }
        
        return null;
    }
    
    /**
     * ISO date formatını kullanıcı dostu formata çevirir
     */
    private String formatDate(String isoDate) {
        if (isoDate == null) return "";
        
        try {
            // ISO format: 2025-07-08T10:30:00
            LocalDateTime dateTime = LocalDateTime.parse(isoDate.split("\\.")[0]); // Millisecond kısmını kaldır
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return isoDate; // Parse edilemezse orjinal formatı döndür
        }
    }
    
    private void approveAdminRequest(AdminRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Onay İsteği");
        confirmAlert.setHeaderText("Admin Onayı");
        confirmAlert.setContentText("\"" + request.getName() + "\" isimli kullanıcının admin hesabını onaylamak istediğinize emin misiniz?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Admin onaylanıyor...");
                
                CompletableFuture.runAsync(() -> {
                    try {
                        // Admin ID değerini al - Önemli: Onay isteği ID'si değil, admin kullanıcı ID'si kullanılmalı
                        long adminId = Long.parseLong(request.getAdminId());
                        System.out.println("Onaylama işlemi başlatılıyor - Admin ID: " + adminId + ", Request ID: " + request.getId());
                        System.out.println("Admin Bilgileri: " + request.getName() + ", " + request.getEmail() + ", " + request.getPhone());
                        
                        System.out.println("API çağrısı yapılıyor: adminId=" + adminId);
                        // API çağrısı
                        String apiResponse = ApiClientFX.approveAdminRequest(accessToken, adminId);
                        System.out.println("Admin onay API yanıtı: " + apiResponse);
                        
                        // Yanıt başarılı mı kontrol et
                        boolean isSuccess = apiResponse.contains("\"success\":true") || 
                                           apiResponse.contains("başarılı") || 
                                           !apiResponse.contains("error");
                        
                        if (isSuccess) {
                            System.out.println("Onay işlemi başarılı: " + adminId);
                            
                            // UI thread'inde güncelleme yap
                            Platform.runLater(() -> {
                                request.setStatus("Onaylandı");
                                tableView.refresh();
                                showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla onaylandı.");
                                statusLabel.setText("Admin onaylandı: " + request.getName() + " - " + 
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                                
                                // Tüm listeyi yeniden yükle (güncel durumu görmek için)
                                loadAdminRequests();
                            });
                        } else {
                            System.err.println("API yanıtında başarı bilgisi bulunamadı: " + apiResponse);
                            Platform.runLater(() -> {
                                statusLabel.setText("Onay işlemi tamamlandı ancak sonuç belirsiz. Listeyi yeniliyorum...");
                                loadAdminRequests(); // Yine de listeyi yenile
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Admin onay hatası: " + e.getMessage());
                        e.printStackTrace(); // Stack trace ekleyerek daha detaylı hata bilgisi
                        Platform.runLater(() -> {
                            statusLabel.setText("Onay işlemi başarısız: " + e.getMessage());
                            showErrorAlert("Onay İşlemi Başarısız", "Admin onaylanırken bir hata oluştu: " + e.getMessage());
                        });
                    }
                });
            }
        });
    }
    
    private void rejectAdminRequest(AdminRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Red İsteği");
        confirmAlert.setHeaderText("Admin Reddi");
        confirmAlert.setContentText("\"" + request.getName() + "\" isimli kullanıcının admin hesabını reddetmek istediğinize emin misiniz?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Admin reddediliyor...");
                
                CompletableFuture.runAsync(() -> {
                    try {
                        // Admin ID değerini al - Önemli: Onay isteği ID'si değil, admin kullanıcı ID'si kullanılmalı
                        long adminId = Long.parseLong(request.getAdminId());
                        System.out.println("Reddetme işlemi başlatılıyor - Admin ID: " + adminId + ", Request ID: " + request.getId());
                        System.out.println("Admin Bilgileri: " + request.getName() + ", " + request.getEmail() + ", " + request.getPhone());
                        
                        System.out.println("API çağrısı yapılıyor: adminId=" + adminId);
                        // API çağrısı
                        String apiResponse = ApiClientFX.rejectAdminRequest(accessToken, adminId);
                        System.out.println("Admin red API yanıtı: " + apiResponse);
                        
                        // Yanıt başarılı mı kontrol et
                        boolean isSuccess = apiResponse.contains("\"success\":true") || 
                                           apiResponse.contains("başarılı") || 
                                           !apiResponse.contains("error");
                        
                        if (isSuccess) {
                            System.out.println("Reddetme işlemi başarılı: " + adminId);
                            
                            // UI thread'inde güncelleme yap
                            Platform.runLater(() -> {
                                request.setStatus("Reddedildi");
                                tableView.refresh();
                                showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla reddedildi.");
                                statusLabel.setText("Admin reddedildi: " + request.getName() + " - " + 
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                                
                                // Tüm listeyi yeniden yükle (güncel durumu görmek için)
                                loadAdminRequests();
                            });
                        } else {
                            System.err.println("API yanıtında başarı bilgisi bulunamadı: " + apiResponse);
                            Platform.runLater(() -> {
                                statusLabel.setText("Red işlemi tamamlandı ancak sonuç belirsiz. Listeyi yeniliyorum...");
                                loadAdminRequests(); // Yine de listeyi yenile
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Admin red hatası: " + e.getMessage());
                        e.printStackTrace(); // Stack trace ekleyerek daha detaylı hata bilgisi
                        Platform.runLater(() -> {
                            statusLabel.setText("Red işlemi başarısız: " + e.getMessage());
                            showErrorAlert("Red İşlemi Başarısız", "Admin reddedilirken bir hata oluştu: " + e.getMessage());
                        });
                    }
                });
            }
        });
    }
    
    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
