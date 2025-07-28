package com.bincard.bincard_superadmin;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AllWalletsPage extends SuperadminPageBase {
    private TableView<WalletInfo> walletsTable;
    private List<WalletInfo> walletsList = new ArrayList<>();
    private Label totalWalletsLabel;
    private Label totalBalanceLabel;
    private Pagination pagination;
    private int currentPage = 0;
    private int totalPages = 1;
    private final int pageSize = 10;

    public static class WalletInfo {
        private Long walletId;
        private Long userId;
        private String wiban;
        private String currency;
        private BigDecimal balance;
        private String status;
        private LocalDateTime lastUpdated;
        private int totalTransactionCount;

        public Long getWalletId() { return walletId; }
        public Long getUserId() { return userId; }
        public String getWiban() { return wiban; }
        public String getCurrency() { return currency; }
        public BigDecimal getBalance() { return balance; }
        public String getStatus() { return status; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public int getTotalTransactionCount() { return totalTransactionCount; }
        public String getFormattedBalance() {
            return String.format("%.2f %s", balance != null ? balance : BigDecimal.ZERO, currency != null ? currency : "");
        }
        public String getFormattedLastUpdated() {
            if (lastUpdated == null) return "N/A";
            return lastUpdated.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        public void setWalletId(Long walletId) { this.walletId = walletId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setWiban(String wiban) { this.wiban = wiban; }
        public void setCurrency(String currency) { this.currency = currency; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public void setStatus(String status) { this.status = status; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
        public void setTotalTransactionCount(int totalTransactionCount) { this.totalTransactionCount = totalTransactionCount; }
    }

    public AllWalletsPage(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Tüm Cüzdanlar");
        this.walletsList = new ArrayList<>();
    }

    @Override
    protected Node createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Başlık
        Label titleLabel = new Label("Tüm Cüzdanlar");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // İstatistikler
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER_LEFT);
        totalWalletsLabel = new Label("0");
        totalWalletsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        totalBalanceLabel = new Label("0.00 TRY");
        totalBalanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        statsContainer.getChildren().addAll(new Label("Toplam Cüzdan:"), totalWalletsLabel, new Label("Toplam Bakiye:"), totalBalanceLabel);

        // Tablo
        walletsTable = new TableView<>();
        walletsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        walletsTable.setStyle("-fx-font-size: 13px;");
        TableColumn<WalletInfo, Long> walletIdColumn = new TableColumn<>("Cüzdan ID");
        walletIdColumn.setCellValueFactory(new PropertyValueFactory<>("walletId"));
        TableColumn<WalletInfo, Long> userIdColumn = new TableColumn<>("Kullanıcı ID");
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        TableColumn<WalletInfo, String> wibanColumn = new TableColumn<>("WIBAN");
        wibanColumn.setCellValueFactory(new PropertyValueFactory<>("wiban"));
        TableColumn<WalletInfo, String> balanceColumn = new TableColumn<>("Bakiye");
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedBalance"));
        TableColumn<WalletInfo, String> statusColumn = new TableColumn<>("Durum");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<WalletInfo, Integer> transactionColumn = new TableColumn<>("İşlem Sayısı");
        transactionColumn.setCellValueFactory(new PropertyValueFactory<>("totalTransactionCount"));
        TableColumn<WalletInfo, String> updatedColumn = new TableColumn<>("Son Güncelleme");
        updatedColumn.setCellValueFactory(new PropertyValueFactory<>("formattedLastUpdated"));
        walletsTable.getColumns().addAll(walletIdColumn, userIdColumn, wibanColumn, balanceColumn, statusColumn, transactionColumn, updatedColumn);
        VBox.setVgrow(walletsTable, Priority.ALWAYS);

        // Sayfalama
        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        mainContainer.getChildren().addAll(titleLabel, statsContainer, walletsTable, pagination);
        loadWalletsData(0);
        return mainContainer;
    }

    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadWalletsData(pageIndex);
        return new Label();
    }

    private void loadWalletsData(int page) {
        try {
            String response = WalletApiClient.getAllWallets(accessToken, page);
            if (response != null && !response.isEmpty()) {
                parseWalletsResponse(response);
                updateTable();
                updateStats();
            } else {
                showAlert("Uyarı", "Cüzdan verileri yüklenemedi. Backend API'si çalışıyor olmalı.");
            }
        } catch (Exception e) {
            showAlert("Hata", "Cüzdanlar yüklenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseWalletsResponse(String response) {
        if (walletsList == null) walletsList = new ArrayList<>();
        walletsList.clear();
        try {
            int dataStart = response.indexOf("\"data\":{");
            if (dataStart == -1) return;
            int contentStart = response.indexOf("\"content\":[", dataStart);
            if (contentStart == -1) return;
            contentStart += 11;
            int contentEnd = response.indexOf("]", contentStart);
            if (contentEnd == -1) return;
            String contentArray = response.substring(contentStart, contentEnd);
            parseWalletObjects(contentArray);
            // totalPages
            totalPages = extractIntFromJson(response, "totalPages");
            if (pagination != null) Platform.runLater(() -> pagination.setPageCount(Math.max(totalPages, 1)));
        } catch (Exception e) {
            System.err.println("JSON parse hatası: " + e.getMessage());
        }
    }

    private void parseWalletObjects(String contentArray) {
        int walletCount = 0;
        int braceCount = 0;
        int startIndex = 0;
        boolean inString = false;
        char prevChar = ' ';
        for (int i = 0; i < contentArray.length(); i++) {
            char c = contentArray.charAt(i);
            if (c == '"' && prevChar != '\\') inString = !inString;
            if (!inString) {
                if (c == '{') {
                    if (braceCount == 0) startIndex = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        String walletJson = contentArray.substring(startIndex, i + 1);
                        WalletInfo wallet = parseWalletObject(walletJson);
                        if (wallet != null) walletsList.add(wallet);
                        walletCount++;
                    }
                }
            }
            prevChar = c;
        }
    }

    private WalletInfo parseWalletObject(String jsonObject) {
        try {
            WalletInfo wallet = new WalletInfo();
            wallet.setWalletId(extractLongFromJson(jsonObject, "walletId"));
            wallet.setUserId(extractLongFromJson(jsonObject, "userId"));
            wallet.setWiban(extractStringFromJson(jsonObject, "wiban"));
            wallet.setCurrency(extractStringFromJson(jsonObject, "currency"));
            wallet.setBalance(new BigDecimal(extractStringFromJson(jsonObject, "balance")));
            wallet.setStatus(extractStringFromJson(jsonObject, "status"));
            wallet.setTotalTransactionCount(extractIntFromJson(jsonObject, "totalTransactionCount"));
            String lastUpdatedStr = extractStringFromJson(jsonObject, "lastUpdated");
            if (lastUpdatedStr != null && !lastUpdatedStr.isEmpty() && !"null".equals(lastUpdatedStr)) {
                wallet.setLastUpdated(LocalDateTime.parse(lastUpdatedStr));
            }
            return wallet;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) return m.group(1);
            // Eğer değer null ise
            String nullPattern = "\"" + key + "\"\\s*:\\s*null";
            if (json.matches(".*" + nullPattern + ".*")) return null;
        } catch (Exception ignored) {}
        return "";
    }
    private Long extractLongFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) return Long.parseLong(m.group(1));
        } catch (Exception ignored) {}
        return 0L;
    }
    private int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {}
        return 0;
    }

    private void updateTable() {
        if (walletsTable != null && walletsList != null) {
            Platform.runLater(() -> {
                walletsTable.getItems().clear();
                walletsTable.getItems().addAll(walletsList);
            });
        }
    }

    private void updateStats() {
        if (walletsList == null) return;
        Platform.runLater(() -> {
            if (totalWalletsLabel != null) totalWalletsLabel.setText(String.valueOf(walletsList.size()));
            if (totalBalanceLabel != null) {
                BigDecimal totalBalance = walletsList.stream().map(WalletInfo::getBalance).filter(b -> b != null).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalBalanceLabel.setText(String.format("%.2f TRY", totalBalance));
            }
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
} 