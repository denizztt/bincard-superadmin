package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.application.HostServices;

public class SuperadminDashboardFX {
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
    
    // Menü öğelerinin ikonları (FontAwesome5 Solid ikonları)
    private final String[] menuIcons = {
        "fas-bus", // Bus
        "fas-credit-card", // Credit Card
        "fas-user-circle", // User Circle
        "fas-comment", // Comment
        "fas-newspaper", // Newspaper
        "fas-money-bill", // Money Bill
        "fas-chart-bar", // Chart Bar
        "fas-road", // Road
        "fas-map-marker-alt", // Map Marker
        "fas-users", // Users
        "fas-wallet", // Wallet
        "fas-chart-line"  // Chart Line
    };

    public SuperadminDashboardFX(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // HostServices için HelloApplication sınıfından erişim
        this.hostServices = HelloApplication.getAppHostServices();
        
        try {
            // İkon testi kaldırıldı - doğrudan UI'ı yükle
            createUI();
        } catch (Exception e) {
            System.err.println("Dashboard oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            
            // Hata durumunda basit bir UI göster
            showErrorUI("Dashboard yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    //     Hata durumunda basit bir UI gösterme metodu
    private void showErrorUI(String errorMessage) {
        VBox errorLayout = new VBox(20);
        errorLayout.setPadding(new Insets(40));
        errorLayout.setAlignment(Pos.CENTER);
        errorLayout.setStyle("-fx-background-color: #f8d7da;");
        
        Label errorTitle = new Label("Hata!");
        errorTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        errorTitle.setTextFill(Color.web("#721c24"));
        
        Label errorDesc = new Label(errorMessage);
        errorDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        errorDesc.setTextFill(Color.web("#721c24"));
        errorDesc.setWrapText(true);
        
        Button backButton = new Button("Ana Menüye Dön");
        backButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px;");
        backButton.setOnAction(e -> {
            // Ana menüye dön
            new MainMenuFX(stage);
        });
        
        errorLayout.getChildren().addAll(errorTitle, errorDesc, backButton);
        
        Scene errorScene = new Scene(errorLayout, 800, 600);
        stage.setScene(errorScene);
        stage.setTitle("Hata - Bincard Superadmin");
        stage.show();
    }

    private void createUI() {
        // Ana container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");
        
        // Üst panel (Header)
        HBox header = createHeader();
        root.setTop(header);
        
        // Sol sidebar menü
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // İçerik alanı (başlangıçta hoşgeldiniz mesajı)
        VBox contentArea = createWelcomeContent();
        ScrollPane contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(true);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScrollPane.setStyle("-fx-background-color: transparent;");
        
        root.setCenter(contentScrollPane);
        
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
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        
        // Arama kutusu
        TextField searchField = new TextField();
        searchField.setPromptText("Ara...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 20; -fx-padding: 8;");
        
        // Arama ikonu yerine renkli kutu
        HBox searchIcon = new HBox();
        searchIcon.setPrefSize(16, 16);
        searchIcon.setStyle("-fx-background-color: #636e72; -fx-background-radius: 2;");
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchIcon, searchField);
        searchBox.setPadding(new Insets(0, 0, 0, 10));
        
        // Sağ taraf - kullanıcı bilgisi ve çıkış
        HBox rightSide = new HBox();
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.setSpacing(15);
        rightSide.setPrefWidth(Integer.MAX_VALUE);
        
        // Bildirim ikonu yerine renkli kutu
        HBox notificationIcon = new HBox();
        notificationIcon.setPrefSize(18, 18);
        notificationIcon.setStyle("-fx-background-color: #636e72; -fx-background-radius: 9;");
        HBox notificationBox = new HBox(notificationIcon);
        notificationBox.setPadding(new Insets(0, 10, 0, 0));
        notificationBox.setStyle("-fx-cursor: hand;");
        
        // Kullanıcı bilgisi
        HBox userInfoBox = new HBox(10);
        userInfoBox.setAlignment(Pos.CENTER);
        
        // Kullanıcı avatarı yerine renkli kutu
        HBox avatarContainer = new HBox();
        avatarContainer.setPrefSize(18, 18);
        avatarContainer.setAlignment(Pos.CENTER);
        avatarContainer.setStyle("-fx-background-color: #4e54c8; -fx-background-radius: 15;");
        avatarContainer.setPadding(new Insets(5, 8, 5, 8));
        
        Label userInfo = new Label("Superadmin");
        userInfo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        userInfo.setTextFill(Color.web("#2d3436"));
        
        userInfoBox.getChildren().addAll(avatarContainer, userInfo);
        
        // Çıkış butonu - basit metin buton
        Button logoutButton = new Button("Çıkış");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 10; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> logout());
        
        rightSide.getChildren().addAll(notificationBox, userInfoBox, logoutButton);
        
        header.getChildren().addAll(searchBox, rightSide);
        return header;
    }
    
    // Eski menü grid metodu yerine sidebar kullanıyoruz
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #e6e6e6; -fx-border-width: 1 0 0 0;");
        
        Label footerText = new Label("© 2025 Bincard Superadmin Panel | Tüm Hakları Saklıdır");
        footerText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        footerText.setTextFill(Color.web("#636e72"));
        
        footer.getChildren().add(footerText);
        return footer;
    }
    
    /**
     * Sidebar menüsünü oluşturur
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #2d3436; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // Logo/başlık alanı
        Label logoLabel = new Label("BINCARD");
        logoLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 22));
        logoLabel.setTextFill(Color.web("#4e54c8"));
        logoLabel.setPadding(new Insets(10, 0, 20, 0));
        
        // Separator
        HBox separator = new HBox();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #4e54c8;");
        separator.setMaxWidth(200);
        separator.setPadding(new Insets(0, 0, 15, 0));
        
        sidebar.getChildren().addAll(logoLabel, separator);
        
        try {
            // Menü öğeleri
            for (int i = 0; i < menuItems.length; i++) {
                HBox menuItem = createSidebarMenuItem(menuItems[i], menuColors[i], menuIcons[i]);
                sidebar.getChildren().add(menuItem);
            }
        } catch (Exception e) {
            System.err.println("Sidebar menü öğeleri oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            
            // İkonlar olmadan menü öğeleri oluştur (yedek plan)
            createSimpleMenuItems(sidebar);
        }
        
        return sidebar;
    }
    
    // İkonlar olmadan basit menü öğeleri oluştur
    private void createSimpleMenuItems(VBox sidebar) {
        for (int i = 0; i < menuItems.length; i++) {
            String title = menuItems[i];
            String color = menuColors[i];
            
            HBox menuItem = new HBox(15);
            menuItem.setPadding(new Insets(10, 10, 10, 15));
            menuItem.setPrefWidth(250);
            menuItem.setAlignment(Pos.CENTER_LEFT);
            menuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            
            // Renkli kare (ikon yerine)
            HBox colorBox = new HBox();
            colorBox.setPrefSize(18, 18);
            colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
            
            // Başlık
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
            titleLabel.setTextFill(Color.WHITE);
            
            menuItem.getChildren().addAll(colorBox, titleLabel);
            
            // Hover efektleri
            menuItem.setOnMouseEntered(e -> 
                menuItem.setStyle("-fx-background-color: #4e54c8; -fx-cursor: hand;"));
            
            menuItem.setOnMouseExited(e -> 
                menuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));
            
            // Tıklama olayı
            final String menuTitle = title; // Lambda için final değişken
            menuItem.setOnMouseClicked(e -> navigateToSection(menuTitle));
            
            sidebar.getChildren().add(menuItem);
        }
    }
    
    /**
     * Sidebar menü öğesi oluşturur
     */
    private HBox createSidebarMenuItem(String title, String color, String iconCode) {
        HBox menuItem = new HBox(15);
        menuItem.setPadding(new Insets(10, 10, 10, 15));
        menuItem.setPrefWidth(250);
        menuItem.setAlignment(Pos.CENTER_LEFT);
        menuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        
        // Renkli kare (ikon yerine)
        HBox colorBox = new HBox();
        colorBox.setPrefSize(18, 18);
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        
        // Başlık
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        menuItem.getChildren().addAll(colorBox, titleLabel);
        
        // Hover efektleri
        menuItem.setOnMouseEntered(e -> 
            menuItem.setStyle("-fx-background-color: #4e54c8; -fx-cursor: hand;"));
        
        menuItem.setOnMouseExited(e -> 
            menuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));
        
        // Tıklama olayı
        menuItem.setOnMouseClicked(e -> navigateToSection(title));
        
        return menuItem;
    }
    
