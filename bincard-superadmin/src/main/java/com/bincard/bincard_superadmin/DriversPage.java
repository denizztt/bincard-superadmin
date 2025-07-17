package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DriversPage extends SuperadminPageBase {

    // Örnek şoför verileri (gerçek uygulamada API'den gelecek)
    private static class Driver {
        private String id;
        private String name;
        private String phone;
        private String licenseNumber;
        private LocalDate licenseExpiryDate;
        private String status;

        public Driver(String id, String name, String phone, String licenseNumber, LocalDate licenseExpiryDate, String status) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.licenseNumber = licenseNumber;
            this.licenseExpiryDate = licenseExpiryDate;
            this.status = status;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getLicenseNumber() { return licenseNumber; }
        public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
        public String getLicenseExpiryDateString() {
            return licenseExpiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        public String getStatus() { return status; }
    }

    public DriversPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Şoförler");
    }

    @Override
    protected Node createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);

        // Üst kısım - Arama ve filtreler
        HBox topControls = createTopControls();

        // Tablo
        TableView<Driver> driverTable = createDriverTable();
        VBox.setVgrow(driverTable, Priority.ALWAYS);

        // Alt kısım - CRUD butonları
        HBox bottomControls = createBottomControls(driverTable);

        content.getChildren().addAll(topControls, driverTable, bottomControls);
        return content;
    }

    private HBox createTopControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        // Arama alanı
        TextField searchField = new TextField();
        searchField.setPromptText("Şoför ara (Ad, Telefon...)");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px;");

        Button searchButton = new Button("Ara");
        searchButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");

        // Filtreler
        Label filterLabel = new Label("Filtrele:");
        filterLabel.setStyle("-fx-font-size: 14px;");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "Aktif", "İzinli", "Pasif");
        statusFilter.setValue("Tümü");
        statusFilter.setStyle("-fx-font-size: 14px;");

        // Yenile butonu
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #1E293B; -fx-text-fill: white;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(searchField, searchButton, filterLabel, statusFilter, spacer, refreshButton);
        return controls;
    }

    private TableView<Driver> createDriverTable() {
        TableView<Driver> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14px;");

        // Sütunlar
        TableColumn<Driver, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Driver, String> nameColumn = new TableColumn<>("Ad Soyad");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Driver, String> phoneColumn = new TableColumn<>("Telefon");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Driver, String> licenseColumn = new TableColumn<>("Ehliyet No");
        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));

        TableColumn<Driver, String> expiryColumn = new TableColumn<>("Ehliyet Geçerlilik");
        expiryColumn.setCellValueFactory(new PropertyValueFactory<>("licenseExpiryDateString"));

        TableColumn<Driver, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idColumn, nameColumn, phoneColumn, licenseColumn, expiryColumn, statusColumn);

        // Örnek veriler
        table.getItems().addAll(
                new Driver("1", "Ahmet Yılmaz", "5551234567", "12345678", LocalDate.of(2025, 5, 15), "Aktif"),
                new Driver("2", "Mehmet Kaya", "5551234568", "23456789", LocalDate.of(2024, 8, 22), "Aktif"),
                new Driver("3", "Ayşe Demir", "5551234569", "34567890", LocalDate.of(2023, 12, 10), "İzinli"),
                new Driver("4", "Fatma Şahin", "5551234570", "45678901", LocalDate.of(2026, 3, 5), "Aktif"),
                new Driver("5", "Ali Öztürk", "5551234571", "56789012", LocalDate.of(2022, 11, 30), "Pasif")
        );

        return table;
    }

    private HBox createBottomControls(TableView<Driver> table) {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button addButton = new Button("Yeni Şoför Ekle");
        addButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDriverDialog());

        Button editButton = new Button("Düzenle");
        editButton.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        editButton.setOnAction(e -> {
            Driver selectedDriver = table.getSelectionModel().getSelectedItem();
            if (selectedDriver != null) {
                showEditDriverDialog(selectedDriver);
            } else {
                showAlert("Lütfen düzenlemek için bir şoför seçin.");
            }
        });

        Button deleteButton = new Button("Sil");
        deleteButton.setStyle("-fx-background-color: #64748B; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> {
            Driver selectedDriver = table.getSelectionModel().getSelectedItem();
            if (selectedDriver != null) {
                showDeleteConfirmation(selectedDriver, table);
            } else {
                showAlert("Lütfen silmek için bir şoför seçin.");
            }
        });

        controls.getChildren().addAll(addButton, editButton, deleteButton);
        return controls;
    }

    private void showAddDriverDialog() {
        Dialog<Driver> dialog = new Dialog<>();
        dialog.setTitle("Yeni Şoför Ekle");
        dialog.setHeaderText("Şoför bilgilerini doldurun");

        // "Tamam" ve "İptal" butonları
        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Form alanları
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Ad Soyad");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Telefon");

        TextField licenseField = new TextField();
        licenseField.setPromptText("Ehliyet No");

        DatePicker expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Ehliyet Geçerlilik Tarihi");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aktif", "İzinli", "Pasif");
        statusCombo.setValue("Aktif");
        statusCombo.setPromptText("Durum");

        content.getChildren().addAll(
                new Label("Ad Soyad:"), nameField,
                new Label("Telefon:"), phoneField,
                new Label("Ehliyet No:"), licenseField,
                new Label("Ehliyet Geçerlilik Tarihi:"), expiryDatePicker,
                new Label("Durum:"), statusCombo
        );

        dialog.getDialogPane().setContent(content);

        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                        licenseField.getText().isEmpty() || expiryDatePicker.getValue() == null) {
                    showAlert("Tüm alanları doldurunuz.");
                    return null;
                }

                return new Driver(
                        "0", // Gerçek uygulamada backend tarafından atanacak
                        nameField.getText(),
                        phoneField.getText(),
                        licenseField.getText(),
                        expiryDatePicker.getValue(),
                        statusCombo.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(driver -> {
            // Gerçek uygulamada API'ye gönderilecek
            showAlert("Şoför başarıyla eklendi: " + driver.getName());
        });
    }

    private void showEditDriverDialog(Driver driver) {
        Dialog<Driver> dialog = new Dialog<>();
        dialog.setTitle("Şoför Düzenle");
        dialog.setHeaderText("Şoför bilgilerini güncelleyin");

        // "Güncelle" ve "İptal" butonları
        ButtonType updateButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Form alanları
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField(driver.getName());
        nameField.setPromptText("Ad Soyad");

        TextField phoneField = new TextField(driver.getPhone());
        phoneField.setPromptText("Telefon");

        TextField licenseField = new TextField(driver.getLicenseNumber());
        licenseField.setPromptText("Ehliyet No");

        DatePicker expiryDatePicker = new DatePicker(driver.getLicenseExpiryDate());
        expiryDatePicker.setPromptText("Ehliyet Geçerlilik Tarihi");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aktif", "İzinli", "Pasif");
        statusCombo.setValue(driver.getStatus());
        statusCombo.setPromptText("Durum");

        content.getChildren().addAll(
                new Label("Ad Soyad:"), nameField,
                new Label("Telefon:"), phoneField,
                new Label("Ehliyet No:"), licenseField,
                new Label("Ehliyet Geçerlilik Tarihi:"), expiryDatePicker,
                new Label("Durum:"), statusCombo
        );

        dialog.getDialogPane().setContent(content);

        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                if (nameField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                        licenseField.getText().isEmpty() || expiryDatePicker.getValue() == null) {
                    showAlert("Tüm alanları doldurunuz.");
                    return null;
                }

                return new Driver(
                        driver.getId(),
                        nameField.getText(),
                        phoneField.getText(),
                        licenseField.getText(),
                        expiryDatePicker.getValue(),
                        statusCombo.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedDriver -> {
            // Gerçek uygulamada API'ye gönderilecek
            showAlert("Şoför başarıyla güncellendi: " + updatedDriver.getName());
        });
    }

    private void showDeleteConfirmation(Driver driver, TableView<Driver> table) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Şoför Sil");
        alert.setHeaderText("Şoför Silme Onayı");
        alert.setContentText("\"" + driver.getName() + "\" isimli şoförü silmek istediğinize emin misiniz?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Gerçek uygulamada API'ye gönderilecek
                table.getItems().remove(driver);
                showAlert("Şoför başarıyla silindi: " + driver.getName());
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}