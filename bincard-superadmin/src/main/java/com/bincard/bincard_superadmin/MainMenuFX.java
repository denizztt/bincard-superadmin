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
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        mainContainer.setPadding(new Insets(40));

        // Başlık
        javafx.scene.control.Label title = new javafx.scene.control.Label("Bincard Superadmin Paneli");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#22223b"));
        title.setAlignment(Pos.CENTER);

        // Buton container
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(400);

        // Superadmin Giriş butonu
        Button loginButton = new Button("Superadmin Giriş");
        loginButton.setFont(Font.font("Montserrat", FontWeight.BOLD, 20));
        loginButton.setStyle("-fx-background-color: #4a4e69; -fx-text-fill: #f2e9e4; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36;");
        loginButton.setPrefWidth(320);
        loginButton.setPrefHeight(65);
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #22223b; -fx-text-fill: #f2e9e4; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #4a4e69; -fx-text-fill: #f2e9e4; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 18 36;"));

        // Event handler
        loginButton.setOnAction(e -> {
            try {
                new AdminLoginFX(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Sadece giriş butonunu ekle
        buttonContainer.getChildren().addAll(loginButton);

        // Ana container'a elemanları ekle
        mainContainer.getChildren().addAll(title, buttonContainer);

        // Scene oluştur
        Scene scene = new Scene(mainContainer, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Bincard Superadmin Paneli");
        stage.setResizable(false);
    }
} 