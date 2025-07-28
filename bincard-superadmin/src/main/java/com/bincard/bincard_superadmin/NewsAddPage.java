package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Haber Ekleme sayfası - NewsPage'deki dialog ile aynı işlevsellik
 */
public class NewsAddPage extends SuperadminPageBase {
    
    // Form alanları
    private TextField titleField;
    private TextArea contentArea;
    private TextField imagePathField;
    private ComboBox<String> typeCombo;
    private ComboBox<String> platformCombo;
    private ComboBox<String> priorityCombo;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> startHourCombo;
    private ComboBox<String> startMinuteCombo;
    private ComboBox<String> endHourCombo;
    private ComboBox<String> endMinuteCombo;
    private CheckBox activeCheckBox;
    private CheckBox allowFeedbackCheckBox;
    private Button saveButton;
    private Button cancelButton;
    private Label statusLabel;
    
    public NewsAddPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Haber Ekle");
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
        content.setStyle("-fx-background-color: #F8FAFC;");
        
        // Başlık
        Label titleLabel = new Label("Yeni Haber Ekle");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Ana form paneli
        VBox formPanel = createFormPanel();
        
        // Buton paneli
        HBox buttonPanel = createButtonPanel();
        
        // Status label
        statusLabel = new Label();
        statusLabel.setVisible(false);
        
