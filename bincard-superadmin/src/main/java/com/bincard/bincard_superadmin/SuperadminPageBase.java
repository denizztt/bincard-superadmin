package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Tüm superadmin sayfaları için temel sınıf.
 * Header, footer ve temel sayfa yapısını içerir.
 */
public abstract class SuperadminPageBase {
    protected Stage stage;
    protected TokenDTO accessToken;
    protected TokenDTO refreshToken;
    protected BorderPane root;
    protected String pageTitle;

    public SuperadminPageBase(Stage stage, TokenDTO accessToken, TokenDTO refreshToken, String pageTitle) {
        this.stage = stage;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.pageTitle = pageTitle;
        createBasicUI();
    }

    /**
     * Temel UI bileşenlerini oluşturur
     */
    private void createBasicUI() {
        // Ana container
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");
        
        // Üst panel (Header)
        HBox header = createHeader();
        root.setTop(header);
        
        // İçerik alanı
        Node content = createContent();
        root.setCenter(content);
        
        // Alt panel (Footer)
        HBox footer = createFooter();
        root.setBottom(footer);
        
        // Scene oluştur
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle(pageTitle + " - Bincard Superadmin");
        stage.setResizable(true);
        
        // Tam ekran modunda aç
        stage.setMaximized(true);
        stage.setFullScreenExitHint("Tam ekrandan çıkmak için ESC tuşuna basın");
        stage.setFullScreen(true);
    }
    
    /**
     * Header bölümünü oluşturur
     */
    // Soft renk paleti için renkler
    protected final String mainColor = "#5d5c61"; // Ana gri renk
    protected final String accentColor1 = "#379683"; // Yumuşak yeşil
    protected final String accentColor2 = "#7395ae"; // Yumuşak mavi
    protected final String accentColor3 = "#557a95"; // Koyu mavi
    protected final String accentColor4 = "#b1a296"; // Yumuşak bej
    
    protected HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setSpacing(20);
        header.setStyle("-fx-background-color: " + mainColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Ana sayfaya dönüş butonu
        Button homeButton = new Button("Ana Sayfa");
        homeButton.setStyle("-fx-background-color: " + accentColor2 + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        homeButton.setOnAction(e -> goToHomePage());
        
        // Sayfa başlığı
        Label title = new Label(pageTitle);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 0, 20));
        
        // Sağ taraf - kullanıcı bilgisi ve çıkış
        HBox rightSide = new HBox();
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.setSpacing(15);
        rightSide.setPrefWidth(Integer.MAX_VALUE);
        
        Label userInfo = new Label("Superadmin");
        userInfo.setFont(Font.font("Montserrat", FontWeight.NORMAL, 16));
        userInfo.setTextFill(Color.WHITE);
        
        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> logout());
        
        rightSide.getChildren().addAll(userInfo, logoutButton);
        
        header.getChildren().addAll(homeButton, title, rightSide);
        return header;
    }
    
    /**
     * Footer bölümünü oluşturur
     */
    protected HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: " + mainColor + ";");
        
        Label footerText = new Label("© 2025 Bincard Superadmin Panel | Tüm Hakları Saklıdır");
        footerText.setFont(Font.font("Montserrat", FontWeight.NORMAL, 14));
        footerText.setTextFill(Color.WHITE);
        
        footer.getChildren().add(footerText);
        return footer;
    }
    
    /**
     * Ana sayfaya yönlendirir
     */
    protected void goToHomePage() {
        new SuperadminDashboardFX(stage, accessToken, refreshToken);
    }
    
    /**
     * Çıkış yapar
     */
    protected void logout() {
        new MainMenuFX(stage);
    }
    
    /**
     * Sayfa içeriğini oluşturur. Alt sınıflar tarafından implement edilmelidir.
     */
    protected abstract Node createContent();
}