    /**
     * Hoşgeldiniz içeriğini oluşturur
     */
    private VBox createWelcomeContent() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));
        content.setStyle("-fx-background-color: #f0f2f5;");
        
        // Hoşgeldiniz başlığı
        Label welcomeTitle = new Label("Bincard Superadmin Paneline Hoşgeldiniz");
        welcomeTitle.setFont(Font.font("Montserrat", FontWeight.BOLD, 32));
        welcomeTitle.setTextFill(Color.web("#2d3436"));
        
        // Açıklama metni
        Label description = new Label("Yönetim işlemleri için sol taraftaki menüyü kullanabilirsiniz.");
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        description.setTextFill(Color.web("#636e72"));
        
        // Zaman bilgisi
        Label timeLabel = new Label("Giriş Zamanı: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")));
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        timeLabel.setTextFill(Color.web("#636e72"));
        
        // İstatistik kartları
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(30, 0, 0, 0));
        
        // Demo istatistik kartları
        VBox totalUsers = createStatCard("Toplam Kullanıcılar", "12,543", "#4e54c8", "\uf0c0");
        VBox activeUsers = createStatCard("Aktif Kullanıcılar", "8,729", "#2ecc71", "\uf0c1");
        VBox totalBuses = createStatCard("Toplam Otobüsler", "342", "#3498db", "\uf207");
        VBox totalDrivers = createStatCard("Toplam Şoförler", "562", "#e74c3c", "\uf2bd");
        
        statsContainer.getChildren().addAll(totalUsers, activeUsers, totalBuses, totalDrivers);
        
        content.getChildren().addAll(welcomeTitle, description, timeLabel, statsContainer);
        
        return content;
    }
    
    /**
     * İstatistik kartı oluşturur
     */
    private VBox createStatCard(String title, String value, String color, String iconCode) {
        VBox statCard = new VBox(10);
        statCard.setAlignment(Pos.CENTER);
        statCard.setPadding(new Insets(20));
        statCard.setPrefWidth(200);
        statCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        
        // İkon yerine renkli kutu
        HBox colorBox = new HBox();
        colorBox.setPrefSize(24, 24);
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
        
        // Değer
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 26));
        valueLabel.setTextFill(Color.web("#2d3436"));
        
        // Başlık
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.web("#636e72"));
        
        statCard.getChildren().addAll(colorBox, valueLabel, titleLabel);
        
        return statCard;
    }
    
    private void navigateToSection(String section) {
        System.out.println(section + " bölümüne yönlendiriliyor...");
        
        try {
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
        } catch (Exception e) {
            System.err.println("Sayfa yönlendirme hatası: " + e.getMessage());
            e.printStackTrace();
            
            // Hata alertini göster
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Sayfa Açılamadı");
            alert.setHeaderText(section + " Sayfası Açılamadı");
            alert.setContentText(section + " sayfasına yönlendirme sırasında bir hata oluştu: " + e.getMessage());
            alert.showAndWait();
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
