package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenuFX {
    private Stage stage;

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

        // Başlık
        javafx.scene.control.Label title = new javafx.scene.control.Label("Bincard Superadmin Paneli");
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
        
        // Kayıt ol butonu
        Button signupButton = new Button("Kayıt Ol");
        signupButton.setFont(Font.font("Montserrat", FontWeight.BOLD, 20));
        signupButton.setStyle("-fx-background-color: #8e2de2; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        signupButton.setPrefWidth(320);
        signupButton.setPrefHeight(65);
        signupButton.setOnMouseEntered(e -> signupButton.setStyle("-fx-background-color: #7A1DC1; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 0);"));
        signupButton.setOnMouseExited(e -> signupButton.setStyle("-fx-background-color: #8e2de2; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);"));

        // Event handler
        signupButton.setOnAction(e -> {
            try {
                new SuperadminSignupFX(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Butonları ekle
        buttonContainer.getChildren().addAll(loginButton, signupButton);

        // Ana container'a elemanları ekle
        mainContainer.getChildren().addAll(title, buttonContainer);

        // Scene oluştur
        Scene scene = new Scene(mainContainer, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Bincard Superadmin Paneli");
        stage.setResizable(false);
    }
} 