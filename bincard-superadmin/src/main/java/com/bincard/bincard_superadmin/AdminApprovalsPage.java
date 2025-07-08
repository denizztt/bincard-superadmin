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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminApprovalsPage extends SuperadminPageBase {

    private TableView<AdminRequest> tableView;
    private Label statusLabel;

    // Admin onay talebi sınıfı
    public static class AdminRequest {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String requestDate;
        private String status;

        public AdminRequest(String id, String name, String email, String phone, String requestDate, String status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.requestDate = requestDate;
            this.status = status;
        }

        public String getId() { return id; }
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
            // Backend'den gelen format: {"success": true, "data": [...], "message": "..."}
            
            if (jsonResponse.contains("\"data\":[")) {
                String dataSection = jsonResponse.split("\"data\":")[1];
                if (dataSection.startsWith("[")) {
                    dataSection = dataSection.substring(1); // [ kaldır
                    int endIndex = dataSection.lastIndexOf("]");
                    if (endIndex > 0) {
                        dataSection = dataSection.substring(0, endIndex);
                    }
                    
                    // Her bir admin request objesini parse et
                    String[] requestObjects = dataSection.split("\\},\\s*\\{");
                    
                    for (String requestStr : requestObjects) {
                        // { ve } karakterlerini temizle
                        requestStr = requestStr.replace("{", "").replace("}", "");
                        
                        AdminRequest request = parseAdminRequestObject(requestStr);
                        if (request != null) {
                            requests.add(request);
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
     */
    private AdminRequest parseAdminRequestObject(String requestStr) {
        try {
            String id = extractJsonValue(requestStr, "id");
            String name = extractJsonValue(requestStr, "name");
            String surname = extractJsonValue(requestStr, "surname");
            String email = extractJsonValue(requestStr, "email");
            String telephone = extractJsonValue(requestStr, "telephone");
            String createdAt = extractJsonValue(requestStr, "createdAt");
            String status = extractJsonValue(requestStr, "status");
            
            // Tam adı oluştur
            String fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");
            fullName = fullName.trim();
            
            // Tarihi formatla
            String formattedDate = formatDate(createdAt);
            
            return new AdminRequest(
                id != null ? id : "0", 
                fullName, 
                email != null ? email : "", 
                telephone != null ? telephone : "", 
                formattedDate, 
                status != null ? status : "Beklemede"
            );
        } catch (Exception e) {
            System.err.println("Admin request parse hatası: " + e.getMessage());
            return null;
        }
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
                        // Gerçek API çağrısı
                        long adminId = Long.parseLong(request.getId());
                        String apiResponse = ApiClientFX.approveAdminRequest(accessToken, adminId);
                        System.out.println("Admin onay API yanıtı: " + apiResponse);
                        
                        // UI thread'inde güncelleme yap
                        Platform.runLater(() -> {
                            request.setStatus("Onaylandı");
                            tableView.refresh();
                            showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla onaylandı.");
                            statusLabel.setText("Admin onaylandı: " + request.getName() + " - " + 
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                        });
                    } catch (Exception e) {
                        System.err.println("Admin onay hatası: " + e.getMessage());
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
                        // Gerçek API çağrısı
                        long adminId = Long.parseLong(request.getId());
                        String apiResponse = ApiClientFX.rejectAdminRequest(accessToken, adminId);
                        System.out.println("Admin red API yanıtı: " + apiResponse);
                        
                        // UI thread'inde güncelleme yap
                        Platform.runLater(() -> {
                            request.setStatus("Reddedildi");
                            tableView.refresh();
                            showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla reddedildi.");
                            statusLabel.setText("Admin reddedildi: " + request.getName() + " - " + 
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                        });
                    } catch (Exception e) {
                        System.err.println("Admin red hatası: " + e.getMessage());
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