        content.getChildren().addAll(titleLabel, formPanel, buttonPanel, statusLabel);
        return content;
    }
    
    /**
     * Form panelini oluşturur
     */
    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Form alanlarını oluştur
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // Başlık
        titleField = new TextField();
        titleField.setPromptText("Haber Başlığı");
        titleField.setPrefWidth(400);
        grid.add(new Label("Başlık:"), 0, 0);
        grid.add(titleField, 1, 0, 3, 1);

        // İçerik
        contentArea = new TextArea();
        contentArea.setPromptText("Haber İçeriği");
        contentArea.setPrefRowCount(5);
        contentArea.setPrefWidth(400);
        grid.add(new Label("İçerik:"), 0, 1);
        grid.add(contentArea, 1, 1, 3, 1);

        // Görsel
        HBox imageBox = createImagePanel();
        grid.add(new Label("Görsel:"), 0, 2);
        grid.add(imageBox, 1, 2, 3, 1);

        // Kategori
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("DUYURU", "KAMPANYA", "BAKIM", "BILGILENDIRME", 
                                   "GUNCELLEME", "UYARI", "ETKINLIK");
        typeCombo.setValue("DUYURU");
        typeCombo.setPromptText("Haber Kategorisi");
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(typeCombo, 1, 3);

        // Platform
        platformCombo = new ComboBox<>();
        platformCombo.getItems().addAll("WEB", "MOBILE", "DESKTOP", "TABLET", "KIOSK", "ALL");
        platformCombo.setValue("WEB");
        platformCombo.setPromptText("Platform");
        grid.add(new Label("Platform:"), 2, 3);
        grid.add(platformCombo, 3, 3);

        // Öncelik
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("COK_DUSUK", "DUSUK", "NORMAL", 
                                       "ORTA_YUKSEK", "YUKSEK", "COK_YUKSEK", "KRITIK");
        priorityCombo.setValue("NORMAL");
        priorityCombo.setPromptText("Öncelik");
        grid.add(new Label("Öncelik:"), 0, 4);
        grid.add(priorityCombo, 1, 4);

        // Tarih alanları
        HBox dateTimePanel = createDateTimePanel();
        grid.add(new Label("Süre:"), 0, 5);
        grid.add(dateTimePanel, 1, 5, 3, 1);

        // Seçenekler
        HBox optionsPanel = createOptionsPanel();
        grid.add(new Label("Seçenekler:"), 0, 6);
        grid.add(optionsPanel, 1, 6, 3, 1);
        
        panel.getChildren().add(grid);
        return panel;
    }
    
    /**
     * Görsel seçim panelini oluşturur
     */
    private HBox createImagePanel() {
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        
        imagePathField = new TextField();
        imagePathField.setPromptText("Resim Yolu");
        imagePathField.setPrefWidth(300);
        
        Button browseButton = new Button("Gözat");
        browseButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
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
        return imageBox;
    }
    
    /**
     * Tarih ve saat seçim panelini oluşturur
     */
    private HBox createDateTimePanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER_LEFT);
        
        // Başlangıç tarihi
        VBox startPanel = new VBox(5);
        startPanel.getChildren().add(new Label("Başlangıç:"));
        
        startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPromptText("Başlangıç Tarihi");
        
        HBox startTimeBox = new HBox(5);
        startHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            startHourCombo.getItems().add(String.format("%02d", i));
        }
        startHourCombo.setValue("00");
        
        startMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            startMinuteCombo.getItems().add(String.format("%02d", i));
        }
        startMinuteCombo.setValue("00");
        
        startTimeBox.getChildren().addAll(startHourCombo, new Label(":"), startMinuteCombo);
        startPanel.getChildren().addAll(startDatePicker, startTimeBox);
        
        // Bitiş tarihi
        VBox endPanel = new VBox(5);
        endPanel.getChildren().add(new Label("Bitiş:"));
        
        endDatePicker = new DatePicker(LocalDate.now().plusMonths(1));
        endDatePicker.setPromptText("Bitiş Tarihi");
        
        HBox endTimeBox = new HBox(5);
        endHourCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            endHourCombo.getItems().add(String.format("%02d", i));
        }
        endHourCombo.setValue("23");
        
        endMinuteCombo = new ComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            endMinuteCombo.getItems().add(String.format("%02d", i));
        }
        endMinuteCombo.setValue("59");
        
        endTimeBox.getChildren().addAll(endHourCombo, new Label(":"), endMinuteCombo);
        endPanel.getChildren().addAll(endDatePicker, endTimeBox);
        
        panel.getChildren().addAll(startPanel, endPanel);
        return panel;
    }
    
    /**
     * Seçenekler panelini oluşturur
     */
    private HBox createOptionsPanel() {
        HBox panel = new HBox(30);
        panel.setAlignment(Pos.CENTER_LEFT);
        
        activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setSelected(true);
        
        allowFeedbackCheckBox = new CheckBox("Geri Bildirime İzin Ver");
        allowFeedbackCheckBox.setSelected(true);
        
        panel.getChildren().addAll(activeCheckBox, allowFeedbackCheckBox);
        return panel;
    }
    
    /**
     * Buton panelini oluşturur
     */
    private HBox createButtonPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 0, 0));
        
        saveButton = new Button("Haberi Kaydet");
        saveButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 12 24;");
        saveButton.setOnAction(e -> saveNews());
        
        cancelButton = new Button("İptal");
        cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-padding: 12 24;");
        cancelButton.setOnAction(e -> {
            // Dashboard'a geri dön
            new SuperadminDashboardFX(stage, accessToken, refreshToken);
        });
        
        panel.getChildren().addAll(saveButton, cancelButton);
        return panel;
    }
    
    /**
     * Haber kaydetme işlemi - NewsPage'deki API çağrısı ile aynı
     */
    private void saveNews() {
        // Validasyon
        if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showAlert("Hata", "Başlık ve içerik alanlarını doldurunuz.");
            return;
        }
        
        // Loading göster
        saveButton.setDisable(true);
        statusLabel.setText("Haber kaydediliyor...");
        statusLabel.setTextFill(Color.BLUE);
        statusLabel.setVisible(true);
        
        CompletableFuture.runAsync(() -> {
            try {
                // Tarih-saat bilgilerini hazırla
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
                
                // Görsel dosyasını oku (varsa)
                byte[] imageData = null;
                String imageName = "";
                if (imagePathField.getText() != null && !imagePathField.getText().isEmpty()) {
                    File imageFile = new File(imagePathField.getText());
                    if (imageFile.exists()) {
                        imageName = imageFile.getName();
                        imageData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    }
                }
                
                // API çağrısı yap
                String response = ApiClientFX.createNews(
                    accessToken,
                    titleField.getText(),
                    contentArea.getText(),
                    imageData,
                    imageName,
                    startDate,
                    endDate,
                    platformCombo.getValue(),
                    priorityCombo.getValue(),
                    typeCombo.getValue(),
                    allowFeedbackCheckBox.isSelected()
                );
                
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    
                    if (response != null && response.contains("success")) {
                        statusLabel.setText("Haber başarıyla kaydedildi!");
                        statusLabel.setTextFill(Color.GREEN);
                        
                        // Form alanlarını temizle
                        clearForm();
                        
                        // 2 saniye sonra dashboard'a dön
                        CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> {
                                    new SuperadminDashboardFX(stage, accessToken, refreshToken);
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        statusLabel.setText("Haber kaydedilirken bir hata oluştu!");
                        statusLabel.setTextFill(Color.RED);
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    statusLabel.setText("Hata: " + e.getMessage());
                    statusLabel.setTextFill(Color.RED);
                });
            }
        });
    }
    
    /**
     * Form alanlarını temizler
     */
    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        imagePathField.clear();
        typeCombo.setValue("DUYURU");
        platformCombo.setValue("WEB");
        priorityCombo.setValue("NORMAL");
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));
        startHourCombo.setValue("00");
        startMinuteCombo.setValue("00");
        endHourCombo.setValue("23");
        endMinuteCombo.setValue("59");
        activeCheckBox.setSelected(true);
        allowFeedbackCheckBox.setSelected(true);
    }
    
    /**
     * Alert gösterir
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}