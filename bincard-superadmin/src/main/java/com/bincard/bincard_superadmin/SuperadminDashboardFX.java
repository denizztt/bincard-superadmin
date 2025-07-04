package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.application.HostServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuperadminDashboardFX {
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    private HostServices hostServices;
    private List<MenuItem> menuItems = new ArrayList<>();
    
    // Soft renk paleti için renkler - SubMenu ve başlıklar için kullanılıyor
    private final String mainColor = "#3a4750"; // Daha koyu, şık bir renk - Sidebar arkaplanı için kullanılıyor
    private final String accentColor1 = "#379683"; // Yumuşak yeşil
    private final String accentColor2 = "#7395ae"; // Yumuşak mavi
    private final String accentColor3 = "#557a95"; // Koyu mavi
    private final String accentColor4 = "#b1a296"; // Yumuşak bej
    
    // Alt menülerin görünürlük durumları
    private Map<String, VBox> subMenuContainers = new HashMap<>();
    
    /**
     * Menü yapısını oluşturur - alfabetik sıralama ile
     */
    private void initializeMenuItems() {
        // Tüm menü öğelerini oluştur
        
        // Admin Onayları (alt menü olmadan)
        MenuItem approvals = new MenuItem("Admin Onayları", accentColor1, "AdminApprovals");
        
        // Duraklar menüsü
        MenuItem stopsMenu = new MenuItem("Duraklar", accentColor3);
        stopsMenu.addSubItem(new MenuItem("Durak Ekle", accentColor3, "StopAdd"));
        stopsMenu.addSubItem(new MenuItem("Durakları Görüntüle", accentColor3, "StopsList"));
        stopsMenu.addSubItem(new MenuItem("Durak Düzenle", accentColor3, "StopEdit"));
        stopsMenu.addSubItem(new MenuItem("Durak Sil", accentColor3, "StopDelete"));
        
        // Haberler menüsü
        MenuItem newsMenu = new MenuItem("Haberler", accentColor3);
        newsMenu.addSubItem(new MenuItem("Haber Ekle", accentColor3, "NewsAdd"));
        newsMenu.addSubItem(new MenuItem("Haberleri Görüntüle", accentColor3, "NewsList"));
        newsMenu.addSubItem(new MenuItem("Haber Düzenle", accentColor3, "NewsEdit"));
        newsMenu.addSubItem(new MenuItem("Haber Sil", accentColor3, "NewsDelete"));
        
        // İstatistikler
        MenuItem stats = new MenuItem("İstatistikler", accentColor3, "Statistics");
        
        // Kullanıcılar menüsü
        MenuItem usersMenu = new MenuItem("Kullanıcılar", accentColor4);
        usersMenu.addSubItem(new MenuItem("Kullanıcı Ekle", accentColor4, "UserAdd"));
        usersMenu.addSubItem(new MenuItem("Kullanıcıları Görüntüle", accentColor4, "UsersList"));
        usersMenu.addSubItem(new MenuItem("Kullanıcı Düzenle", accentColor4, "UserEdit"));
        usersMenu.addSubItem(new MenuItem("Kullanıcı Sil", accentColor4, "UserDelete"));
        
        // Otobüs Rotaları menüsü
        MenuItem routesMenu = new MenuItem("Otobüs Rotaları", accentColor2);
        routesMenu.addSubItem(new MenuItem("Rota Ekle", accentColor2, "RouteAdd"));
        routesMenu.addSubItem(new MenuItem("Rotaları Görüntüle", accentColor2, "RoutesList"));
        routesMenu.addSubItem(new MenuItem("Rota Düzenle", accentColor2, "RouteEdit"));
        routesMenu.addSubItem(new MenuItem("Rota Sil", accentColor2, "RouteDelete"));
        
        // Otobüsler menüsü
        MenuItem busesMenu = new MenuItem("Otobüsler", accentColor1);
        busesMenu.addSubItem(new MenuItem("Otobüs Ekle", accentColor1, "BusAdd"));
        busesMenu.addSubItem(new MenuItem("Otobüsleri Görüntüle", accentColor1, "BusesList"));
        busesMenu.addSubItem(new MenuItem("Otobüs Düzenle", accentColor1, "BusEdit"));
        busesMenu.addSubItem(new MenuItem("Otobüs Sil", accentColor1, "BusDelete"));
        
        // Raporlar menüsü
        MenuItem reportsMenu = new MenuItem("Raporlar", accentColor2);
        reportsMenu.addSubItem(new MenuItem("Günlük Raporlar", accentColor2, "DailyReports"));
        reportsMenu.addSubItem(new MenuItem("Aylık Raporlar", accentColor2, "MonthlyReports"));
        reportsMenu.addSubItem(new MenuItem("Yıllık Raporlar", accentColor2, "YearlyReports"));
        
        // Şoförler menüsü
        MenuItem driversMenu = new MenuItem("Şoförler", accentColor2);
        driversMenu.addSubItem(new MenuItem("Şoför Ekle", accentColor2, "DriverAdd"));
        driversMenu.addSubItem(new MenuItem("Şoförleri Görüntüle", accentColor2, "DriversList"));
        driversMenu.addSubItem(new MenuItem("Şoför Düzenle", accentColor2, "DriverEdit"));
        driversMenu.addSubItem(new MenuItem("Şoför Sil", accentColor2, "DriverDelete"));
        
        // Alfabetik sırada menü listesine ekle
        menuItems.add(approvals);  // Admin Onayları
        menuItems.add(stopsMenu);  // Duraklar
        menuItems.add(newsMenu);   // Haberler
        menuItems.add(stats);      // İstatistikler
        menuItems.add(usersMenu);  // Kullanıcılar
        menuItems.add(routesMenu); // Otobüs Rotaları
        menuItems.add(busesMenu);  // Otobüsler
        menuItems.add(reportsMenu); // Raporlar
        menuItems.add(driversMenu); // Şoförler
    }

    public SuperadminDashboardFX(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // HostServices için HelloApplication sınıfından erişim
        this.hostServices = HelloApplication.getAppHostServices();
        
        // Menü yapısını başlat
        initializeMenuItems();
        
        try {
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
        
        // Sidebar'ı ScrollPane içine al (uzun menüler için kaydırma desteği)
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setStyle("-fx-background-color: transparent; -fx-background: " + mainColor + "; -fx-padding: 0;");
        sidebarScroll.setPrefWidth(250);
        
        root.setLeft(sidebarScroll);
        
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
        
        // Tam ekran modunda başlat - varsayılan olarak
        stage.setFullScreenExitHint("Tam ekrandan çıkmak için ESC tuşuna basın");
        stage.setFullScreen(true);
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
        
        // Bildirim ikonu - tıklanabilir ve mavi renkli
        HBox notificationIcon = new HBox();
        notificationIcon.setPrefSize(18, 18);
        notificationIcon.setStyle("-fx-background-color: #4e54c8; -fx-background-radius: 9;");
        HBox notificationBox = new HBox(notificationIcon);
        notificationBox.setPadding(new Insets(0, 10, 0, 0));
        notificationBox.setStyle("-fx-cursor: hand;");
        
        // Bildirim ikonuna tıklama işlevselliği ekle
        notificationBox.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bildirimler");
            alert.setHeaderText("Bildirimler");
            alert.setContentText("Şu anda yeni bir bildiriminiz bulunmamaktadır.");
            alert.showAndWait();
        });
        
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
        sidebar.setStyle("-fx-background-color: " + mainColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 0);");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // Logo/başlık alanı
        Label logoLabel = new Label("BINCARD");
        logoLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 22));
        logoLabel.setTextFill(Color.WHITE);
        logoLabel.setPadding(new Insets(10, 0, 15, 0));
        
        // Arama çubuğu container
        VBox searchContainer = new VBox(5);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setMaxWidth(200);
        searchContainer.setStyle("-fx-background-color: transparent;"); // Beyaz dikdörtgeni kaldırmak için arkaplanı transparan yap
        
        // Arama çubuğu ekle
        TextField searchMenuField = new TextField();
        searchMenuField.setPromptText("Menüde ara...");
        searchMenuField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 8; -fx-font-size: 14px;");
        searchMenuField.setPrefWidth(200);
        searchMenuField.setMaxWidth(200);
        
        // Arama sonucu bilgisi için label (başlangıçta görünmez)
        Label searchResultLabel = new Label("Sonuç bulunamadı");
        searchResultLabel.setTextFill(Color.LIGHTPINK);
        searchResultLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        searchResultLabel.setVisible(false);
        searchResultLabel.setManaged(false);
        
        // Arama container'ına ekle
        searchContainer.getChildren().addAll(searchMenuField, searchResultLabel);
        
        // Arama çubuğu işlevselliği
        searchMenuField.setOnKeyReleased(e -> searchInMenu(searchMenuField.getText(), searchResultLabel));
        
        // Separator çizgisi kaldırıldı
        
        sidebar.getChildren().addAll(logoLabel, searchContainer);
        
        // Ana menü öğelerini ekle
        for (MenuItem menuItem : menuItems) {
            VBox menuContainer = new VBox(0); // Ana menü ve alt menüler için container
            
            // Ana menü öğesi
            HBox mainMenuItem = createMenuItem(menuItem);
            menuContainer.getChildren().add(mainMenuItem);
            
            if (menuItem.hasSubItems()) {
                // Alt menü container'ı
                VBox subMenuBox = new VBox(0);
                subMenuBox.setPadding(new Insets(0, 0, 0, 20)); // Sol taraftan padding
                subMenuBox.setVisible(false); // Başlangıçta gizli
                subMenuBox.setManaged(false); // Yer kaplamasın
                
                // Alt menü öğelerini ekle
                for (MenuItem subItem : menuItem.getSubItems()) {
                    HBox subMenuItem = createSubMenuItem(subItem);
                    subMenuBox.getChildren().add(subMenuItem);
                }
                
                menuContainer.getChildren().add(subMenuBox);
                
                // Ok işareti elementi referansı
                Label arrowLabel = null;
                for (Node child : mainMenuItem.getChildren()) {
                    if (child instanceof Label && ((Label) child).getText().equals("▼")) {
                        arrowLabel = (Label) child;
                        break;
                    }
                }
                
                // Final değişken olarak tanımla (lambda içinde kullanabilmek için)
                final Label finalArrowLabel = arrowLabel;
                
                // Ana menü tıklama olayı (alt menüyü göster/gizle)
                mainMenuItem.setOnMouseClicked(e -> {
                    boolean isVisible = subMenuBox.isVisible();
                    subMenuBox.setVisible(!isVisible);
                    subMenuBox.setManaged(!isVisible);
                    
                    // Ana menü arka plan rengini değiştir (açık/kapalı durumu göstermek için)
                    if (!isVisible) {
                        mainMenuItem.setStyle("-fx-background-color: " + menuItem.getColor() + "; -fx-cursor: hand;");
                        // Ok işaretini çevir (açık)
                        if (finalArrowLabel != null) {
                            finalArrowLabel.setText("▲");
                        }
                    } else {
                        mainMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                        // Ok işaretini çevir (kapalı)
                        if (finalArrowLabel != null) {
                            finalArrowLabel.setText("▼");
                        }
                    }
                });
                
                // Alt menü container'ını HashMap'e ekle (daha sonra erişim için)
                subMenuContainers.put(menuItem.getTitle(), subMenuBox);
            } else {
                // Alt menüsü yoksa doğrudan ana menüye tıklama olayı ekle
                mainMenuItem.setOnMouseClicked(e -> navigateToSection(menuItem.getTargetPage()));
            }
            
            sidebar.getChildren().add(menuContainer);
        }
        
        return sidebar;
    }
    
    /**
     * Ana menü öğesi oluşturur
     */
    private HBox createMenuItem(MenuItem menuItem) {
        HBox menuBox = new HBox(15);
        menuBox.setPadding(new Insets(10, 10, 10, 15));
        menuBox.setPrefWidth(250);
        menuBox.setAlignment(Pos.CENTER_LEFT);
        menuBox.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        
        // Renkli kare (ikon yerine)
        HBox colorBox = new HBox();
        colorBox.setPrefSize(16, 16);
        colorBox.setStyle("-fx-background-color: " + menuItem.getColor() + "; -fx-background-radius: 3;");
        
        // Başlık
        Label titleLabel = new Label(menuItem.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 15));
        titleLabel.setTextFill(Color.WHITE);
        
        menuBox.getChildren().addAll(colorBox, titleLabel);
        
        // Eğer alt menüsü varsa ok işareti ekle
        if (menuItem.hasSubItems()) {
            Label arrowLabel = new Label("▼");
            arrowLabel.setTextFill(Color.LIGHTGRAY);
            arrowLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            menuBox.getChildren().addAll(spacer, arrowLabel);
        }
        
        // Hover efektleri
        menuBox.setOnMouseEntered(e -> {
            // Alt menüsü açıksa, renk değişimi yapmadan hover efekti uygula
            if (menuItem.hasSubItems() && subMenuContainers.containsKey(menuItem.getTitle()) && 
                subMenuContainers.get(menuItem.getTitle()).isVisible()) {
                menuBox.setStyle("-fx-background-color: " + menuItem.getColor() + "; -fx-cursor: hand;");
            } else {
                menuBox.setStyle("-fx-background-color: " + menuItem.getColor() + "99; -fx-cursor: hand;"); // %60 opaklık
            }
        });
        
        menuBox.setOnMouseExited(e -> {
            // Sadece alt menüsü açık değilse hover'dan çıkınca rengini değiştir
            if (!menuItem.hasSubItems() || !subMenuContainers.containsKey(menuItem.getTitle()) || 
                !subMenuContainers.get(menuItem.getTitle()).isVisible()) {
                menuBox.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            } else {
                // Alt menü açıksa, hover'dan çıkınca da renkli kal (daha hafif)
                menuBox.setStyle("-fx-background-color: " + menuItem.getColor() + "80; -fx-cursor: hand;"); // %50 opaklık
            }
        });
        
        return menuBox;
    }
    
    /**
     * Alt menü öğesi oluşturur
     */
    private HBox createSubMenuItem(MenuItem subItem) {
        HBox subMenuBox = new HBox(15);
        subMenuBox.setPadding(new Insets(8, 10, 8, 15));
        subMenuBox.setPrefWidth(230);
        subMenuBox.setAlignment(Pos.CENTER_LEFT);
        subMenuBox.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        
        // Alt menü için daha küçük renkli kare
        HBox colorBox = new HBox();
        colorBox.setPrefSize(12, 12);
        colorBox.setStyle("-fx-background-color: " + subItem.getColor() + "; -fx-background-radius: 2;");
        
        // Başlık - daha küçük font
        Label titleLabel = new Label(subItem.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);
        
        subMenuBox.getChildren().addAll(colorBox, titleLabel);
        
        // Hover efektleri
        subMenuBox.setOnMouseEntered(e -> {
            subMenuBox.setStyle("-fx-background-color: #557a95; -fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
        });
        
        subMenuBox.setOnMouseExited(e -> {
            subMenuBox.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            titleLabel.setTextFill(Color.LIGHTGRAY);
        });
        
        // Tıklama olayı - hedef sayfaya yönlendir
        subMenuBox.setOnMouseClicked(e -> navigateToSection(subItem.getTargetPage()));
        
        return subMenuBox;
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
                // Ana sayfalar
                case "Otobüsler":
                    new BusesPage(stage, accessToken, refreshToken);
                    break;
                case "Şoförler":
                    new DriversPage(stage, accessToken, refreshToken);
                    break;
                case "Haberler":
                    new NewsPage(stage, accessToken, refreshToken, hostServices);
                    break;
                case "Admin Onayları":
                case "AdminApprovals":
                    new AdminApprovalsPage(stage, accessToken, refreshToken, hostServices);
                    break;
                case "Statistics":
                    showUnderConstructionAlert("İstatistikler");
                    break;
                    
                // Otobüs alt sayfaları
                case "BusAdd":
                    showUnderConstructionAlert("Otobüs Ekle");
                    break;
                case "BusesList":
                    new BusesPage(stage, accessToken, refreshToken);
                    break;
                case "BusEdit":
                    showUnderConstructionAlert("Otobüs Düzenle");
                    break;
                case "BusDelete":
                    showUnderConstructionAlert("Otobüs Sil");
                    break;
                    
                // Şoför alt sayfaları
                case "DriverAdd":
                    showUnderConstructionAlert("Şoför Ekle");
                    break;
                case "DriversList":
                    new DriversPage(stage, accessToken, refreshToken);
                    break;
                case "DriverEdit":
                    showUnderConstructionAlert("Şoför Düzenle");
                    break;
                case "DriverDelete":
                    showUnderConstructionAlert("Şoför Sil");
                    break;
                    
                // Haber alt sayfaları
                case "NewsAdd":
                    showUnderConstructionAlert("Haber Ekle");
                    break;
                case "NewsList":
                    new NewsPage(stage, accessToken, refreshToken, hostServices);
                    break;
                case "NewsEdit":
                    showUnderConstructionAlert("Haber Düzenle");
                    break;
                case "NewsDelete":
                    showUnderConstructionAlert("Haber Sil");
                    break;
                    
                // Rota alt sayfaları
                case "RouteAdd":
                    showUnderConstructionAlert("Rota Ekle");
                    break;
                case "RoutesList":
                    showUnderConstructionAlert("Rotaları Görüntüle");
                    break;
                case "RouteEdit":
                    showUnderConstructionAlert("Rota Düzenle");
                    break;
                case "RouteDelete":
                    showUnderConstructionAlert("Rota Sil");
                    break;
                    
                // Durak alt sayfaları
                case "StopAdd":
                    showUnderConstructionAlert("Durak Ekle");
                    break;
                case "StopsList":
                    showUnderConstructionAlert("Durakları Görüntüle");
                    break;
                case "StopEdit":
                    showUnderConstructionAlert("Durak Düzenle");
                    break;
                case "StopDelete":
                    showUnderConstructionAlert("Durak Sil");
                    break;
                    
                // Kullanıcı alt sayfaları
                case "UserAdd":
                    showUnderConstructionAlert("Kullanıcı Ekle");
                    break;
                case "UsersList":
                    showUnderConstructionAlert("Kullanıcıları Görüntüle");
                    break;
                case "UserEdit":
                    showUnderConstructionAlert("Kullanıcı Düzenle");
                    break;
                case "UserDelete":
                    showUnderConstructionAlert("Kullanıcı Sil");
                    break;
                    
                // Rapor alt sayfaları
                case "DailyReports":
                    showUnderConstructionAlert("Günlük Raporlar");
                    break;
                case "MonthlyReports":
                    showUnderConstructionAlert("Aylık Raporlar");
                    break;
                case "YearlyReports":
                    showUnderConstructionAlert("Yıllık Raporlar");
                    break;
                    
                // Eski sayfalar ve diğerleri
                case "Otobüs Kartları":
                case "Geri Bildirimler":
                case "Ödeme Noktaları":
                case "Raporlar":
                case "Otobüs Rotaları":
                case "Duraklar":
                case "Kullanıcılar":
                case "Cüzdan":
                case "İstatistikler":
                    showUnderConstructionAlert(section);
                    break;
                default:
                    // Varsayılan durum
                    showUnderConstructionAlert("Bilinmeyen Sayfa: " + section);
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
    
    /**
     * Menüde arama yaparak eşleşen öğelere gitmeyi sağlar
     */
    private void searchInMenu(String searchText, Label resultLabel) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Sonuç etiketini gizle
            resultLabel.setVisible(false);
            resultLabel.setManaged(false);
            
            // Arama metni boşsa tüm menüleri normal göster
            resetAllMenus();
            return;
        }
        
        searchText = searchText.toLowerCase().trim();
        boolean foundMatch = false;
        
        // Önce doğrudan ana menü öğelerinde ara
        for (MenuItem item : menuItems) {
            String title = item.getTitle().toLowerCase();
            boolean matchesMainMenu = title.contains(searchText);
            boolean hasMatchingSubMenu = false;
            
            // Alt menülerde ara
            if (item.hasSubItems()) {
                for (MenuItem subItem : item.getSubItems()) {
                    if (subItem.getTitle().toLowerCase().contains(searchText)) {
                        hasMatchingSubMenu = true;
                    }
                }
            }
            
            // Ana menü öğelerinin bulunduğu container'ları bul
            VBox menuContainer = findMenuContainer(item.getTitle());
            
            if (menuContainer != null) {
                // Eşleşme varsa göster, yoksa gizle
                menuContainer.setVisible(matchesMainMenu || hasMatchingSubMenu);
                menuContainer.setManaged(matchesMainMenu || hasMatchingSubMenu);
                
                // Eşleşen alt menüleri göster
                if (hasMatchingSubMenu && subMenuContainers.containsKey(item.getTitle())) {
                    VBox subMenuBox = subMenuContainers.get(item.getTitle());
                    subMenuBox.setVisible(true);
                    subMenuBox.setManaged(true);
                    
                    // Alt menü içindeki öğeleri filtrele
                    for (Node node : subMenuBox.getChildren()) {
                        if (node instanceof HBox) {
                            HBox subMenuItem = (HBox) node;
                            String subMenuTitle = "";
                            
                            // Alt menü başlığını bul
                            for (Node child : subMenuItem.getChildren()) {
                                if (child instanceof Label) {
                                    Label label = (Label) child;
                                    if (!label.getText().equals("▼") && !label.getText().equals("▲")) {
                                        subMenuTitle = label.getText();
                                        break;
                                    }
                                }
                            }
                            
                            // Alt menü başlığı eşleşiyorsa göster ve tıklama işlevi ekle
                            boolean subMenuMatches = subMenuTitle.toLowerCase().contains(searchText);
                            subMenuItem.setVisible(subMenuMatches);
                            subMenuItem.setManaged(subMenuMatches);
                            
                            // Eğer alt menü eşleşiyorsa vurgula - daha güzel bir renk kullan
                            if (subMenuMatches) {
                                String menuColor = item.getColor();
                                // Daha parlak bir vurgu rengi için menü renginin opaklığını arttır
                                subMenuItem.setStyle("-fx-background-color: " + menuColor + "AA; -fx-cursor: hand;"); // %67 opaklık
                                
                                // Mouse üzerine gelince ve çıkınca rengi koru
                                subMenuItem.setOnMouseEntered(e -> {
                                    subMenuItem.setStyle("-fx-background-color: " + menuColor + "; -fx-cursor: hand;");
                                });
                                
                                subMenuItem.setOnMouseExited(e -> {
                                    subMenuItem.setStyle("-fx-background-color: " + menuColor + "AA; -fx-cursor: hand;");
                                });
                                
                                // Yazı rengini beyaz yap
                                for (Node child : subMenuItem.getChildren()) {
                                    if (child instanceof Label && !((Label) child).getText().equals("▼") && !((Label) child).getText().equals("▲")) {
                                        ((Label) child).setTextFill(Color.WHITE);
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Ana menü öğesine eşleşme vurgusu ekle
                if (matchesMainMenu) {
                    HBox mainMenuItem = (HBox) menuContainer.getChildren().get(0);
                    String menuColor = item.getColor();
                    // Ana menü için daha güçlü bir vurgu rengi
                    mainMenuItem.setStyle("-fx-background-color: " + menuColor + "; -fx-cursor: hand;");
                    
                    // Mouse çıkınca vurguyu koru (arama aktifken)
                    mainMenuItem.setOnMouseExited(e -> {
                        mainMenuItem.setStyle("-fx-background-color: " + menuColor + "; -fx-cursor: hand;");
                    });
                }
                
                if (matchesMainMenu || hasMatchingSubMenu) {
                    foundMatch = true;
                }
            }
        }
        
        // Eşleşme yoksa kullanıcıya bildir
        if (!foundMatch) {
            resultLabel.setText("Sonuç bulunamadı: " + searchText);
            resultLabel.setVisible(true);
            resultLabel.setManaged(true);
        } else {
            resultLabel.setVisible(false);
            resultLabel.setManaged(false);
        }
    }
    
    /**
     * Tüm menüleri orijinal durumuna sıfırlar
     */
    private void resetAllMenus() {
        for (MenuItem item : menuItems) {
            // Ana menü öğelerinin bulunduğu container'ları bul
            VBox menuContainer = findMenuContainer(item.getTitle());
            if (menuContainer != null) {
                // Tüm menü containerları görünür yap
                menuContainer.setVisible(true);
                menuContainer.setManaged(true);
                
                // Ana menü öğesinin stilini sıfırla
                if (menuContainer.getChildren().size() > 0 && menuContainer.getChildren().get(0) instanceof HBox) {
                    HBox mainMenuItem = (HBox) menuContainer.getChildren().get(0);
                    mainMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    
                    // Ana menü hover efektlerini yeniden tanımla
                    final MenuItem finalItem = item; // Lambda için final değişken
                    mainMenuItem.setOnMouseEntered(e -> {
                        if (finalItem.hasSubItems() && subMenuContainers.containsKey(finalItem.getTitle()) && 
                            subMenuContainers.get(finalItem.getTitle()).isVisible()) {
                            mainMenuItem.setStyle("-fx-background-color: " + finalItem.getColor() + "; -fx-cursor: hand;");
                        } else {
                            mainMenuItem.setStyle("-fx-background-color: " + finalItem.getColor() + "99; -fx-cursor: hand;");
                        }
                    });
                    
                    mainMenuItem.setOnMouseExited(e -> {
                        if (!finalItem.hasSubItems() || !subMenuContainers.containsKey(finalItem.getTitle()) || 
                            !subMenuContainers.get(finalItem.getTitle()).isVisible()) {
                            mainMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                        } else {
                            mainMenuItem.setStyle("-fx-background-color: " + finalItem.getColor() + "80; -fx-cursor: hand;");
                        }
                    });
                }
                
                // Alt menü görünürlüğünü sıfırla
                if (subMenuContainers.containsKey(item.getTitle())) {
                    VBox subMenuBox = subMenuContainers.get(item.getTitle());
                    // Alt menüyü kapat
                    subMenuBox.setVisible(false);
                    subMenuBox.setManaged(false);
                    
                    // Alt menü içindeki tüm öğeleri görünür yap ve stillerini sıfırla
                    for (Node node : subMenuBox.getChildren()) {
                        if (node instanceof HBox) {
                            HBox subMenuItem = (HBox) node;
                            subMenuItem.setVisible(true);
                            subMenuItem.setManaged(true);
                            subMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                            
                            // Alt başlık yazı rengini gri yap
                            for (Node child : subMenuItem.getChildren()) {
                                if (child instanceof Label && !((Label) child).getText().equals("▼") && !((Label) child).getText().equals("▲")) {
                                    ((Label) child).setTextFill(Color.LIGHTGRAY);
                                }
                            }
                            
                            // Alt menü hover efektlerini yeniden tanımla
                            final String menuColor = item.getColor();
                            subMenuItem.setOnMouseEntered(e -> {
                                subMenuItem.setStyle("-fx-background-color: " + menuColor + "; -fx-cursor: hand;");
                                for (Node child : subMenuItem.getChildren()) {
                                    if (child instanceof Label && !((Label) child).getText().equals("▼") && !((Label) child).getText().equals("▲")) {
                                        ((Label) child).setTextFill(Color.WHITE);
                                    }
                                }
                            });
                            
                            subMenuItem.setOnMouseExited(e -> {
                                subMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                                for (Node child : subMenuItem.getChildren()) {
                                    if (child instanceof Label && !((Label) child).getText().equals("▼") && !((Label) child).getText().equals("▲")) {
                                        ((Label) child).setTextFill(Color.LIGHTGRAY);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Belirli başlığa sahip menü container'ını bulur
     */
    private VBox findMenuContainer(String title) {
        // Ana sahneyi ve BorderPane'i al
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        
        // ScrollPane'i al (sol bölümde)
        ScrollPane sidebarScroll = (ScrollPane) root.getLeft();
        
        // VBox sidebar'ı al
        VBox sidebar = (VBox) sidebarScroll.getContent();
        
        // Logo, arama kutusu ve separator'ı atla (ilk 3 öğe)
        for (int i = 3; i < sidebar.getChildren().size(); i++) {
            Node node = sidebar.getChildren().get(i);
            if (node instanceof VBox) {
                VBox container = (VBox) node;
                if (container.getChildren().size() > 0 && container.getChildren().get(0) instanceof HBox) {
                    HBox menuItem = (HBox) container.getChildren().get(0);
                    for (Node child : menuItem.getChildren()) {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            if (label.getText().equals(title)) {
                                return container;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
