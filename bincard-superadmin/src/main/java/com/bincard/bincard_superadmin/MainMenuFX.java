package com.bincard.bincard_superadmin;

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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenuFX {
    private Stage stage;
    private Label timeLabel;
    private Timer timer;

    public MainMenuFX(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        // Ana container
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #1F1C2C 0%, #928DAB 100%);");
        mainContainer.setPadding(new Insets(40));

        // Saat label'ı - üst sağ köşe
        timeLabel = new Label();
        timeLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 18));
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.setAlignment(Pos.TOP_RIGHT);
        mainContainer.getChildren().add(timeLabel);

        // Merhaba yazısı ve emoji
        Label welcomeLabel = new Label("Merhaba 👋");
        welcomeLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.web("#FFFFFF"));
        welcomeLabel.setAlignment(Pos.CENTER);

        // Başlık
        Label title = new Label("Bincard Superadmin Paneli");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#FFFFFF"));
        title.setAlignment(Pos.CENTER);

        // Buton container
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(400);

        // Superadmin Giriş butonu
        Button loginButton = new Button("Superadmin Giriş");
        loginButton.setFont(Font.font("Montserrat", FontWeight.BOLD, 20));
        loginButton.setStyle("-fx-background-color: #4e54c8; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        loginButton.setPrefWidth(320);
        loginButton.setPrefHeight(65);
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #3F3D8F; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 0);"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #4e54c8; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);"));

        // Event handler
        loginButton.setOnAction(e -> {
            try {
                new SuperadminLoginFX(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Butonları ekle (sadece giriş butonu)
        buttonContainer.getChildren().add(loginButton);

        // Ana container'a elemanları ekle
        mainContainer.getChildren().addAll(welcomeLabel, title, buttonContainer);

        // Scene oluştur
        Scene scene = new Scene(mainContainer, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Bincard Superadmin Paneli");
        stage.setResizable(false);
        
        // Stage kapandığında timer'ı durdur
        stage.setOnCloseRequest(e -> {
            if (timer != null) {
                timer.cancel();
            }
        });
        
        // Saati başlat
        startClock();
    }
    
    /**
     * Saat timer'ını başlatır
     */
    private void startClock() {
        // İlk güncelleme
        updateTimeLabel();
        
        // Her saniye güncelle
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateTimeLabel());
            }
        }, 1000, 1000);
    }
    
    /**
     * Saat label'ını günceller
     */
    private void updateTimeLabel() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", 
            java.util.Locale.forLanguageTag("tr-TR"));
        timeLabel.setText(now.format(formatter));
    }
}