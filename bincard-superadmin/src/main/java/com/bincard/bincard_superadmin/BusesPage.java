package com.bincard.bincard_superadmin;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BusesPage extends SuperadminPageBase {
    
    // Örnek otobüs verileri (gerçek uygulamada API'den gelecek)
    private static class Bus {
        private String id;
        private String plateNumber;
        private String model;
        private int capacity;
        private String status;
        
        public Bus(String id, String plateNumber, String model, int capacity, String status) {
            this.id = id;
            this.plateNumber = plateNumber;
            this.model = model;
            this.capacity = capacity;
            this.status = status;
        }
        
        public String getId() { return id; }
        public String getPlateNumber() { return plateNumber; }
        public String getModel() { return model; }
        public int getCapacity() { return capacity; }
        public String getStatus() { return status; }
    }

    public BusesPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Otobüsler");
    }

    @Override
    protected Node createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Üst kısım - Arama ve filtreler
        HBox topControls = createTopControls();
        
        // Tablo
        TableView<Bus> busTable = createBusTable();
        VBox.setVgrow(busTable, Priority.ALWAYS);
        
        // Alt kısım - CRUD butonları
        HBox bottomControls = createBottomControls(busTable);
        
        content.getChildren().addAll(topControls, busTable, bottomControls);
        return content;
    }
    
    private HBox createTopControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Arama alanı
        TextField searchField = new TextField();
        searchField.setPromptText("Otobüs ara (Plaka, Model...)");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px;");
        
        Button searchButton = new Button("Ara");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        // Filtreler
        Label filterLabel = new Label("Filtrele:");
        filterLabel.setStyle("-fx-font-size: 14px;");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tümü", "Aktif", "Bakımda", "Arızalı");
        statusFilter.setValue("Tümü");
        statusFilter.setStyle("-fx-font-size: 14px;");
        
        // Yenile butonu
        Button refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        controls.getChildren().addAll(searchField, searchButton, filterLabel, statusFilter, spacer, refreshButton);
        return controls;
    }
    
    private TableView<Bus> createBusTable() {
        TableView<Bus> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14px;");
        
        // Sütunlar
        TableColumn<Bus, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Bus, String> plateColumn = new TableColumn<>("Plaka");
        plateColumn.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        
        TableColumn<Bus, String> modelColumn = new TableColumn<>("Model");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        
        TableColumn<Bus, Integer> capacityColumn = new TableColumn<>("Kapasite");
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        
        TableColumn<Bus, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        table.getColumns().addAll(idColumn, plateColumn, modelColumn, capacityColumn, statusColumn);
        
        // Örnek veriler
        table.getItems().addAll(
            new Bus("1", "34 ABC 123", "Mercedes Citaro", 80, "Aktif"),
            new Bus("2", "34 DEF 456", "MAN Lion's City", 70, "Aktif"),
            new Bus("3", "34 GHI 789", "Otokar Kent", 60, "Bakımda"),
            new Bus("4", "34 JKL 012", "Temsa Avenue", 75, "Aktif"),
            new Bus("5", "34 MNO 345", "BMC Procity", 65, "Arızalı")
        );
        
        return table;
    }
    
    private HBox createBottomControls(TableView<Bus> table) {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_RIGHT);
        
        Button addButton = new Button("Yeni Otobüs Ekle");
        addButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddBusDialog());
        
        Button editButton = new Button("Düzenle");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(e -> {
            Bus selectedBus = table.getSelectionModel().getSelectedItem();
            if (selectedBus != null) {
                showEditBusDialog(selectedBus);
            } else {
                showAlert("Lütfen düzenlemek için bir otobüs seçin.");
            }
        });
        
        Button deleteButton = new Button("Sil");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> {
            Bus selectedBus = table.getSelectionModel().getSelectedItem();
            if (selectedBus != null) {
                showDeleteConfirmation(selectedBus, table);
            } else {
                showAlert("Lütfen silmek için bir otobüs seçin.");
            }
        });
        
        controls.getChildren().addAll(addButton, editButton, deleteButton);
        return controls;
    }
    
    private void showAddBusDialog() {
        Dialog<Bus> dialog = new Dialog<>();
        dialog.setTitle("Yeni Otobüs Ekle");
        dialog.setHeaderText("Otobüs bilgilerini doldurun");
        
        // "Tamam" ve "İptal" butonları
        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Form alanları
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField plateField = new TextField();
        plateField.setPromptText("Plaka");
        
        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Kapasite");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aktif", "Bakımda", "Arızalı");
        statusCombo.setValue("Aktif");
        statusCombo.setPromptText("Durum");
        
        content.getChildren().addAll(
            new Label("Plaka:"), plateField,
            new Label("Model:"), modelField,
            new Label("Kapasite:"), capacityField,
            new Label("Durum:"), statusCombo
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int capacity = Integer.parseInt(capacityField.getText());
                    return new Bus(
                        "0", // Gerçek uygulamada backend tarafından atanacak
                        plateField.getText(),
                        modelField.getText(),
                        capacity,
                        statusCombo.getValue()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Kapasite sayısal bir değer olmalıdır.");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(bus -> {
            // Gerçek uygulamada API'ye gönderilecek
            showAlert("Otobüs başarıyla eklendi: " + bus.getPlateNumber());
        });
    }
    
    private void showEditBusDialog(Bus bus) {
        Dialog<Bus> dialog = new Dialog<>();
        dialog.setTitle("Otobüs Düzenle");
        dialog.setHeaderText("Otobüs bilgilerini güncelleyin");
        
        // "Güncelle" ve "İptal" butonları
        ButtonType updateButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        // Form alanları
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField plateField = new TextField(bus.getPlateNumber());
        plateField.setPromptText("Plaka");
        
        TextField modelField = new TextField(bus.getModel());
        modelField.setPromptText("Model");
        
        TextField capacityField = new TextField(String.valueOf(bus.getCapacity()));
        capacityField.setPromptText("Kapasite");
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aktif", "Bakımda", "Arızalı");
        statusCombo.setValue(bus.getStatus());
        statusCombo.setPromptText("Durum");
        
        content.getChildren().addAll(
            new Label("Plaka:"), plateField,
            new Label("Model:"), modelField,
            new Label("Kapasite:"), capacityField,
            new Label("Durum:"), statusCombo
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Sonucu işle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    int capacity = Integer.parseInt(capacityField.getText());
                    return new Bus(
                        bus.getId(),
                        plateField.getText(),
                        modelField.getText(),
                        capacity,
                        statusCombo.getValue()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Kapasite sayısal bir değer olmalıdır.");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedBus -> {
            // Gerçek uygulamada API'ye gönderilecek
            showAlert("Otobüs başarıyla güncellendi: " + updatedBus.getPlateNumber());
        });
    }
    
    private void showDeleteConfirmation(Bus bus, TableView<Bus> table) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Otobüs Sil");
        alert.setHeaderText("Otobüs Silme Onayı");
        alert.setContentText("\"" + bus.getPlateNumber() + "\" plakalı otobüsü silmek istediğinize emin misiniz?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Gerçek uygulamada API'ye gönderilecek
                table.getItems().remove(bus);
                showAlert("Otobüs başarıyla silindi: " + bus.getPlateNumber());
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