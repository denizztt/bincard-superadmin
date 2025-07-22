package com.bincard.bincard_superadmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Cüzdan (Wallet) API operasyonları için özel client
 * 
 * Bu client aşağıdaki wallet API'lerini yönetir:
 * - Sistem istatistikleri
 * - Kimlik doğrulama istekleri
 * - Kimlik doğrulama işlemleri
 */
public class WalletApiClient {
    private static final String BASE_URL = "http://localhost:8080/v1/api";
    
    // =================================================================
    // WALLET ADMIN STATISTICS API
    // =================================================================
    
    /**
     * Cüzdan sistem istatistiklerini getirir
     * GET /v1/api/wallet/admin/stats
     * 
     * Expected Response:
     * {
     *   "message": "sistem istatistikleri",
     *   "data": {
     *     "totalTransactions": 0,
     *     "successfulTransactions": 0,
     *     "totalUsers": 10,
     *     "totalWallets": 0,
     *     "totalBalance": 0,
     *     "activeWallets": 0,
     *     "failedTransactions": 0,
     *     "activeUsers": 10,
     *     "serverTime": "2025-07-12T21:26:47.6229213",
     *     "suspendedUsers": 0,
     *     "lockedWallets": 0
     *   },
     *   "success": true
     * }
     */
    public static String getWalletAdminStats(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/wallet/admin/stats";
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        
        int code = conn.getResponseCode();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                        "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            System.out.println("Wallet admin stats API yanıtı: " + response.toString());
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Cüzdan istatistikleri alınamadı: " + code);
            }
        }
    }
    
    /**
     * Tüm cüzdanları getirir - Sabit sayfa boyutu (10) ve ASC sıralama ile
     * GET /v1/api/wallet/admin/all
     */
    public static String getAllWallets(TokenDTO accessToken, int page) {
        int pageSize = 10; // Sabit sayfa boyutu
        
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/wallet/admin/all");
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(pageSize);
            urlBuilder.append("&sort=id,asc"); // ASC sıralama
            
            System.out.println("Wallet getAllWallets API URL: " + urlBuilder.toString());
            
            // URL yapısını Java 20+ uyumlu şekilde oluştur
            URL url;
            try {
                url = new URI(urlBuilder.toString()).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            int code = conn.getResponseCode();
            System.out.println("Wallet getAllWallets API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("Wallet getAllWallets API tam yanıtı (" + responseStr.length() + " karakter): " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                System.err.println("API hatası: " + errorMsg);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Cüzdanlar alınamadı: " + code) + "\",\"data\":{\"content\":[]}}";
            }
            
        } catch (Exception e) {
            System.err.println("Wallet getAllWallets API hatası: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API bağlantı hatası: " + e.getMessage() + "\",\"data\":{\"content\":[]}}";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("BufferedReader kapatılırken hata: " + e.getMessage());
                }
            }
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("InputStream kapatılırken hata: " + e.getMessage());
                }
            }
        }
    }
    
    // =================================================================
    // WALLET IDENTITY VERIFICATION API
    // =================================================================
    
    /**
     * Kimlik doğrulama isteklerini getirir
     * GET /v1/api/wallet/identity-requests
     */
    public static String getIdentityRequests(TokenDTO accessToken, String status, String startDate, String endDate, 
                                           int page, int size, String sortBy, String sortDir) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/wallet/identity-requests");
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(size);
            urlBuilder.append("&sortBy=").append(sortBy);
            urlBuilder.append("&sortDir=").append(sortDir);
            
            if (status != null && !status.isEmpty()) {
                urlBuilder.append("&status=").append(status);
            }
            if (startDate != null && !startDate.isEmpty()) {
                urlBuilder.append("&startDate=").append(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                urlBuilder.append("&endDate=").append(endDate);
            }
            
            System.out.println("Identity requests API URL: " + urlBuilder.toString());
            
            // URL yapısını Java 20+ uyumlu şekilde oluştur
            URL url;
            try {
                url = new URI(urlBuilder.toString()).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            int code = conn.getResponseCode();
            System.out.println("Identity requests API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("Identity requests API tam yanıtı: " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                System.err.println("API hatası: " + errorMsg);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Kimlik istekleri alınamadı: " + code) + "\",\"data\":{\"content\":[]}}";
            }
            
        } catch (Exception e) {
            System.err.println("Identity requests API isteği sırasında hata: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API isteği başarısız: " + e.getMessage() + "\",\"data\":{\"content\":[]}}";
        } finally {
            // Kaynakları temizle
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("BufferedReader kapatılırken hata: " + e.getMessage());
                }
            }
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("InputStream kapatılırken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Kimlik doğrulama isteğini işler (onayla/reddet)
     * POST /v1/api/wallet/process
     */
    public static String processIdentityRequest(TokenDTO accessToken, Long requestId, boolean approved, String adminNote) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            // URL yapısını Java 20+ uyumlu şekilde oluştur
            URL url;
            try {
                url = new URI(BASE_URL + "/wallet/process").toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String jsonInput = String.format(
                "{\"requestId\":%d,\"approved\":%s,\"adminNote\":\"%s\"}",
                requestId, approved ? "true" : "false", adminNote != null ? adminNote : ""
            );
            
            System.out.println("Process request JSON: " + jsonInput);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int code = conn.getResponseCode();
            System.out.println("Process identity request API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("Process identity request API yanıtı: " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "İstek işlenemedi: " + code) + "\"}";
            }
            
        } catch (Exception e) {
            System.err.println("Process identity request API isteği sırasında hata: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API isteği başarısız: " + e.getMessage() + "\"}";
        } finally {
            // Kaynakları temizle
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("BufferedReader kapatılırken hata: " + e.getMessage());
                }
            }
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("InputStream kapatılırken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Cüzdan transfer işlemlerini getirir
     * GET /v1/api/wallet/transfers
     */
    public static String getWalletTransfers(TokenDTO accessToken, String status, String startDate, String endDate, 
                                          int page, int size, String sortBy, String sortDir) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/wallet/transfers");
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(size);
            urlBuilder.append("&sortBy=").append(sortBy);
            urlBuilder.append("&sortDir=").append(sortDir);
            
            // "Tümü" değerini gönderme, boş bırak
            if (status != null && !status.isEmpty() && !status.equals("Tümü")) {
                urlBuilder.append("&status=").append(status);
            }
            if (startDate != null && !startDate.isEmpty()) {
                urlBuilder.append("&startDate=").append(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                urlBuilder.append("&endDate=").append(endDate);
            }
            
            System.out.println("Wallet transfers API URL: " + urlBuilder.toString());
            
            // URL yapısını Java 20+ uyumlu şekilde oluştur
            URL url;
            try {
                url = new URI(urlBuilder.toString()).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            int code = conn.getResponseCode();
            System.out.println("Wallet transfers API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("Wallet transfers API tam yanıtı: " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                System.err.println("API hatası: " + errorMsg);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Transfer işlemleri alınamadı: " + code) + "\",\"data\":{\"content\":[]}}";
            }
            
        } catch (Exception e) {
            System.err.println("Wallet transfers API hatası: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API bağlantı hatası: " + e.getMessage() + "\",\"data\":{\"content\":[]}}";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("BufferedReader kapatılırken hata: " + e.getMessage());
                }
            }
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("InputStream kapatılırken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Tüm cüzdanları sayfalama ile getirir
     * GET /v1/api/wallet/admin/all
     */
    public static String getAllWallets(TokenDTO accessToken, int page, int size) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/wallet/admin/all");
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(size);
            urlBuilder.append("&sort=id,desc");
            
            System.out.println("All wallets API URL: " + urlBuilder.toString());
            
            // URL yapısını Java 20+ uyumlu şekilde oluştur
            URL url;
            try {
                url = new URI(urlBuilder.toString()).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            int code = conn.getResponseCode();
            System.out.println("All wallets API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("All wallets API tam yanıtı: " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                System.err.println("API hatası: " + errorMsg);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Cüzdanlar alınamadı: " + code) + "\",\"data\":{\"content\":[]}}";
            }
            
        } catch (Exception e) {
            System.err.println("All wallets API hatası: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API bağlantı hatası: " + e.getMessage() + "\",\"data\":{\"content\":[]}}";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("BufferedReader kapatılırken hata: " + e.getMessage());
                }
            }
            
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("InputStream kapatılırken hata: " + e.getMessage());
                }
            }
        }
    }
    
    // =================================================================
    // UTILITY METHODS
    // =================================================================
    
    /**
     * JSON içinden "message" veya "error" alanını çeken yardımcı fonksiyon
     */
    private static String extractJsonMessage(String json) {
        if (json == null) return null;
        String[] keys = {"message", "error", "detail"};
        for (String key : keys) {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                // Değer bir string mi?
                if (json.charAt(startIndex) == '"') {
                    startIndex++;
                    int endIndex = json.indexOf('"', startIndex);
                    if (endIndex != -1) {
                        return json.substring(startIndex, endIndex);
                    }
                } else {
                    int endIndex = json.indexOf(',', startIndex);
                    if (endIndex == -1) endIndex = json.indexOf('}', startIndex);
                    if (endIndex != -1) {
                        return json.substring(startIndex, endIndex).replaceAll("[\"{}]", "").trim();
                    }
                }
            }
        }
        return null;
    }

    private String extractNumberOrStringFromJson(String json, String key) {
        try {
            // Önce string olarak dene
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) return m.group(1);
            // Sonra sayısal olarak dene
            pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(json);
            if (m.find()) return m.group(1);
            // null ise
            String nullPattern = "\"" + key + "\"\\s*:\\s*null";
            if (json.matches(".*" + nullPattern + ".*")) return null;
        } catch (Exception ignored) {}
        return "";
    }
}
