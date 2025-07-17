package com.bincard.bincard_superadmin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.application.Platform;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Sistem İstatistikleri sayfası
 * - Cüzdan sistem istatistikleri
 * - Genel sistem durumu
 * - Performans metrikleri
 */
public class StatisticsPage extends SuperadminPageBase {
    
    private final DecimalFormat numberFormat = new DecimalFormat("#,###");
    
    // Cüzdan istatistikleri UI bileşenleri
    private HBox totalTransactionsLabel;
    private HBox successfulTransactionsLabel;
    private HBox failedTransactionsLabel;
    private HBox totalUsersLabel;
    private HBox activeUsersLabel;
    private HBox suspendedUsersLabel;
    private HBox totalWalletsLabel;
    private HBox activeWalletsLabel;
    private HBox lockedWalletsLabel;
    private HBox totalBalanceLabel;
    private HBox serverTimeLabel;
    private Label lastUpdateLabel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    
    public StatisticsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Sistem İstatistikleri");
        loadWalletStats();
    }
    
    @Override
    protected VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f8f9fa;");
        
        // Başlık
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon statsIcon = new FontIcon(FontAwesomeSolid.CHART_LINE);
        statsIcon.setIconSize(24);
        statsIcon.setIconColor(Color.web("#007bff"));
        
        Label titleLabel = new Label("Sistem İstatistikleri");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#343a40"));
        
        // Yenile butonu
        refreshButton = new Button("Yenile");
        refreshButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC_ALT);
        refreshIcon.setIconSize(14);
        refreshIcon.setIconColor(Color.WHITE);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> loadWalletStats());
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(statsIcon, titleLabel, spacer, loadingIndicator, refreshButton);
        
        // İstatistik kartları
        VBox statsCards = createStatsCards();
        
        // Son güncellenme bilgisi
        HBox updateInfo = new HBox(10);
        updateInfo.setAlignment(Pos.CENTER_LEFT);
        updateInfo.setPadding(new Insets(10));
        updateInfo.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 5;");
        
        FontIcon clockIcon = new FontIcon(FontAwesomeSolid.CLOCK);
        clockIcon.setIconSize(12);
        clockIcon.setIconColor(Color.web("#6c757d"));
        
        lastUpdateLabel = new Label("Son Güncelleme: -");
        lastUpdateLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lastUpdateLabel.setTextFill(Color.web("#6c757d"));
        
        updateInfo.getChildren().addAll(clockIcon, lastUpdateLabel);
        
        content.getChildren().addAll(headerBox, statsCards, updateInfo);
        
        return content;
    }
    
    private VBox createStatsCards() {
        VBox container = new VBox(15);
        
        // Cüzdan İstatistikleri başlığı
        Label walletStatsTitle = new Label("Cüzdan Sistem İstatistikleri");
        walletStatsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        walletStatsTitle.setTextFill(Color.web("#495057"));
        
        // İstatistik kartları için grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10));
        
        // İşlem istatistikleri
        VBox transactionStats = createStatsSection("İşlem İstatistikleri", "#007bff", FontAwesomeSolid.EXCHANGE_ALT);
        totalTransactionsLabel = createStatLabel("Toplam İşlem", "0");
        successfulTransactionsLabel = createStatLabel("Başarılı İşlem", "0");
        failedTransactionsLabel = createStatLabel("Başarısız İşlem", "0");
        transactionStats.getChildren().addAll(totalTransactionsLabel, successfulTransactionsLabel, failedTransactionsLabel);
        
        // Kullanıcı istatistikleri
        VBox userStats = createStatsSection("Kullanıcı İstatistikleri", "#28a745", FontAwesomeSolid.USERS);
        totalUsersLabel = createStatLabel("Toplam Kullanıcı", "0");
        activeUsersLabel = createStatLabel("Aktif Kullanıcı", "0");
        suspendedUsersLabel = createStatLabel("Askıya Alınan", "0");
        userStats.getChildren().addAll(totalUsersLabel, activeUsersLabel, suspendedUsersLabel);
        
        // Cüzdan istatistikleri
        VBox walletStats = createStatsSection("Cüzdan İstatistikleri", "#6f42c1", FontAwesomeSolid.WALLET);
        totalWalletsLabel = createStatLabel("Toplam Cüzdan", "0");
        activeWalletsLabel = createStatLabel("Aktif Cüzdan", "0");
        lockedWalletsLabel = createStatLabel("Kilitli Cüzdan", "0");
        walletStats.getChildren().addAll(totalWalletsLabel, activeWalletsLabel, lockedWalletsLabel);
        
        // Bakiye istatistikleri
        VBox balanceStats = createStatsSection("Bakiye İstatistikleri", "#dc3545", FontAwesomeSolid.COINS);
        totalBalanceLabel = createStatLabel("Toplam Bakiye", "0 TL");
        serverTimeLabel = createStatLabel("Sunucu Zamanı", "-");
        balanceStats.getChildren().addAll(totalBalanceLabel, serverTimeLabel);
        
        // Grid'e yerleştir
        statsGrid.add(transactionStats, 0, 0);
        statsGrid.add(userStats, 1, 0);
        statsGrid.add(walletStats, 0, 1);
        statsGrid.add(balanceStats, 1, 1);
        
        // Her sütunun eşit genişlikte olması için
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        statsGrid.getColumnConstraints().addAll(col1, col2);
        
        container.getChildren().addAll(walletStatsTitle, statsGrid);
        
        return container;
    }
    
    private VBox createStatsSection(String title, String color, FontAwesomeSolid icon) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Başlık
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon sectionIcon = new FontIcon(icon);
        sectionIcon.setIconSize(16);
        sectionIcon.setIconColor(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(color));
        
        titleBox.getChildren().addAll(sectionIcon, titleLabel);
        section.getChildren().add(titleBox);
        
        return section;
    }
    
    private HBox createStatLabel(String name, String value) {
        HBox statBox = new HBox();
        statBox.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name + ":");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        nameLabel.setTextFill(Color.web("#6c757d"));
        nameLabel.setPrefWidth(120);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        valueLabel.setTextFill(Color.web("#212529"));
        
        statBox.getChildren().addAll(nameLabel, valueLabel);
        
        return statBox;
    }
    
    private void loadWalletStats() {
        // UI'yi güncelle
        Platform.runLater(() -> {
            refreshButton.setDisable(true);
            loadingIndicator.setVisible(true);
        });
        
        // Async olarak API çağrısı yap
        CompletableFuture.runAsync(() -> {
            try {
                String response = WalletApiClient.getWalletAdminStats(accessToken);
                
                Platform.runLater(() -> {
                    updateStatsUI(response);
                    refreshButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    
                    // Son güncelleme zamanını güncelle
                    lastUpdateLabel.setText("Son Güncelleme: " + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Cüzdan istatistikleri yüklenirken hata: " + e.getMessage());
                    refreshButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        });
    }
    
    private void updateStatsUI(String jsonResponse) {
        try {
            // JSON response'u parse et
            // Expected format: {"message":"sistem istatistikleri","data":{...},"success":true}
            
            if (jsonResponse.contains("\"success\":true") && jsonResponse.contains("\"data\":{")) {
                // Data bölümünü çıkar
                String dataStart = "\"data\":{";
                int dataStartIndex = jsonResponse.indexOf(dataStart);
                if (dataStartIndex != -1) {
                    int dataEndIndex = jsonResponse.lastIndexOf("}");
                    String dataSection = jsonResponse.substring(dataStartIndex + dataStart.length(), dataEndIndex);
                    
                    // Her bir değeri çıkar ve güncelle
                    updateStatFromJson(dataSection, "totalTransactions", totalTransactionsLabel);
                    updateStatFromJson(dataSection, "successfulTransactions", successfulTransactionsLabel);
                    updateStatFromJson(dataSection, "failedTransactions", failedTransactionsLabel);
                    updateStatFromJson(dataSection, "totalUsers", totalUsersLabel);
                    updateStatFromJson(dataSection, "activeUsers", activeUsersLabel);
                    updateStatFromJson(dataSection, "suspendedUsers", suspendedUsersLabel);
                    updateStatFromJson(dataSection, "totalWallets", totalWalletsLabel);
                    updateStatFromJson(dataSection, "activeWallets", activeWalletsLabel);
                    updateStatFromJson(dataSection, "lockedWallets", lockedWalletsLabel);
                    
                    // Bakiye (TL formatında)
                    updateBalanceFromJson(dataSection, "totalBalance", totalBalanceLabel);
                    
                    // Sunucu zamanı
                    updateTimeFromJson(dataSection, "serverTime", serverTimeLabel);
                    
                    // İstatistikler güncellendi
                } else {
                    showError("API yanıtı beklenmeyen formatta.");
                }
            } else {
                showError("API çağrısı başarısız: " + extractErrorMessage(jsonResponse));
            }
            
        } catch (Exception e) {
            showError("İstatistikler güncellenirken hata: " + e.getMessage());
        }
    }
    
    private void updateStatFromJson(String jsonData, String key, HBox labelBox) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonData);
            
            if (m.find()) {
                long value = Long.parseLong(m.group(1));
                Label valueLabel = (Label) labelBox.getChildren().get(1);
                valueLabel.setText(numberFormat.format(value));
            }
        } catch (Exception e) {
            System.err.println("Stat update hatası (" + key + "): " + e.getMessage());
        }
    }
    
    private void updateBalanceFromJson(String jsonData, String key, HBox labelBox) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonData);
            
            if (m.find()) {
                double value = Double.parseDouble(m.group(1));
                Label valueLabel = (Label) labelBox.getChildren().get(1);
                valueLabel.setText(numberFormat.format(value) + " TL");
            }
        } catch (Exception e) {
            System.err.println("Balance update hatası (" + key + "): " + e.getMessage());
        }
    }
    
    private void updateTimeFromJson(String jsonData, String key, HBox labelBox) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonData);
            
            if (m.find()) {
                String serverTime = m.group(1);
                Label valueLabel = (Label) labelBox.getChildren().get(1);
                
                // ISO datetime formatını daha okunabilir hale getir
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(serverTime);
                    valueLabel.setText(dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                } catch (Exception e) {
                    valueLabel.setText(serverTime);
                }
            }
        } catch (Exception e) {
            System.err.println("Time update hatası (" + key + "): " + e.getMessage());
        }
    }
    
    private String extractErrorMessage(String jsonResponse) {
        try {
            String pattern = "\"message\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonResponse);
            
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("Error message extract hatası: " + e.getMessage());
        }
        return "Bilinmeyen hata";
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
