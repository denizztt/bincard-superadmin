package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Cüzdan durumu güncelleme sayfası
 */
public class WalletStatusUpdatePage extends SuperadminPageBase {
    
    private TextField phoneNumberField;
    private CheckBox isActiveCheckBox;
    private Label resultLabel;
    private Button updateButton;

    public WalletStatusUpdatePage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Cüzdan Durumu Güncelleme");
    }

    @Override
    protected Node createContent() {
        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        HBox titleContainer = createTitleContainer();
        
        // Form container
        VBox formContainer = createFormContainer();
        
        // Sonuç alanı
        VBox resultContainer = createResultContainer();

        mainContainer.getChildren().addAll(titleContainer, formContainer, resultContainer);
        
        return mainContainer;
    }
    
    private HBox createTitleContainer() {
        HBox titleContainer = new HBox(15);
        titleContainer.setAlignment(Pos.CENTER);
        
        FontIcon titleIcon = new FontIcon(FontAwesomeSolid.EDIT);
        titleIcon.setIconSize(32);
        titleIcon.setIconColor(Color.web("#2c3e50"));
        
        Label titleLabel = new Label("Cüzdan Durumu Güncelleme");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        titleContainer.getChildren().addAll(titleIcon, titleLabel);
        return titleContainer;
    }
    
    private VBox createFormContainer() {
        VBox formContainer = new VBox(20);
        formContainer.setMaxWidth(500);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
            "-fx-padding: 40;"
        );
        
        // Telefon numarası alanı
        VBox phoneContainer = new VBox(8);
        Label phoneLabel = new Label("Telefon Numarası:");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        phoneLabel.setTextFill(Color.web("#34495e"));
        
        phoneNumberField = new TextField();
        phoneNumberField.setPromptText("örn: +90 532 123 45 67");
        phoneNumberField.setPrefHeight(40);
        phoneNumberField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-border-color: #bdc3c7; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        phoneContainer.getChildren().addAll(phoneLabel, phoneNumberField);
        
        // Durum seçimi
        VBox statusContainer = new VBox(8);
        Label statusLabel = new Label("Cüzdan Durumu:");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        statusLabel.setTextFill(Color.web("#34495e"));
        
        isActiveCheckBox = new CheckBox("Aktif");
        isActiveCheckBox.setSelected(true);
        isActiveCheckBox.setFont(Font.font("Segoe UI", 14));
        isActiveCheckBox.setTextFill(Color.web("#2c3e50"));
        
        statusContainer.getChildren().addAll(statusLabel, isActiveCheckBox);
        
        // Güncelleme butonu
        updateButton = new Button("Cüzdan Durumunu Güncelle");
        updateButton.setPrefHeight(45);
        updateButton.setPrefWidth(300);
        updateButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        updateButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        
        updateButton.setOnAction(e -> updateWalletStatus());
        
        formContainer.getChildren().addAll(phoneContainer, statusContainer, updateButton);
        return formContainer;
    }
    
    private VBox createResultContainer() {
        VBox resultContainer = new VBox(10);
        resultContainer.setAlignment(Pos.CENTER);
        resultContainer.setMaxWidth(600);
        
        resultLabel = new Label();
        resultLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        resultLabel.setWrapText(true);
        resultLabel.setAlignment(Pos.CENTER);
        resultLabel.setStyle("-fx-padding: 15; -fx-background-radius: 8;");
        
        resultContainer.getChildren().add(resultLabel);
        return resultContainer;
    }
    
    private void updateWalletStatus() {
        String phoneNumber = phoneNumberField.getText().trim();
        boolean isActive = isActiveCheckBox.isSelected();
        
        if (phoneNumber.isEmpty()) {
            showResult("Lütfen telefon numarasını girin.", false);
            return;
        }
        
        try {
            updateButton.setDisable(true);
            updateButton.setText("Güncelleniyor...");
            
            // API çağrısı
            String response = ApiClientFX.updateWalletStatus(accessToken, isActive);
            
            if (response != null && response.contains("\"success\":true")) {
                showResult("Cüzdan durumu başarıyla güncellendi.", true);
                phoneNumberField.clear();
            } else {
                String errorMessage = extractErrorMessage(response);
                showResult("Güncelleme başarısız: " + errorMessage, false);
            }
            
        } catch (Exception e) {
            showResult("Hata oluştu: " + e.getMessage(), false);
        } finally {
            updateButton.setDisable(false);
            updateButton.setText("Cüzdan Durumunu Güncelle");
        }
    }
    
    private void showResult(String message, boolean success) {
        resultLabel.setText(message);
        if (success) {
            resultLabel.setStyle(
                "-fx-background-color: #d4edda; " +
                "-fx-text-fill: #155724; " +
                "-fx-border-color: #c3e6cb; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;"
            );
        } else {
            resultLabel.setStyle(
                "-fx-background-color: #f8d7da; " +
                "-fx-text-fill: #721c24; " +
                "-fx-border-color: #f5c6cb; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;"
            );
        }
        resultLabel.setVisible(true);
    }
    
    private String extractErrorMessage(String response) {
        if (response == null) return "Bilinmeyen hata";
        
        try {
            // JSON response'dan message alanını çıkar
            int messageStart = response.indexOf("\"message\":\"");
            if (messageStart != -1) {
                messageStart += 11; // "message":"
                int messageEnd = response.indexOf("\"", messageStart);
                if (messageEnd != -1) {
                    return response.substring(messageStart, messageEnd);
                }
            }
        } catch (Exception e) {
            // JSON parsing hatası
        }
        
        return "API yanıt hatası";
    }
}
