package com.bincard.bincard_superadmin;

import javafx.application.HostServices;
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
        // Gerçek uygulamada API'dan verileri çekersiniz
        // Şimdilik demo verilerle dolduralım
        statusLabel.setText("Admin onay istekleri yükleniyor...");
        
        CompletableFuture.supplyAsync(() -> {
            // API'dan verileri çekme simülasyonu
            try {
                Thread.sleep(1000); // Simülasyon için gecikme
                
                // Demo veriler - Gerçek uygulamada API'dan gelecek
                ObservableList<AdminRequest> requests = FXCollections.observableArrayList();
                requests.add(new AdminRequest("1", "Ahmet Yılmaz", "ahmet.yilmaz@mail.com", "+905551234567", "2025-06-30 10:15", "Beklemede"));
                requests.add(new AdminRequest("2", "Mehmet Demir", "mehmet.demir@mail.com", "+905552345678", "2025-07-01 14:30", "Beklemede"));
                requests.add(new AdminRequest("3", "Ayşe Kara", "ayse.kara@mail.com", "+905553456789", "2025-07-02 09:45", "Onaylandı"));
                requests.add(new AdminRequest("4", "Fatma Şahin", "fatma.sahin@mail.com", "+905554567890", "2025-07-02 16:20", "Reddedildi"));
                requests.add(new AdminRequest("5", "Ali Öztürk", "ali.ozturk@mail.com", "+905555678901", "2025-07-03 08:10", "Beklemede"));
                
                return requests;
            } catch (Exception e) {
                throw new RuntimeException("Admin istekleri yüklenirken hata oluştu", e);
            }
        }).thenAcceptAsync(requests -> {
            // UI thread'inde çalış
            tableView.setItems(requests);
            statusLabel.setText("Son güncelleme: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
        }, javafx.application.Platform::runLater).exceptionally(e -> {
            javafx.application.Platform.runLater(() -> {
                statusLabel.setText("Hata: " + e.getMessage());
                showErrorAlert("Admin İstekleri Yüklenemedi", "Admin istekleri yüklenirken bir hata oluştu: " + e.getMessage());
            });
            return null;
        });
    }
    
    private void approveAdminRequest(AdminRequest request) {
        // Gerçek uygulamada API çağrısı yapılacak
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Onay İsteği");
        confirmAlert.setHeaderText("Admin Onayı");
        confirmAlert.setContentText("\"" + request.getName() + "\" isimli kullanıcının admin hesabını onaylamak istediğinize emin misiniz?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Admin onaylanıyor...");
                
                CompletableFuture.runAsync(() -> {
                    try {
                        // API isteği simülasyonu
                        Thread.sleep(1000);
                        
                        // UI thread'inde güncelleme yap
                        javafx.application.Platform.runLater(() -> {
                            request.setStatus("Onaylandı");
                            tableView.refresh();
                            showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla onaylandı.");
                            statusLabel.setText("Admin onaylandı: " + request.getName() + " - " + 
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showErrorAlert("Onay Hatası", "Admin onaylanırken bir hata oluştu: " + e.getMessage());
                            statusLabel.setText("Hata: Admin onaylanamadı");
                        });
                    }
                });
            }
        });
    }
    
    private void rejectAdminRequest(AdminRequest request) {
        // Gerçek uygulamada API çağrısı yapılacak
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Red İsteği");
        confirmAlert.setHeaderText("Admin Reddi");
        confirmAlert.setContentText("\"" + request.getName() + "\" isimli kullanıcının admin hesabını reddetmek istediğinize emin misiniz?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Admin talebi reddediliyor...");
                
                CompletableFuture.runAsync(() -> {
                    try {
                        // API isteği simülasyonu
                        Thread.sleep(1000);
                        
                        // UI thread'inde güncelleme yap
                        javafx.application.Platform.runLater(() -> {
                            request.setStatus("Reddedildi");
                            tableView.refresh();
                            showSuccessAlert("İşlem Başarılı", "Admin hesabı başarıyla reddedildi.");
                            statusLabel.setText("Admin reddedildi: " + request.getName() + " - " + 
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss")));
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showErrorAlert("Red Hatası", "Admin talebi reddedilirken bir hata oluştu: " + e.getMessage());
                            statusLabel.setText("Hata: Admin talebi reddedilemedi");
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
