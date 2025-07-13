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
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Yeni ödeme noktası ekleme sayfası
 */
public class PaymentPointAddPage extends SuperadminPageBase {

    // Form alanları
    private TextField nameField;
    private TextField latitudeField;
    private TextField longitudeField;
    private TextField streetField;
    private TextField districtField;
    private TextField cityField;
    private TextField postalCodeField;
    private TextField contactNumberField;
    private TextField workingHoursField;
    private TextArea descriptionArea;
    private CheckBox activeCheckBox;
    
    // Ödeme yöntemleri için checkbox'lar
    private CheckBox cashCheckBox;
    private CheckBox creditCardCheckBox;
    private CheckBox debitCardCheckBox;
    private CheckBox mobileAppCheckBox;
    private CheckBox qrCodeCheckBox;
    
    // Butonlar
    private Button saveButton;
    private Button cancelButton;
    private Button clearButton;

    public PaymentPointAddPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Ödeme Noktası Ekle");
    }

    @Override
    protected Node createContent() {
        // Ana container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        Label titleLabel = new Label("Yeni Ödeme Noktası Ekle");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // İkon ile başlık container'ı
        HBox titleContainer = new HBox(15);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        FontIcon titleIcon = new FontIcon(FontAwesomeSolid.PLUS_CIRCLE);
        titleIcon.setIconSize(28);
        titleIcon.setIconColor(Color.web("#2c3e50"));
        titleContainer.getChildren().addAll(titleIcon, titleLabel);

        // Form container
        VBox formContainer = createFormContainer();
        
        // Buton container
        HBox buttonContainer = createButtonContainer();

        // Ana container'a ekle
        mainContainer.getChildren().addAll(titleContainer, formContainer, buttonContainer);

        // ScrollPane içine al
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        return scrollPane;
    }

    private VBox createFormContainer() {
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Temel Bilgiler Bölümü
        VBox basicInfoSection = createBasicInfoSection();
        
        // Konum Bilgileri Bölümü
        VBox locationSection = createLocationSection();
        
        // Adres Bilgileri Bölümü
        VBox addressSection = createAddressSection();
        
        // İletişim ve Çalışma Saatleri Bölümü
        VBox contactSection = createContactSection();
        
        // Ödeme Yöntemleri Bölümü
        VBox paymentMethodsSection = createPaymentMethodsSection();
        
        // Açıklama ve Durum Bölümü
        VBox descriptionSection = createDescriptionSection();

        formContainer.getChildren().addAll(
            basicInfoSection,
            createSeparator(),
            locationSection,
            createSeparator(),
            addressSection,
            createSeparator(),
            contactSection,
            createSeparator(),
            paymentMethodsSection,
            createSeparator(),
            descriptionSection
        );

        return formContainer;
    }

    private VBox createBasicInfoSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("Temel Bilgiler");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // Ödeme noktası adı
        VBox nameContainer = createFormField("Ödeme Noktası Adı *", "Merkez Ödeme Noktası");
        nameField = (TextField) ((VBox) nameContainer.getChildren().get(1)).getChildren().get(0);
        
        section.getChildren().addAll(sectionTitle, nameContainer);
        return section;
    }

    private VBox createLocationSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("Konum Bilgileri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // Konum alanları için grid
        HBox locationGrid = new HBox(20);
        locationGrid.setAlignment(Pos.CENTER_LEFT);
        
        // Enlem
        VBox latitudeContainer = createFormField("Enlem (Latitude) *", "41.0082");
        latitudeField = (TextField) ((VBox) latitudeContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(latitudeContainer, Priority.ALWAYS);
        
        // Boylam
        VBox longitudeContainer = createFormField("Boylam (Longitude) *", "28.9784");
        longitudeField = (TextField) ((VBox) longitudeContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(longitudeContainer, Priority.ALWAYS);
        
        locationGrid.getChildren().addAll(latitudeContainer, longitudeContainer);
        
        // Konum yardım metni
        Label locationHelp = new Label("💡 Konum bilgilerini haritadan alabilir veya GPS koordinatlarını girebilirsiniz.");
        locationHelp.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        locationHelp.setTextFill(Color.web("#7f8c8d"));
        
        section.getChildren().addAll(sectionTitle, locationGrid, locationHelp);
        return section;
    }

    private VBox createAddressSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("Adres Bilgileri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // Sokak adresi
        VBox streetContainer = createFormField("Sokak Adresi", "Atatürk Caddesi No: 123");
        streetField = (TextField) ((VBox) streetContainer.getChildren().get(1)).getChildren().get(0);
        
        // İlçe ve Şehir
        HBox cityGrid = new HBox(20);
        cityGrid.setAlignment(Pos.CENTER_LEFT);
        
        VBox districtContainer = createFormField("İlçe", "Kadıköy");
        districtField = (TextField) ((VBox) districtContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(districtContainer, Priority.ALWAYS);
        
        VBox cityContainer = createFormField("Şehir *", "İstanbul");
        cityField = (TextField) ((VBox) cityContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(cityContainer, Priority.ALWAYS);
        
        cityGrid.getChildren().addAll(districtContainer, cityContainer);
        
        // Posta kodu
        VBox postalCodeContainer = createFormField("Posta Kodu", "34000");
        postalCodeField = (TextField) ((VBox) postalCodeContainer.getChildren().get(1)).getChildren().get(0);
        postalCodeField.setPrefWidth(150);
        
        section.getChildren().addAll(sectionTitle, streetContainer, cityGrid, postalCodeContainer);
        return section;
    }

    private VBox createContactSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("İletişim ve Çalışma Saatleri");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // İletişim ve çalışma saatleri
        HBox contactGrid = new HBox(20);
        contactGrid.setAlignment(Pos.CENTER_LEFT);
        
        VBox contactContainer = createFormField("İletişim Numarası", "+90 212 123 45 67");
        contactNumberField = (TextField) ((VBox) contactContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(contactContainer, Priority.ALWAYS);
        
        VBox workingHoursContainer = createFormField("Çalışma Saatleri", "09:00 - 18:00");
        workingHoursField = (TextField) ((VBox) workingHoursContainer.getChildren().get(1)).getChildren().get(0);
        HBox.setHgrow(workingHoursContainer, Priority.ALWAYS);
        
        contactGrid.getChildren().addAll(contactContainer, workingHoursContainer);
        
        section.getChildren().addAll(sectionTitle, contactGrid);
        return section;
    }

    private VBox createPaymentMethodsSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("Ödeme Yöntemleri *");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // Ödeme yöntemleri grid
        VBox paymentMethodsGrid = new VBox(10);
        
        // İlk satır
        HBox row1 = new HBox(30);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        cashCheckBox = new CheckBox("Nakit");
        cashCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        cashCheckBox.setSelected(true); // Varsayılan olarak seçili
        
        creditCardCheckBox = new CheckBox("Kredi Kartı");
        creditCardCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        creditCardCheckBox.setSelected(true); // Varsayılan olarak seçili
        
        debitCardCheckBox = new CheckBox("Banka Kartı");
        debitCardCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        row1.getChildren().addAll(cashCheckBox, creditCardCheckBox, debitCardCheckBox);
        
        // İkinci satır
        HBox row2 = new HBox(30);
        row2.setAlignment(Pos.CENTER_LEFT);
        
        mobileAppCheckBox = new CheckBox("Mobil Uygulama");
        mobileAppCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        qrCodeCheckBox = new CheckBox("QR Kod");
        qrCodeCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        row2.getChildren().addAll(mobileAppCheckBox, qrCodeCheckBox);
        
        paymentMethodsGrid.getChildren().addAll(row1, row2);
        
        // Yardım metni
        Label paymentHelp = new Label("💡 En az bir ödeme yöntemi seçilmelidir.");
        paymentHelp.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        paymentHelp.setTextFill(Color.web("#7f8c8d"));
        
        section.getChildren().addAll(sectionTitle, paymentMethodsGrid, paymentHelp);
        return section;
    }

    private VBox createDescriptionSection() {
        VBox section = new VBox(15);
        
        // Bölüm başlığı
        Label sectionTitle = new Label("Açıklama ve Durum");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#34495e"));
        
        // Açıklama alanı
        VBox descriptionContainer = new VBox(8);
        Label descriptionLabel = new Label("Açıklama");
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        descriptionLabel.setTextFill(Color.web("#34495e"));
        
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Ödeme noktası hakkında detaylı bilgi yazabilirsiniz...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setMaxHeight(120);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; " +
                                "-fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14px;");
        
        descriptionContainer.getChildren().addAll(descriptionLabel, descriptionArea);
        
        // Aktif durumu
        activeCheckBox = new CheckBox("Aktif");
        activeCheckBox.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        activeCheckBox.setSelected(true); // Varsayılan olarak aktif
        activeCheckBox.setTextFill(Color.web("#27ae60"));
        
        section.getChildren().addAll(sectionTitle, descriptionContainer, activeCheckBox);
        return section;
    }

    private VBox createFormField(String labelText, String placeholder) {
        VBox fieldContainer = new VBox(8);
        
        // Label
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        label.setTextFill(Color.web("#34495e"));
        
        // Field container
        VBox inputContainer = new VBox();
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setPrefHeight(40);
        textField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; " +
                          "-fx-background-radius: 5; -fx-padding: 0 10; -fx-font-size: 14px;");
        
        // Focus efektleri
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 2; " +
                                  "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 0 10; -fx-font-size: 14px;");
            } else {
                textField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; " +
                                  "-fx-background-radius: 5; -fx-padding: 0 10; -fx-font-size: 14px;");
            }
        });
        
        inputContainer.getChildren().add(textField);
        fieldContainer.getChildren().addAll(label, inputContainer);
        
        return fieldContainer;
    }

    private HBox createButtonContainer() {
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Temizle butonu
        clearButton = new Button("Temizle");
        clearButton.setPrefWidth(120);
        clearButton.setPrefHeight(40);
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        clearButton.setOnAction(e -> clearForm());
        
        // İptal butonu
        cancelButton = new Button("İptal");
        cancelButton.setPrefWidth(120);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; " +
                             "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> goBack());
        
        // Kaydet butonu
        saveButton = new Button("Kaydet");
        saveButton.setPrefWidth(120);
        saveButton.setPrefHeight(40);
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        saveButton.setOnAction(e -> savePaymentPoint());
        
        // Hover efektleri
        addButtonHoverEffect(clearButton, "#7f8c8d");
        addButtonHoverEffect(cancelButton, "#c0392b");
        addButtonHoverEffect(saveButton, "#229954");
        
        buttonContainer.getChildren().addAll(clearButton, cancelButton, saveButton);
        return buttonContainer;
    }

    private void addButtonHoverEffect(Button button, String hoverColor) {
        String originalStyle = button.getStyle();
        button.setOnMouseEntered(e -> {
            String newStyle = originalStyle.replace(button.getStyle().split("-fx-background-color: ")[1].split(";")[0], hoverColor);
            button.setStyle(newStyle);
        });
        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");
        return separator;
    }

    private void clearForm() {
        // Tüm alanları temizle
        nameField.clear();
        latitudeField.clear();
        longitudeField.clear();
        streetField.clear();
        districtField.clear();
        cityField.clear();
        postalCodeField.clear();
        contactNumberField.clear();
        workingHoursField.clear();
        descriptionArea.clear();
        
        // Checkbox'ları sıfırla
        cashCheckBox.setSelected(true);
        creditCardCheckBox.setSelected(true);
        debitCardCheckBox.setSelected(false);
        mobileAppCheckBox.setSelected(false);
        qrCodeCheckBox.setSelected(false);
        activeCheckBox.setSelected(true);
        
        // İlk alana focus
        nameField.requestFocus();
    }

    private void savePaymentPoint() {
        // Validasyon kontrolü
        if (!validateForm()) {
            return;
        }
        
        // Ödeme noktası verilerini hazırla
        String paymentPointData = createPaymentPointJson();
        
        // API'ye kaydet
        saveButton.setDisable(true);
        saveButton.setText("Kaydediliyor...");
        
        // Asenkron API çağrısı
        new Thread(() -> {
            try {
                String response = PaymentPointApiClient.addPaymentPoint(paymentPointData, accessToken);
                
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Kaydet");
                    
                    if (response.contains("success") || response.contains("created")) {
                        showSuccessAlert("Ödeme noktası başarıyla eklendi!");
                        clearForm();
                    } else {
                        showErrorAlert("Ödeme noktası eklenirken bir hata oluştu: " + response);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Kaydet");
                    showErrorAlert("Ödeme noktası eklenirken bir hata oluştu: " + e.getMessage());
                });
            }
        }).start();
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();
        
        // Zorunlu alanları kontrol et
        if (nameField.getText().trim().isEmpty()) {
            errors.add("Ödeme noktası adı zorunludur.");
        }
        
        if (latitudeField.getText().trim().isEmpty()) {
            errors.add("Enlem bilgisi zorunludur.");
        } else {
            try {
                Double.parseDouble(latitudeField.getText().trim());
            } catch (NumberFormatException e) {
                errors.add("Enlem bilgisi geçerli bir sayı olmalıdır.");
            }
        }
        
        if (longitudeField.getText().trim().isEmpty()) {
            errors.add("Boylam bilgisi zorunludur.");
        } else {
            try {
                Double.parseDouble(longitudeField.getText().trim());
            } catch (NumberFormatException e) {
                errors.add("Boylam bilgisi geçerli bir sayı olmalıdır.");
            }
        }
        
        if (cityField.getText().trim().isEmpty()) {
            errors.add("Şehir bilgisi zorunludur.");
        }
        
        // En az bir ödeme yöntemi seçili olmalı
        if (!cashCheckBox.isSelected() && !creditCardCheckBox.isSelected() && 
            !debitCardCheckBox.isSelected() && !mobileAppCheckBox.isSelected() && 
            !qrCodeCheckBox.isSelected()) {
            errors.add("En az bir ödeme yöntemi seçilmelidir.");
        }
        
        // Uzunluk kontrolü
        if (nameField.getText().length() > 150) {
            errors.add("Ödeme noktası adı 150 karakterden fazla olamaz.");
        }
        
        if (descriptionArea.getText().length() > 1000) {
            errors.add("Açıklama 1000 karakterden fazla olamaz.");
        }
        
        // Hata varsa kullanıcıya göster
        if (!errors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Lütfen aşağıdaki hataları düzeltin:\n\n");
            for (String error : errors) {
                errorMessage.append("• ").append(error).append("\n");
            }
            
            showErrorAlert(errorMessage.toString());
            return false;
        }
        
        return true;
    }

    private String createPaymentPointJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": \"").append(nameField.getText().trim()).append("\",\n");
        json.append("  \"location\": {\n");
        json.append("    \"latitude\": ").append(latitudeField.getText().trim()).append(",\n");
        json.append("    \"longitude\": ").append(longitudeField.getText().trim()).append("\n");
        json.append("  },\n");
        json.append("  \"address\": {\n");
        json.append("    \"street\": \"").append(streetField.getText().trim()).append("\",\n");
        json.append("    \"district\": \"").append(districtField.getText().trim()).append("\",\n");
        json.append("    \"city\": \"").append(cityField.getText().trim()).append("\",\n");
        json.append("    \"postalCode\": \"").append(postalCodeField.getText().trim()).append("\"\n");
        json.append("  },\n");
        json.append("  \"contactNumber\": \"").append(contactNumberField.getText().trim()).append("\",\n");
        json.append("  \"workingHours\": \"").append(workingHoursField.getText().trim()).append("\",\n");
        json.append("  \"paymentMethods\": [");
        
        // Ödeme yöntemlerini ekle
        List<String> selectedMethods = new ArrayList<>();
        if (cashCheckBox.isSelected()) selectedMethods.add("\"CASH\"");
        if (creditCardCheckBox.isSelected()) selectedMethods.add("\"CREDIT_CARD\"");
        if (debitCardCheckBox.isSelected()) selectedMethods.add("\"DEBIT_CARD\"");
        if (mobileAppCheckBox.isSelected()) selectedMethods.add("\"MOBILE_APP\"");
        if (qrCodeCheckBox.isSelected()) selectedMethods.add("\"QR_CODE\"");
        
        json.append(String.join(", ", selectedMethods));
        json.append("],\n");
        json.append("  \"description\": \"").append(descriptionArea.getText().trim()).append("\",\n");
        json.append("  \"active\": ").append(activeCheckBox.isSelected()).append("\n");
        json.append("}");
        
        return json.toString();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText("İşlem Başarılı");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText("İşlem Başarısız");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBack() {
        // Ödeme noktaları listesine geri dön
        new PaymentPointsTablePage(stage, accessToken, refreshToken);
    }
}
