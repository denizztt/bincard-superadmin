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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.HostServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

// İkon destekleri için import
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class SuperadminDashboardFX {
    private Stage stage;
    private TokenDTO accessToken;
    private TokenDTO refreshToken;
    private HostServices hostServices;
    private List<MenuItem> menuItems = new ArrayList<>();
    
    // Mobil uygulama ile uyumlu modern renk paleti
    private final String primaryColor = "#3F51B5"; // Indigo - Ana renk
    private final String accentColor = "#5C6BC0"; // Light Indigo - Vurgu rengi
    private final String secondaryColor = "#9FA8DA"; // Even lighter Indigo - İkincil renk
    private final String backgroundColor = "#F8F9FA"; // Very light gray with blue hint - Arkaplan
    private final String cardShadowColor = "#E0E0E0"; // Light gray - Kart gölgesi
    private final String textPrimaryColor = "#212121"; // Very dark gray - Ana metin
    private final String textSecondaryColor = "#757575"; // Medium gray - İkincil metin
    
    // Sidebar gradient için değiştirilmiş ana renk
    private final String mainColor = "linear-gradient(to bottom, " + primaryColor + " 0%, " + accentColor + " 100%)"; // Indigo gradient - Sidebar için
    private final String accentColor1 = secondaryColor; // Light Indigo tonu
    private final String accentColor2 = accentColor; // Light Indigo
    private final String accentColor3 = primaryColor; // Ana Indigo
    private final String accentColor4 = "#7986CB"; // Medium Indigo tonu
    
    // Alt menülerin görünürlük durumları
    private Map<String, VBox> subMenuContainers = new HashMap<>();
    
    /**
     * Menü yapısını oluşturur - alfabetik sıralama ile
     */
    private void initializeMenuItems() {
        // Tüm menü öğelerini oluştur
        
        // Admin Onayları (alt menü olmadan)
        MenuItem approvals = new MenuItem("Admin Onayları", accentColor1, FontAwesomeSolid.SHIELD_ALT, "AdminApprovals");
        
        // Duraklar menüsü
        MenuItem stopsMenu = new MenuItem("Duraklar", accentColor3, FontAwesomeSolid.BUS_ALT);
        stopsMenu.addSubItem(new MenuItem("Durak Ekle", accentColor3, FontAwesomeSolid.PLUS_CIRCLE, "StopAdd"));
        stopsMenu.addSubItem(new MenuItem("Durakları Görüntüle", accentColor3, FontAwesomeSolid.LIST, "StopsList"));
        stopsMenu.addSubItem(new MenuItem("Durak Düzenle", accentColor3, FontAwesomeSolid.EDIT, "StopEdit"));
        stopsMenu.addSubItem(new MenuItem("Durak Sil", accentColor3, FontAwesomeSolid.TRASH_ALT, "StopDelete"));
        
        // Haberler menüsü
        MenuItem newsMenu = new MenuItem("Haberler", accentColor3, FontAwesomeSolid.NEWSPAPER);
        newsMenu.addSubItem(new MenuItem("Haber Ekle", accentColor3, FontAwesomeSolid.PLUS_CIRCLE, "NewsAdd"));
        newsMenu.addSubItem(new MenuItem("Haberleri Görüntüle", accentColor3, FontAwesomeSolid.LIST, "NewsList"));
        newsMenu.addSubItem(new MenuItem("Haber Düzenle", accentColor3, FontAwesomeSolid.EDIT, "NewsEdit"));
        newsMenu.addSubItem(new MenuItem("Haber Sil", accentColor3, FontAwesomeSolid.TRASH_ALT, "NewsDelete"));
        
        // İstatistikler
        MenuItem stats = new MenuItem("İstatistikler", accentColor3, FontAwesomeSolid.CHART_BAR, "Statistics");
        
        // Denetim Kayıtları
        MenuItem auditLogs = new MenuItem("Denetim Kayıtları", accentColor4, FontAwesomeSolid.CLIPBOARD_LIST, "AuditLogs");
        
        // Kullanıcılar menüsü
        MenuItem usersMenu = new MenuItem("Kullanıcılar", accentColor4, FontAwesomeSolid.USERS);
        usersMenu.addSubItem(new MenuItem("Kullanıcı Ekle", accentColor4, FontAwesomeSolid.USER_PLUS, "UserAdd"));
        usersMenu.addSubItem(new MenuItem("Kullanıcıları Görüntüle", accentColor4, FontAwesomeSolid.LIST, "UsersList"));
        usersMenu.addSubItem(new MenuItem("Kullanıcı Düzenle", accentColor4, FontAwesomeSolid.USER_EDIT, "UserEdit"));
        usersMenu.addSubItem(new MenuItem("Kullanıcı Sil", accentColor4, FontAwesomeSolid.USER_MINUS, "UserDelete"));
        usersMenu.addSubItem(new MenuItem("Kimlik İstekleri", accentColor4, FontAwesomeSolid.ID_CARD_ALT, "IdentityRequests"));
        
        // Otobüs Rotaları menüsü
        MenuItem routesMenu = new MenuItem("Otobüs Rotaları", accentColor2, FontAwesomeSolid.ROUTE);
        routesMenu.addSubItem(new MenuItem("Rota Ekle", accentColor2, FontAwesomeSolid.PLUS_CIRCLE, "RouteAdd"));
        routesMenu.addSubItem(new MenuItem("Rotaları Görüntüle", accentColor2, FontAwesomeSolid.LIST, "RoutesList"));
        routesMenu.addSubItem(new MenuItem("Rota Düzenle", accentColor2, FontAwesomeSolid.EDIT, "RouteEdit"));
        routesMenu.addSubItem(new MenuItem("Rota Sil", accentColor2, FontAwesomeSolid.TRASH_ALT, "RouteDelete"));
        
        // Otobüsler menüsü
        MenuItem busesMenu = new MenuItem("Otobüsler", accentColor1, FontAwesomeSolid.BUS);
        busesMenu.addSubItem(new MenuItem("Otobüs Ekle", accentColor1, FontAwesomeSolid.PLUS_CIRCLE, "BusAdd"));
        busesMenu.addSubItem(new MenuItem("Otobüsleri Görüntüle", accentColor1, FontAwesomeSolid.LIST, "BusesList"));
        busesMenu.addSubItem(new MenuItem("Otobüs Düzenle", accentColor1, FontAwesomeSolid.EDIT, "BusEdit"));
        busesMenu.addSubItem(new MenuItem("Otobüs Sil", accentColor1, FontAwesomeSolid.TRASH_ALT, "BusDelete"));
        
        // Raporlar menüsü
        MenuItem reportsMenu = new MenuItem("Raporlar", accentColor2, FontAwesomeSolid.FILE_ALT);
        reportsMenu.addSubItem(new MenuItem("Gelir Raporları", accentColor2, FontAwesomeSolid.CHART_LINE, "IncomeReports"));
        reportsMenu.addSubItem(new MenuItem("Günlük Raporlar", accentColor2, FontAwesomeSolid.CALENDAR_DAY, "DailyReports"));
        reportsMenu.addSubItem(new MenuItem("Aylık Raporlar", accentColor2, FontAwesomeSolid.CALENDAR_ALT, "MonthlyReports"));
        reportsMenu.addSubItem(new MenuItem("Yıllık Raporlar", accentColor2, FontAwesomeSolid.CALENDAR, "YearlyReports"));
        
        // Şoförler menüsü
        MenuItem driversMenu = new MenuItem("Şoförler", accentColor2, FontAwesomeSolid.ID_CARD);
        driversMenu.addSubItem(new MenuItem("Şoför Ekle", accentColor2, FontAwesomeSolid.USER_PLUS, "DriverAdd"));
        driversMenu.addSubItem(new MenuItem("Şoförleri Görüntüle", accentColor2, FontAwesomeSolid.LIST, "DriversList"));
        driversMenu.addSubItem(new MenuItem("Şoför Düzenle", accentColor2, FontAwesomeSolid.USER_EDIT, "DriverEdit"));
        driversMenu.addSubItem(new MenuItem("Şoför Sil", accentColor2, FontAwesomeSolid.USER_MINUS, "DriverDelete"));
        
        // Ödeme Noktaları menüsü
        MenuItem paymentPointsMenu = new MenuItem("Ödeme Noktaları", accentColor3, FontAwesomeSolid.CREDIT_CARD);
        paymentPointsMenu.addSubItem(new MenuItem("Ödeme Noktası Ekle", accentColor3, FontAwesomeSolid.PLUS_CIRCLE, "PaymentPointAdd"));
        paymentPointsMenu.addSubItem(new MenuItem("Tablo Görünümü", accentColor3, FontAwesomeSolid.TABLE, "PaymentPointsList"));
        paymentPointsMenu.addSubItem(new MenuItem("Harita Görünümü", accentColor3, FontAwesomeSolid.MAP_MARKED_ALT, "PaymentPointsMap"));
        
        // Cüzdanlar menüsü
        MenuItem walletsMenu = new MenuItem("Cüzdanlar", accentColor4, FontAwesomeSolid.WALLET);
        walletsMenu.addSubItem(new MenuItem("Cüzdan Durumu Güncelleme", accentColor4, FontAwesomeSolid.EDIT, "WalletStatusUpdate"));
        walletsMenu.addSubItem(new MenuItem("Tüm Cüzdanlar", accentColor4, FontAwesomeSolid.LIST, "AllWallets"));
        
        // Alfabetik sırada menü listesine ekle
        menuItems.add(approvals);  // Admin Onayları
        menuItems.add(auditLogs);  // Denetim Kayıtları
        menuItems.add(walletsMenu); // Cüzdanlar
        menuItems.add(stopsMenu);  // Duraklar
        menuItems.add(newsMenu);   // Haberler
        menuItems.add(stats);      // İstatistikler
        menuItems.add(paymentPointsMenu); // Ödeme Noktaları
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
        root.setStyle("-fx-background-color: " + backgroundColor + ";");
        
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
        scene.getStylesheets().clear(); // Varsayılan stil dosyalarını temizle
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
        
        // Sol taraf - Dashboard başlığı
        Label dashboardTitle = new Label("Ana Sayfa");
        dashboardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        dashboardTitle.setTextFill(Color.web(textPrimaryColor));
        dashboardTitle.setPadding(new Insets(0, 0, 0, 0));
        
        // Orta kısım - boş alan için spacer
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Sağ taraf - kullanıcı bilgisi ve çıkış
        HBox rightSide = new HBox();
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.setSpacing(15);
        
        // Bildirim ikonu - tıklanabilir ve mavi renkli
        FontIcon notificationIcon = new FontIcon(FontAwesomeSolid.BELL);
        notificationIcon.setIconSize(18);
        notificationIcon.setIconColor(Color.web(primaryColor));
        HBox notificationBox = new HBox(notificationIcon);
        notificationBox.setAlignment(Pos.CENTER);
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
        
        // Kullanıcı avatarı ikonu
        FontIcon avatarIcon = new FontIcon(FontAwesomeSolid.USER_CIRCLE);
        avatarIcon.setIconSize(24);
        avatarIcon.setIconColor(Color.web(primaryColor));
        HBox avatarContainer = new HBox(avatarIcon);
        avatarContainer.setAlignment(Pos.CENTER);
        avatarContainer.setPadding(new Insets(0, 5, 0, 0));
        
        Label userInfo = new Label("Superadmin");
        userInfo.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        userInfo.setTextFill(Color.web(textPrimaryColor));
        
        userInfoBox.getChildren().addAll(avatarContainer, userInfo);
        
        // Çıkış butonu - basit metin buton
        Button logoutButton = new Button("Çıkış");
        logoutButton.setStyle("-fx-background-color: " + primaryColor + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 10; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> logout());
        
        rightSide.getChildren().addAll(notificationBox, userInfoBox, logoutButton);
        
        // Header'a öğeleri ekle - sadece gereken öğeler
        header.getChildren().addAll(dashboardTitle, spacer, rightSide);
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
        footerText.setTextFill(Color.web(textSecondaryColor));
        
        footer.getChildren().add(footerText);
        return footer;
    }
    
    /**
     * Sidebar menüsünü oluşturur
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background: " + mainColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 0);");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // Logo/başlık alanı
        Label logoLabel = new Label("BINCARD");
        logoLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 22));
        logoLabel.setTextFill(Color.WHITE);
        logoLabel.setPadding(new Insets(10, 0, 15, 0));
        
        // Arama container
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setMaxWidth(200);
        searchContainer.setStyle("-fx-background-color: transparent;");
        
        // Arama ikonu - sorunlu olduğu için HBox ile değiştiriyoruz
        Label searchLabel = new Label("🔍");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        searchLabel.setTextFill(Color.WHITE);
        searchLabel.setStyle("-fx-cursor: hand;");
        
        // Arama çubuğu (başlangıçta gizli)
        TextField searchMenuField = new TextField();
        searchMenuField.setPromptText("Menüde ara...");
        searchMenuField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 8; -fx-font-size: 14px;");
        searchMenuField.setPrefWidth(160);
        searchMenuField.setMaxWidth(160);
        searchMenuField.setVisible(false);
        searchMenuField.setManaged(false);
        
        // Arama sonucu bilgisi için label (başlangıçta görünmez)
        Label searchResultLabel = new Label("Sonuç bulunamadı");
        searchResultLabel.setTextFill(Color.LIGHTPINK);
        searchResultLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        searchResultLabel.setVisible(false);
        searchResultLabel.setManaged(false);
        
        // Arama ikonu container
        VBox searchIconBox = new VBox(5);
        searchIconBox.setAlignment(Pos.CENTER);
        searchIconBox.getChildren().addAll(searchLabel);
        
        // Arama çubuğu container
        VBox searchFieldBox = new VBox(5);
        searchFieldBox.setAlignment(Pos.CENTER);
        searchFieldBox.getChildren().addAll(searchMenuField, searchResultLabel);
        searchFieldBox.setVisible(false);
        searchFieldBox.setManaged(false);
        
        // İkona tıklama olayı - arama çubuğunu göster/gizle
        searchLabel.setOnMouseClicked(e -> {
            boolean isVisible = searchFieldBox.isVisible();
            searchFieldBox.setVisible(!isVisible);
            searchFieldBox.setManaged(!isVisible);
            searchMenuField.setVisible(!isVisible);
            searchMenuField.setManaged(!isVisible);
            
            // Arama çubuğu görünürse focus yap ve ikonu gizle
            if (!isVisible) {
                searchMenuField.requestFocus();
                searchIconBox.setVisible(false);
                searchIconBox.setManaged(false);
            }
        });
        
        // Arama çubuğu kaybettiğinde ve içeriği boş ise, ikonu tekrar göster
        searchMenuField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && searchMenuField.getText().isEmpty()) {
                searchFieldBox.setVisible(false);
                searchFieldBox.setManaged(false);
                searchIconBox.setVisible(true);
                searchIconBox.setManaged(true);
            }
        });
        
        // Containerları ana container'a ekle
        searchContainer.getChildren().addAll(searchIconBox, searchFieldBox);
        
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
                
                // Ana menü tıklama olayı (alt menüyü göster/gizle) - Accordion mantığı ile
                mainMenuItem.setOnMouseClicked(e -> {
                    boolean isVisible = subMenuBox.isVisible();
                    
                    // Accordion mantığı: Önce tüm alt menüleri kapat
                    closeAllSubMenus();
                    
                    // Eğer menü kapalıysa aç, açıksa aç (çünkü yukarıda kapattık)
                    if (!isVisible) {
                        subMenuBox.setVisible(true);
                        subMenuBox.setManaged(true);
                        
                        // Ana menü arka plan rengini değiştir (açık durumu göstermek için)
                        mainMenuItem.setStyle("-fx-background-color: " + menuItem.getColor() + "; -fx-cursor: hand;");
                        // Ok işaretini çevir (açık)
                        if (finalArrowLabel != null) {
                            finalArrowLabel.setText("▲");
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
        
        // FontIcon kullanarak ikon oluştur
        FontIcon icon = new FontIcon(menuItem.getIcon());
        icon.setIconSize(18);
        icon.setIconColor(Color.WHITE);
        
        // Başlık
        Label titleLabel = new Label(menuItem.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 15));
        titleLabel.setTextFill(Color.WHITE);
        
        menuBox.getChildren().addAll(icon, titleLabel);
        
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
        
        // FontIcon kullanarak ikon oluştur
        FontIcon icon = new FontIcon(subItem.getIcon());
        icon.setIconSize(14);
        icon.setIconColor(Color.LIGHTGRAY);
        
        // Başlık - daha küçük font
        Label titleLabel = new Label(subItem.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);
        
        subMenuBox.getChildren().addAll(icon, titleLabel);
        
        // Hover efektleri
        subMenuBox.setOnMouseEntered(e -> {
            subMenuBox.setStyle("-fx-background-color: #557a95; -fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE);
            icon.setIconColor(Color.WHITE);
        });
        
        subMenuBox.setOnMouseExited(e -> {
            subMenuBox.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            titleLabel.setTextFill(Color.LIGHTGRAY);
            icon.setIconColor(Color.LIGHTGRAY);
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
        content.setStyle("-fx-background-color: linear-gradient(to bottom, " + primaryColor + " 0%, " + accentColor + " 100%);");
        
        // Kullanıcı adını al (basit bir çözüm)
        String userDisplayName = "Yönetici";
        
        // Hoşgeldiniz başlığı
        Label welcomeTitle = new Label("Merhaba " + userDisplayName + " 👋");
        welcomeTitle.setFont(Font.font("Montserrat", FontWeight.BOLD, 36));
        welcomeTitle.setTextFill(Color.WHITE);
        
        // Alt başlık
        Label subtitle = new Label("Bincard Superadmin Panel'e Hoşgeldiniz");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        subtitle.setTextFill(Color.web("#f8f9fa"));
        
        // Açıklama metni
        Label description = new Label("Sistem yönetimi ve analiz işlemleri için tasarlanmış kontrol paneli");
        description.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        description.setTextFill(Color.web("#e9ecef"));
        
        // Sistem saati - Anlık güncellenen
        Label timeLabel = new Label();
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        timeLabel.setTextFill(Color.WHITE);
        
        // Saati güncelle
        updateTimeLabel(timeLabel);
        
        // Saati her saniye güncelle
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateTimeLabel(timeLabel))
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
        
        // İstatistik kartları
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(30, 0, 0, 0));
        
        // İstatistik kartları - Gerçek veriler yüklenene kadar sıfır değerler
        VBox totalUsers = createStatCard("Toplam Kullanıcılar", "0", primaryColor, "\uf0c0");
        VBox activeUsers = createStatCard("Aktif Kullanıcılar", "0", accentColor, "\uf0c1");
        VBox totalBuses = createStatCard("Toplam Otobüsler", "0", secondaryColor, "\uf207");
        VBox dailyIncome = createStatCard("Günlük Gelir", "₺0", "#7986CB", "\uf155");
        
        statsContainer.getChildren().addAll(totalUsers, activeUsers, totalBuses, dailyIncome);
        
        // Gelir verilerini API'dan yükle ve kartları güncelle
        loadDashboardData(dailyIncome);
        
        // Hızlı erişim butonları
        HBox quickActionsContainer = new HBox(15);
        quickActionsContainer.setAlignment(Pos.CENTER);
        quickActionsContainer.setPadding(new Insets(40, 0, 0, 0));
        // Harita butonunu kaldırdım, quickActionsContainer'a ekleme yok
        content.getChildren().clear();
        content.getChildren().addAll(welcomeTitle, subtitle, description, timeLabel, statsContainer, quickActionsContainer);
        
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
        statCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, " + cardShadowColor + ", 10, 0, 0, 5); -fx-border-color: " + cardShadowColor + "; -fx-border-radius: 15; -fx-border-width: 1;");
        
        // Gradient renkli üst bar
        HBox colorBar = new HBox();
        colorBar.setPrefHeight(4);
        colorBar.setMaxHeight(4);
        colorBar.setStyle("-fx-background: " + color + "; -fx-background-radius: 15 15 0 0;");
        
        // İkon ve değer alanı
        VBox contentArea = new VBox(15);
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setPadding(new Insets(20, 20, 20, 20));
        
        // Değer - daha büyük ve vurgulu
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Montserrat", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(textPrimaryColor));
        
        // Başlık - daha küçük ve subtle
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        titleLabel.setTextFill(Color.web(textSecondaryColor));
        titleLabel.setWrapText(true);
        
        contentArea.getChildren().addAll(valueLabel, titleLabel);
        statCard.getChildren().addAll(colorBar, contentArea);
        
        return statCard;
    }
    
    /**
     * Dashboard verilerini API'dan yükler
     */
    private void loadDashboardData(VBox dailyIncomeCard) {
        // Gelir verilerini asenkron olarak yükle
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                String response = ApiClientFX.getIncomeSummary(accessToken);
                return parseIncomeSummary(response);
            } catch (Exception e) {
                System.err.println("Dashboard API'si mevcut değil, örnek verilerle devam ediliyor: " + e.getMessage());
                // API mevcut değilse örnek verilerle devam et
                return new double[]{3500.0, 24500.0, 105000.0, 125000.0}; // örnek veriler
            }
        }).thenAccept(incomeData -> {
            if (incomeData != null) {
                javafx.application.Platform.runLater(() -> {
                    // Günlük gelir kartını güncelle
                    Label valueLabel = (Label) dailyIncomeCard.getChildren().get(1);
                    valueLabel.setText(String.format("₺%,.0f", incomeData[0])); // dailyIncome
                });
            }
        });
    }
    
    /**
     * Gelir API yanıtını parse eder
     */
    private double[] parseIncomeSummary(String jsonResponse) {
        try {
            double[] result = new double[4]; // daily, weekly, monthly, total
            
            if (jsonResponse.contains("\"data\":{")) {
                String dataSection = jsonResponse.split("\"data\":")[1];
                if (dataSection.startsWith("{")) {
                    int endIndex = dataSection.lastIndexOf("}");
                    if (endIndex > 0) {
                        dataSection = dataSection.substring(1, endIndex);
                    }
                    
                    // JSON değerlerini parse et
                    result[0] = extractDoubleFromJson(dataSection, "dailyIncome");
                    result[1] = extractDoubleFromJson(dataSection, "weeklyIncome");
                    result[2] = extractDoubleFromJson(dataSection, "monthlyIncome");
                    result[3] = extractDoubleFromJson(dataSection, "totalIncome");
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("Gelir parse hatası: " + e.getMessage());
            return new double[]{0, 0, 0, 0};
        }
    }
    
    /**
     * JSON string'den double değer çıkarır
     */
    private double extractDoubleFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+\\.?[0-9]*)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        } catch (Exception e) {
            System.err.println("Double değer parse hatası: " + e.getMessage());
        }
        return 0.0;
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
                case "AuditLogs":
                    new AuditLogsPage(stage, accessToken, refreshToken);
                    break;
                case "Statistics":
                    new StatisticsPage(stage, accessToken, refreshToken);
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
                case "IdentityRequests":
                    new IdentityRequestsPage(stage, accessToken, refreshToken, hostServices);
                    break;
                    
                // Rapor alt sayfaları
                case "IncomeReports":
                    new IncomeReportsPage(stage, accessToken, refreshToken);
                    break;
                case "DailyReports":
                    showUnderConstructionAlert("Günlük Raporlar");
                    break;
                case "MonthlyReports":
                    showUnderConstructionAlert("Aylık Raporlar");
                    break;
                case "YearlyReports":
                    showUnderConstructionAlert("Yıllık Raporlar");
                    break;
                    
                // Ödeme Noktaları sayfaları
                case "PaymentPointsList":
                    new PaymentPointsPage(stage, accessToken, refreshToken);
                    break;
                case "PaymentPointsMap":
                    new PaymentPointsMapPage(stage, accessToken, refreshToken);
                    break;
                case "PaymentPointAdd":
                    new PaymentPointAddPage(stage, accessToken, refreshToken);
                    break;
                    
                // Eski sayfalar ve diğerleri
                case "Otobüs Kartları":
                case "Geri Bildirimler":
                case "Raporlar":
                case "Otobüs Rotaları":
                case "Duraklar":
                case "Kullanıcılar":
                case "Cüzdan":
                case "İstatistikler":
                    new IncomeReportsPage(stage, accessToken, refreshToken);
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
        try {
            // Kayıtlı token'ları temizle
            System.out.println("Çıkış yapılıyor, token'lar temizleniyor...");
            AuthApiClient.clearSavedTokens();
            System.out.println("Token'lar başarıyla temizlendi.");
        } catch (Exception e) {
            System.err.println("Token temizleme sırasında hata: " + e.getMessage());
        }
        
        // Ana menüye dön
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
    
    /**
     * Tüm alt menüleri kapatır - Accordion mantığı için
     */
    private void closeAllSubMenus() {
        for (Map.Entry<String, VBox> entry : subMenuContainers.entrySet()) {
            String menuTitle = entry.getKey();
            VBox subMenuBox = entry.getValue();
            
            // Alt menüyü kapat
            subMenuBox.setVisible(false);
            subMenuBox.setManaged(false);
            
            // Ana menü öğesinin stilini sıfırla ve ok işaretini aşağı çevir
            // Ana menü öğesini bul
            for (MenuItem menuItem : menuItems) {
                if (menuItem.getTitle().equals(menuTitle) && menuItem.hasSubItems()) {
                    // Ana menü öğesinin HBox'ını bul
                    HBox mainMenuItem = findMainMenuItemBox(menuTitle);
                    if (mainMenuItem != null) {
                        // Arka plan stilini şeffaf yap
                        mainMenuItem.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                        
                        // Ok işaretini aşağı çevir
                        for (Node child : mainMenuItem.getChildren()) {
                            if (child instanceof Label && (((Label) child).getText().equals("▲") || ((Label) child).getText().equals("▼"))) {
                                ((Label) child).setText("▼");
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Belirtilen menü başlığına sahip ana menü öğesinin HBox'ını bulur
     */
    private HBox findMainMenuItemBox(String menuTitle) {
        // Bu metod sidebar içindeki VBox'ları dolaşır ve doğru ana menü öğesini bulur
        // Sidebar'daki tüm çocukları kontrol et
        if (stage != null && stage.getScene() != null && stage.getScene().getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            if (root.getLeft() instanceof VBox) {
                VBox sidebar = (VBox) root.getLeft();
                for (Node child : sidebar.getChildren()) {
                    if (child instanceof VBox) {
                        VBox menuContainer = (VBox) child;
                        if (menuContainer.getChildren().size() > 0 && menuContainer.getChildren().get(0) instanceof HBox) {
                            HBox mainMenuItem = (HBox) menuContainer.getChildren().get(0);
                            // Bu ana menü öğesinin başlığını bul
                            for (Node menuChild : mainMenuItem.getChildren()) {
                                if (menuChild instanceof Label) {
                                    Label titleLabel = (Label) menuChild;
                                    if (titleLabel.getText().equals(menuTitle)) {
                                        return mainMenuItem;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Sistem saatini günceller
     */
    private void updateTimeLabel(Label timeLabel) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("tr-TR"));
        String timeText = "🕒 " + now.format(formatter);
        timeLabel.setText(timeText);
    }
}
