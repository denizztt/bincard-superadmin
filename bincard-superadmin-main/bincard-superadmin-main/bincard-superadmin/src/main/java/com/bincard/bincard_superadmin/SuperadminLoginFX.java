package com.bincard.bincard_superadmin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SuperadminLoginFX {
    private TextField phoneField;
    private PasswordField passwordField;
    private TextField verificationCodeField;
    private Button loginButton;
    private Button verifyButton;
    private Button backButton;
    private TextArea resultArea;
    private Stage stage;
    private String currentPhone;
    private String currentPassword;
    private boolean isVerificationStep = false;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    private Timer tokenRefreshTimer;

    public SuperadminLoginFX(Stage stage) {
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
        card.setMaxWidth(500);
        card.setPrefWidth(500);

        // Başlık
        Label title = new Label("Superadmin Girişi");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#4e54c8"));
        title.setAlignment(Pos.CENTER);

        // Telefon alanı
        VBox phoneContainer = new VBox(8);
        Label phoneLabel = new Label("Telefon (Kullanıcı Adı):");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        phoneLabel.setTextFill(Color.web("#34495e"));
        
        phoneField = new TextField();
        phoneField.setPromptText("Telefon numaranızı giriniz");
        phoneField.setStyle("-fx-font-size: 16; -fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #3498db; -fx-border-width: 2;");
        phoneField.setPrefHeight(45);

        phoneContainer.getChildren().addAll(phoneLabel, phoneField);

        // Şifre alanı
        VBox passwordContainer = new VBox(8);
        Label passwordLabel = new Label("Şifre:");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        passwordLabel.setTextFill(Color.web("#34495e"));
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Şifrenizi giriniz");
        passwordField.setStyle("-fx-font-size: 16; -fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #3498db; -fx-border-width: 2;");
        passwordField.setPrefHeight(45);

        passwordContainer.getChildren().addAll(passwordLabel, passwordField);

        // Doğrulama kodu alanı (başlangıçta gizli)
        VBox verificationContainer = new VBox(8);
        verificationContainer.setAlignment(Pos.CENTER);
        verificationContainer.setMaxWidth(400);
        verificationContainer.setVisible(false);
        verificationContainer.setManaged(false);

        Label verificationTitle = new Label("Telefon Doğrulama");
        verificationTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        verificationTitle.setTextFill(Color.web("#2c3e50"));
        verificationTitle.setAlignment(Pos.CENTER);

        Label verificationDesc = new Label("Lütfen telefonunuza gelen 6 haneli doğrulama kodunu giriniz.");
        verificationDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        verificationDesc.setTextFill(Color.web("#34495e"));
        verificationDesc.setAlignment(Pos.CENTER);

        verificationCodeField = new TextField();
        verificationCodeField.setPromptText("6 haneli kod");
        verificationCodeField.setStyle("-fx-font-size: 18; -fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-alignment: center;");
        verificationCodeField.setPrefHeight(45);
        verificationCodeField.setMaxWidth(220);
        verificationCodeField.setAlignment(Pos.CENTER);

        verificationContainer.getChildren().addAll(verificationTitle, verificationDesc, verificationCodeField);

        // Giriş butonu
        loginButton = new Button("Giriş Yap");
        loginButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        loginButton.setPrefHeight(50);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));

        // Doğrula butonu (başlangıçta gizli)
        verifyButton = new Button("Doğrula");
        verifyButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        verifyButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        verifyButton.setPrefHeight(50);
        verifyButton.setMaxWidth(Double.MAX_VALUE);
        verifyButton.setVisible(false);
        verifyButton.setManaged(false);
        verifyButton.setOnMouseEntered(e -> verifyButton.setStyle("-fx-background-color: #229954; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        verifyButton.setOnMouseExited(e -> verifyButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));

        // Sonuç alanı - başlangıçta gizli olarak ayarla
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(3);
        resultArea.setStyle("-fx-font-size: 15; -fx-background-color: #f2e9e4; -fx-border-color: #c9ada7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #22223b;");
        resultArea.setPrefHeight(70);
        resultArea.setVisible(false); // Başlangıçta gizli
        resultArea.setManaged(false); // Yer kaplamasın

        // Geri dön butonu
        backButton = new Button("← Ana Menü");
        backButton.setFont(Font.font("Montserrat", FontWeight.BOLD, 16));
        backButton.setStyle("-fx-background-color: #9a8c98; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        backButton.setPrefHeight(40);
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #4a4e69; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #9a8c98; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));

        // Event handlers
        loginButton.setOnAction(e -> handleLogin());
        verifyButton.setOnAction(e -> handleVerification());
        backButton.setOnAction(e -> showMainMenu());

        // Enter tuşu ile giriş
        passwordField.setOnAction(e -> handleLogin());
        verificationCodeField.setOnAction(e -> handleVerification());

        // Kart içeriğini oluştur
        card.getChildren().addAll(title, phoneContainer, passwordContainer, verificationContainer, loginButton, verifyButton, resultArea, backButton);

        // Ana container'a kartı ekle
        mainContainer.getChildren().add(card);

        // Scene oluştur
        Scene scene = new Scene(mainContainer, 800, 700);
        stage.setScene(scene);
        stage.setTitle("Superadmin Girişi - Bincard Superadmin");
        stage.setResizable(false);
    }

    private void handleLogin() {
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();

        if (phone.isEmpty() || password.isEmpty()) {
            showResult("Tüm alanlar zorunludur!", false);
            return;
        }

        String phoneOnlyDigits = phone.replaceAll("\\D", "");
        if (!phoneOnlyDigits.matches("^\\d{10,11}$")) {
            showResult("Geçerli bir telefon numarası giriniz!", false);
            return;
        }

        // UI'ı devre dışı bırak
        setUIEnabled(false);
        loginButton.setText("Giriş yapılıyor...");

        // Arka planda login işlemini yap
        new Thread(() -> {
            try {
                // İlk aşama: Telefon ve şifre ile giriş
                // Bu aşamada SMS doğrulama kodu gönderilir
                LoginResponse response = ApiClientFX.login(phoneOnlyDigits, password);
                
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        // Doğrulama adımına geç
                        currentPhone = phoneOnlyDigits;
                        currentPassword = password;
                        isVerificationStep = true;
                        
                        // UI'ı doğrulama moduna geçir
                        showVerificationStep();
                        showResult(response.getMessage(), true);
                    } else {
                        showResult("Giriş başarısız: " + response.getMessage(), false);
                        setUIEnabled(true);
                        loginButton.setText("Giriş Yap");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showResult("Giriş başarısız: " + ex.getMessage(), false);
                    setUIEnabled(true);
                    loginButton.setText("Giriş Yap");
                });
            }
        }).start();
    }

    private void handleVerification() {
        String verificationCode = verificationCodeField.getText().trim();

        if (verificationCode.isEmpty()) {
            showResult("Doğrulama kodu zorunludur!", false);
            return;
        }

        if (!verificationCode.matches("^\\d{6}$")) {
            showResult("6 haneli doğrulama kodunu giriniz!", false);
            return;
        }

        // UI'ı devre dışı bırak
        setUIEnabled(false);
        verifyButton.setText("Doğrulanıyor...");

        // Arka planda doğrulama işlemini yap
        new Thread(() -> {
            try {
                TokenResponse tokenResponse = ApiClientFX.phoneVerify(currentPhone, verificationCode);
                
                // Token'ları sakla
                accessToken = tokenResponse.getAccessToken();
                refreshToken = tokenResponse.getRefreshToken();
                
                // Token yenileme zamanlayıcısını başlat
                scheduleTokenRefresh();
                
                Platform.runLater(() -> {
                    try {
                        // İşlem başarılı mesajını göster
                        showResult("Doğrulama başarılı! Dashboard açılıyor...", true);
                        
                        // UI'ı güncelle ve bir süre bekle
                        setUIEnabled(true);
                        verifyButton.setText("Doğrulama Başarılı");
                        
                        // Dashboard'a geçiş için kısa bir bekleme ekle
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000); // 1 saniye bekle
                                Platform.runLater(() -> {
                                    try {
                                        // Doğrulama başarılı, ana dashboard'a yönlendir
                                        new SuperadminDashboardFX(stage, accessToken, refreshToken);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        showResult("Dashboard açılırken hata: " + e.getMessage(), false);
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showResult("Dashboard açılırken hata: " + e.getMessage(), false);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showResult("Doğrulama başarısız: " + ex.getMessage(), false);
                    setUIEnabled(true);
                    verifyButton.setText("Doğrula");
                });
            }
        }).start();
    }
    
    private void scheduleTokenRefresh() {
        if (tokenRefreshTimer != null) {
            tokenRefreshTimer.cancel();
        }
        
        tokenRefreshTimer = new Timer();
        
        // Access token'ın sona ermesine 10 saniye kala yenile
        LocalDateTime expiresAt = accessToken.getExpiresAt();
        LocalDateTime refreshTime = expiresAt.minusSeconds(10);
        LocalDateTime now = LocalDateTime.now();
        
        long delayMillis = Duration.between(now, refreshTime).toMillis();
        if (delayMillis < 0) {
            // Token zaten süresi dolmuş veya dolmak üzere, hemen yenile
            refreshAccessToken();
            return;
        }
        
        tokenRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshAccessToken();
            }
        }, delayMillis);
    }
    
    private void refreshAccessToken() {
        try {
            System.out.println("Access token yenileniyor...");
            TokenDTO newAccessToken = ApiClientFX.refreshToken(refreshToken.getToken());
            accessToken = newAccessToken;
            
            // Yeni token için zamanlayıcıyı tekrar ayarla
            scheduleTokenRefresh();
            
            System.out.println("Access token başarıyla yenilendi. Yeni son geçerlilik: " + 
                    accessToken.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            System.err.println("Token yenileme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showVerificationStep() {
        // Telefon ve şifre alanlarını devre dışı bırak
        phoneField.setDisable(true);
        passwordField.setDisable(true);
        loginButton.setVisible(false);
        loginButton.setManaged(false);
        // Doğrulama alanını göster
        verificationCodeField.setVisible(true);
        verificationCodeField.setManaged(true);
        verifyButton.setVisible(true);
        verifyButton.setManaged(true);
        // Yeni: container'ı da göster
        VBox verificationContainer = (VBox) verificationCodeField.getParent();
        verificationContainer.setVisible(true);
        verificationContainer.setManaged(true);
        // Doğrulama alanına odaklan
        verificationCodeField.requestFocus();
    }

    private void showResult(String message, boolean isSuccess) {
        if (message == null || message.trim().isEmpty() || message.trim().equalsIgnoreCase("null")) {
            message = isSuccess ? "İşlem başarılı." : "Bir hata oluştu. Lütfen tekrar deneyin.";
        }
        
        // Mesaj varsa, sonuç alanını görünür yap
        resultArea.setVisible(true);
        resultArea.setManaged(true);
        
        resultArea.setText(message);
        if (isSuccess) {
            resultArea.setStyle("-fx-font-size: 15; -fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #155724;");
        } else {
            resultArea.setStyle("-fx-font-size: 15; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #721c24;");
        }
    }

    private void setUIEnabled(boolean enabled) {
        if (!isVerificationStep) {
            phoneField.setDisable(!enabled);
            passwordField.setDisable(!enabled);
            loginButton.setDisable(!enabled);
        } else {
            verificationCodeField.setDisable(!enabled);
            verifyButton.setDisable(!enabled);
        }
        backButton.setDisable(!enabled);
    }

    private void showMainMenu() {
        if (tokenRefreshTimer != null) {
            tokenRefreshTimer.cancel();
        }
        new MainMenuFX(stage);
    }
}
