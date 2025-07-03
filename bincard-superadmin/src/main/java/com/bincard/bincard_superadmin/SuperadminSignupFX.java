package com.bincard.bincard_superadmin;

import java.io.IOException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SuperadminSignupFX {
    private TextField nameField;
    private TextField surnameField;
    private TextField phoneField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button signupButton;
    private Button backButton;
    private TextArea resultArea;
    private Stage stage;

    public SuperadminSignupFX(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        // Ana container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #1F1C2C 0%, #928DAB 100%);");
        mainContainer.setPadding(new Insets(40));

        // Kart container
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        card.setPadding(new Insets(40));
        card.setMaxWidth(600);
        card.setPrefWidth(600);

        // Başlık
        Label title = new Label("Superadmin Kayıt");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#4e54c8"));
        title.setAlignment(Pos.CENTER);
        
        // Form grid
        GridPane formGrid = new GridPane();
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setHgap(15);
        formGrid.setVgap(20);
        formGrid.setPadding(new Insets(20, 0, 20, 0));
        
        // Ad alanı
        Label nameLabel = new Label("Ad:");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        nameField = new TextField();
        nameField.setPromptText("Adınızı giriniz");
        nameField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        nameField.setPrefHeight(40);
        
        // Soyad alanı
        Label surnameLabel = new Label("Soyad:");
        surnameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        surnameField = new TextField();
        surnameField.setPromptText("Soyadınızı giriniz");
        surnameField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        surnameField.setPrefHeight(40);
        
        // Telefon alanı
        Label phoneLabel = new Label("Telefon:");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        phoneField = new TextField();
        phoneField.setPromptText("Telefon numaranızı giriniz");
        phoneField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        phoneField.setPrefHeight(40);
        
        // E-posta alanı
        Label emailLabel = new Label("E-posta:");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        emailField = new TextField();
        emailField.setPromptText("E-posta adresinizi giriniz");
        emailField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        emailField.setPrefHeight(40);
        
        // Şifre alanı
        Label passwordLabel = new Label("Şifre:");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        passwordField = new PasswordField();
        passwordField.setPromptText("Şifrenizi giriniz");
        passwordField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        passwordField.setPrefHeight(40);
        
        // Şifre tekrar alanı
        Label confirmPasswordLabel = new Label("Şifre (Tekrar):");
        confirmPasswordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Şifrenizi tekrar giriniz");
        confirmPasswordField.setStyle("-fx-font-size: 16; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #4e54c8; -fx-border-width: 1;");
        confirmPasswordField.setPrefHeight(40);
        
        // Grid'e form elemanlarını ekle
        formGrid.add(nameLabel, 0, 0);
        formGrid.add(nameField, 1, 0);
        formGrid.add(surnameLabel, 0, 1);
        formGrid.add(surnameField, 1, 1);
        formGrid.add(phoneLabel, 0, 2);
        formGrid.add(phoneField, 1, 2);
        formGrid.add(emailLabel, 0, 3);
        formGrid.add(emailField, 1, 3);
        formGrid.add(passwordLabel, 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(confirmPasswordLabel, 0, 5);
        formGrid.add(confirmPasswordField, 1, 5);
        
        // Kayıt ol butonu
        signupButton = new Button("Kayıt Ol");
        signupButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        signupButton.setStyle("-fx-background-color: #8e2de2; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        signupButton.setPrefHeight(50);
        signupButton.setMaxWidth(Double.MAX_VALUE);
        signupButton.setOnMouseEntered(e -> signupButton.setStyle("-fx-background-color: #7A1DC1; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        signupButton.setOnMouseExited(e -> signupButton.setStyle("-fx-background-color: #8e2de2; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        // Sonuç alanı
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(3);
        resultArea.setStyle("-fx-font-size: 15; -fx-background-color: #f8f9fa; -fx-border-color: #ced4da; -fx-border-radius: 5; -fx-background-radius: 5;");
        resultArea.setPrefHeight(70);
        
        // Geri dön butonu
        backButton = new Button("← Ana Menü");
        backButton.setFont(Font.font("Montserrat", FontWeight.BOLD, 16));
        backButton.setStyle("-fx-background-color: #4e54c8; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        backButton.setPrefHeight(40);
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #3F3D8F; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #4e54c8; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        // Event handlers
        signupButton.setOnAction(e -> handleSignup());
        backButton.setOnAction(e -> showMainMenu());
        
        // Kart içeriğini oluştur
        card.getChildren().addAll(title, formGrid, signupButton, resultArea, backButton);
        
        // Ana container'a kartı ekle
        mainContainer.getChildren().add(card);
        
        // Scene oluştur
        Scene scene = new Scene(mainContainer, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Superadmin Kayıt - Bincard Superadmin");
        stage.setResizable(false);
    }
    
    private void handleSignup() {
        // Form alanlarından değerleri al
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Form doğrulama
        if (name.isEmpty() || surname.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showResult("Tüm alanlar zorunludur!", false);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showResult("Şifreler eşleşmiyor!", false);
            return;
        }
        
        String phoneOnlyDigits = phone.replaceAll("\\D", "");
        if (!phoneOnlyDigits.matches("^\\d{10,11}$")) {
            showResult("Geçerli bir telefon numarası giriniz!", false);
            return;
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showResult("Geçerli bir e-posta adresi giriniz!", false);
            return;
        }
        
        // UI'ı devre dışı bırak
        setUIEnabled(false);
        signupButton.setText("Kayıt yapılıyor...");
        
        // Arka planda kayıt işlemini yap
        new Thread(() -> {
            try {
                // Kayıt API'sini çağır
                LoginResponse response = ApiClientFX.signup(name, surname, phoneOnlyDigits, password, email);
                
                // UI thread'inde sonucu göster
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showResult("Kayıt başarılı! Lütfen giriş yapınız.", true);
                        // Birkaç saniye sonra ana menüye dön
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                Platform.runLater(() -> showMainMenu());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        showResult("Kayıt başarısız: " + response.getMessage(), false);
                        setUIEnabled(true);
                        signupButton.setText("Kayıt Ol");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showResult("Hata: " + e.getMessage(), false);
                    setUIEnabled(true);
                    signupButton.setText("Kayıt Ol");
                });
            }
        }).start();
    }
    
    private void showResult(String message, boolean success) {
        resultArea.setText(message);
        resultArea.setStyle("-fx-font-size: 15; -fx-background-color: " + 
                (success ? "#d4edda; -fx-text-fill: #155724;" : "#f8d7da; -fx-text-fill: #721c24;") +
                " -fx-border-color: " + (success ? "#c3e6cb;" : "#f5c6cb;") +
                " -fx-border-radius: 5; -fx-background-radius: 5;");
    }
    
    private void setUIEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        surnameField.setDisable(!enabled);
        phoneField.setDisable(!enabled);
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        confirmPasswordField.setDisable(!enabled);
        signupButton.setDisable(!enabled);
        backButton.setDisable(!enabled);
    }
    
    private void showMainMenu() {
        new MainMenuFX(stage);
    }
}
