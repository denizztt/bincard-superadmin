package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.application.HostServices;

public class AdminDashboardFX {
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    private HostServices hostServices;
    
    // Menü öğeleri
    private final String[] menuItems = {
        "Otobüsler", "Otobüs Kartları", "Şoförler", "Geri Bildirimler", 
        "Haberler", "Ödeme Noktaları", "Raporlar", "Otobüs Rotaları", 
        "Duraklar", "Kullanıcılar", "Cüzdan", "İstatistikler"
    };
    
    // Menü öğelerinin renkleri
    private final String[] menuColors = {
        "#3498db", "#2ecc71", "#e74c3c", "#9b59b6", 
        "#f39c12", "#1abc9c", "#34495e", "#d35400", 
        "#16a085", "#27ae60", "#8e44ad", "#f1c40f"
    };
    
    // Menü öğelerinin ikonları (şimdilik boş, gerçek uygulamada ikon eklenebilir)
    private final String[] menuIcons = {
        "bus", "card", "driver", "feedback", 
        "news", "payment", "report", "route", 
        "stop", "user", "wallet", "stats"
    };

    public AdminDashboardFX(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // HostServices için HelloApplication sınıfından erişim
        this.hostServices = HelloApplication.getAppHostServices();
        createUI();
    }

    private void createUI() {
        // Ana container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");
        
        // Üst panel (Header)
        HBox header = createHeader();
        root.setTop(header);
        
        // Menü paneli
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        FlowPane menuGrid = createMenuGrid();
        scrollPane.setContent(menuGrid);
        
        root.setCenter(scrollPane);
        
        // Alt panel (Footer)
        HBox footer = createFooter();
        root.setBottom(footer);
        
        // Scene oluştur
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Bincard Superadmin Paneli");
        stage.setResizable(true);
        stage.setMaximized(true);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setSpacing(20);
        header.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2c3e50); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Logo veya başlık
        Label title = new Label("BINCARD SUPERADMIN");
        title.setFont(Font.font("Montserrat", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#4a4e69"));
        
        // Sağ taraf - kullanıcı bilgisi ve çıkış
        HBox rightSide = new HBox();
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.setSpacing(15);
        rightSide.setPrefWidth(Integer.MAX_VALUE);
        
        Label userInfo = new Label("Superadmin");
        userInfo.setFont(Font.font("Montserrat", FontWeight.NORMAL, 16));
        userInfo.setTextFill(Color.web("#4a4e69"));
        
        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> logout());
        
        rightSide.getChildren().addAll(userInfo, logoutButton);
        
        header.getChildren().addAll(title, rightSide);
        return header;
    }
    
    private FlowPane createMenuGrid() {
        FlowPane menuGrid = new FlowPane();
        menuGrid.setHgap(20);
        menuGrid.setVgap(20);
        menuGrid.setPadding(new Insets(30));
        menuGrid.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < menuItems.length; i++) {
            VBox menuItem = createMenuItem(menuItems[i], menuColors[i], menuIcons[i]);
            menuGrid.getChildren().add(menuItem);
        }
        
        return menuGrid;
    }
    
    private VBox createMenuItem(String title, String color, String iconName) {
        VBox item = new VBox();
        item.setAlignment(Pos.CENTER);
        item.setSpacing(15);
        item.setPadding(new Insets(25));
        item.setPrefWidth(250);
        item.setPrefHeight(200);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 1);");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 1);"));
        
        // İkon alanı (gerçek ikon yoksa renkli bir kare göster)
        VBox iconBox = new VBox();
        iconBox.setPrefWidth(60);
        iconBox.setPrefHeight(60);
        iconBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        iconBox.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(iconName.substring(0, 1).toUpperCase());
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        iconLabel.setTextFill(Color.WHITE);
        iconBox.getChildren().add(iconLabel);
        
        // Başlık
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        item.getChildren().addAll(iconBox, titleLabel);
        
        // Tıklama olayı
        item.setOnMouseClicked(e -> navigateToSection(title));
        
        return item;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: #2c3e50;");
        
        Label footerText = new Label("© 2025 Bincard Superadmin Panel | Tüm Hakları Saklıdır");
        footerText.setFont(Font.font("Montserrat", FontWeight.NORMAL, 14));
        footerText.setTextFill(Color.web("#4a4e69"));
        
        footer.getChildren().add(footerText);
        return footer;
    }
    
    private void navigateToSection(String section) {
        System.out.println(section + " bölümüne yönlendiriliyor...");
        
        switch (section) {
            case "Otobüsler":
                new BusesPage(stage, accessToken, refreshToken);
                break;
            case "Şoförler":
                new DriversPage(stage, accessToken, refreshToken);
                break;
            case "Otobüs Kartları":
                showUnderConstructionAlert(section);
                break;
            case "Geri Bildirimler":
                showUnderConstructionAlert(section);
                break;
            case "Haberler":
                new NewsPage(stage, accessToken, refreshToken, hostServices);
                break;
            case "Ödeme Noktaları":
                showUnderConstructionAlert(section);
                break;
            case "Raporlar":
                showUnderConstructionAlert(section);
                break;
            case "Otobüs Rotaları":
                showUnderConstructionAlert(section);
                break;
            case "Duraklar":
                showUnderConstructionAlert(section);
                break;
            case "Kullanıcılar":
                showUnderConstructionAlert(section);
                break;
            case "Cüzdan":
                showUnderConstructionAlert(section);
                break;
            case "İstatistikler":
                showUnderConstructionAlert(section);
                break;
            default:
                // Varsayılan durum
                break;
        }
    }
    
    private void showUnderConstructionAlert(String section) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Yapım Aşamasında");
        alert.setHeaderText(section + " Sayfası");
        alert.setContentText("Bu bölüm henüz yapım aşamasındadır. Lütfen daha sonra tekrar deneyiniz.");
        alert.showAndWait();
    }
    
    private void logout() {
        // Çıkış işlemleri (token temizleme vb.)
        new MainMenuFX(stage);
    }
} 