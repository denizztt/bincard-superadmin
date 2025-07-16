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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ApiClientFX {
    private static final String BASE_URL = "http://localhost:8080/v1/api";

    // =================================================================
    // UTILITY METHODS
    // =================================================================
    
    // Haber API Metotları
    
    public static String getAllNews(TokenDTO accessToken, String platform) throws IOException {
        System.out.println("📰 getAllNews çağrıldı");
        System.out.println("   - Platform: " + platform);
        System.out.println("   - AccessToken: " + (accessToken != null ? "✅ Mevcut" : "❌ Null"));
        
        String endpoint = BASE_URL + "/news/";
        if (platform != null && !platform.isEmpty() && !platform.equals("Tümü")) {
            endpoint += "?platform=" + platform;
        }
        
        System.out.println("   - API Endpoint: " + endpoint);
        
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
        
        System.out.println("   - Authorization Header: Bearer " + accessToken.getToken().substring(0, Math.min(20, accessToken.getToken().length())) + "...");

        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        // Güvenli stream alma
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                // Error stream null ise, response message'ı al
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("   - Response Length: " + responseStr.length());
            System.out.println("   - Response Preview: " + responseStr.substring(0, Math.min(200, responseStr.length())) + "...");
            
            if (code == 200) {
                System.out.println("✅ Haberler başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Haber alma hatası: " + code + " - " + responseStr);
                throw new IOException("Haberler alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    public static String createNews(
            TokenDTO accessToken,
            String title,
            String content,
            byte[] imageData,
            String imageName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String platform,
            String priority,
            String type,
            boolean allowFeedback
    ) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/news/create").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            // Başlık
            writeFormField(os, boundary, "title", title);
            
            // İçerik
            writeFormField(os, boundary, "content", content);
            
            // Görsel (varsa)
            if (imageData != null && imageData.length > 0) {
                writeFileField(os, boundary, "image", imageName, imageData);
            }
            
            // Başlangıç tarihi
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            String formattedStartDate = startDate.format(formatter);
            writeFormField(os, boundary, "startDate", formattedStartDate);
            
            // Bitiş tarihi
            if (endDate != null) {
                String formattedEndDate = endDate.format(formatter);
                writeFormField(os, boundary, "endDate", formattedEndDate);
            }
            
            // Platform
            writeFormField(os, boundary, "platform", platform);
            
            // Öncelik
            writeFormField(os, boundary, "priority", priority);
            
            // Tür
            writeFormField(os, boundary, "type", type);
            
            // Geri bildirim izni
            writeFormField(os, boundary, "allowFeedback", String.valueOf(allowFeedback));
            
            // Form sonlandırma
            os.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                throw new IOException("Haber oluşturulamadı: " + code + " - " + response.toString());
            }
        }
    }
    
    public static String updateNews(
            TokenDTO accessToken,
            Long id,
            String title,
            String content,
            byte[] imageData,
            String imageName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String platform,
            String priority,
            String type,
            Boolean allowFeedback,
            Boolean active
    ) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/news/update").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        // Konsola gönderilen form alanlarını yazdır
        System.out.println("\n===== API REQUEST DETAILS =====");
        System.out.println("URL: " + url.toString());
        System.out.println("Method: PUT");
        System.out.println("Content-Type: multipart/form-data; boundary=" + boundary);
        System.out.println("Authorization: Bearer " + accessToken.getToken().substring(0, Math.min(10, accessToken.getToken().length())) + "...");
        System.out.println("\nForm Fields:");
        System.out.println("- id: " + id);
        if (title != null) System.out.println("- title: " + title);
        if (content != null) System.out.println("- content: " + (content.length() > 50 ? content.substring(0, 50) + "..." : content));
        if (imageData != null) System.out.println("- image: " + imageName + " (" + imageData.length + " bytes)");
        if (startDate != null) System.out.println("- startDate: " + startDate.format(DateTimeFormatter.ISO_DATE_TIME));
        if (endDate != null) System.out.println("- endDate: " + endDate.format(DateTimeFormatter.ISO_DATE_TIME));
        if (platform != null) System.out.println("- platform: " + platform);
        if (priority != null) System.out.println("- priority: " + priority);
        if (type != null) System.out.println("- type: " + type);
        if (allowFeedback != null) System.out.println("- allowFeedback: " + allowFeedback);
        if (active != null) System.out.println("- active: " + active);
        System.out.println("==============================\n");
        
        try (OutputStream os = conn.getOutputStream()) {
            // ID
            writeFormField(os, boundary, "id", id.toString());
            
            // Başlık (varsa)
            if (title != null && !title.isEmpty()) {
                writeFormField(os, boundary, "title", title);
            }
            
            // İçerik (varsa)
            if (content != null && !content.isEmpty()) {
                writeFormField(os, boundary, "content", content);
            }
            
            // Görsel (varsa)
            if (imageData != null && imageData.length > 0) {
                writeFileField(os, boundary, "image", imageName, imageData);
            }
            
            // Başlangıç tarihi (varsa)
            if (startDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                String formattedStartDate = startDate.format(formatter);
                writeFormField(os, boundary, "startDate", formattedStartDate);
            }
            
            // Bitiş tarihi (varsa)
            if (endDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                String formattedEndDate = endDate.format(formatter);
                writeFormField(os, boundary, "endDate", formattedEndDate);
            }
            
            // Platform (varsa)
            if (platform != null && !platform.isEmpty()) {
                writeFormField(os, boundary, "platform", platform);
            }
            
            // Öncelik (varsa)
            if (priority != null && !priority.isEmpty()) {
                writeFormField(os, boundary, "priority", priority);
            }
            
            // Tür (varsa)
            if (type != null && !type.isEmpty()) {
                writeFormField(os, boundary, "type", type);
            }
            
            // Geri bildirim izni (varsa)
            if (allowFeedback != null) {
                writeFormField(os, boundary, "allowFeedback", allowFeedback.toString());
            }
            
            // Aktif/Pasif (varsa)
            if (active != null) {
                writeFormField(os, boundary, "active", active.toString());
            }
            
            // Form sonlandırma
            os.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        
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
            
            // Konsola API yanıtını yazdır
            System.out.println("\n===== API RESPONSE DETAILS =====");
            System.out.println("Status Code: " + code);
            System.out.println("Response: " + response.toString());
            System.out.println("==============================\n");
            
            if (code == 200) {
                return response.toString();
            } else {
                throw new IOException("Haber güncellenemedi: " + code + " - " + response.toString());
            }
        }
    }
    
    public static String softDeleteNews(TokenDTO accessToken, Long id) throws IOException {
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/news/" + id + "/soft-delete").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                throw new IOException("Haber silinemedi: " + code + " - " + response.toString());
            }
        }
    }
    
    // Multipart form veri yazma yardımcı metotları
    private static void writeFormField(OutputStream os, String boundary, String fieldName, String fieldValue) throws IOException {
        String fieldPart = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n" +
                fieldValue + "\r\n";
        os.write(fieldPart.getBytes("UTF-8"));
    }
    
    private static void writeFileField(OutputStream os, String boundary, String fieldName, String fileName, byte[] fileData) throws IOException {
        String filePartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";
        os.write(filePartHeader.getBytes("UTF-8"));
        os.write(fileData);
        os.write("\r\n".getBytes("UTF-8"));
    }

    // JSON içinden "message" veya "error" alanını çeken yardımcı fonksiyon
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
    
    /**
     * Kayıtlı token'ları diskten okuyarak yeni TokenDTO nesneleri oluşturur
     * 
     * @return TokenResponse nesnesi, eğer token'lar bulunamazsa veya geçerli değilse null
     */
    public static TokenResponse getSavedTokens() {
        try {
            // Şifreli token'ları diskten oku
            TokenSecureStorage.TokenPair tokenPair = TokenSecureStorage.retrieveTokens();
            if (tokenPair == null) {
                System.out.println("Kaydedilmiş token bulunamadı.");
                return null;
            }
            
            // Süresi dolmuş mu kontrol et
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessExpiry = LocalDateTime.parse(tokenPair.getAccessExpiry());
            
            if (now.isAfter(accessExpiry)) {
                System.out.println("Access token süresi dolmuş. Yenileme gerekli.");
                
                // Refresh token ile yenilemeyi dene
                try {
                    TokenDTO newAccessToken = AuthApiClient.refreshToken(tokenPair.getRefreshToken());
                    
                    // Yeni tokenları sakla (refresh token değişmediği için eski refresh token'ı kullan)
                    LocalDateTime refreshExpiry = LocalDateTime.parse(tokenPair.getRefreshExpiry());
                    TokenDTO refreshTokenDTO = new TokenDTO(
                            tokenPair.getRefreshToken(),
                            now.minusHours(1), // Tam olmayan issuedAt
                            refreshExpiry,
                            now,
                            AuthApiClient.getPublicIpAddress(),
                            AuthApiClient.getDeviceInfo(),
                            TokenType.REFRESH
                    );
                    
                    // Yeni token'ları kaydet
                    TokenSecureStorage.storeTokens(newAccessToken, refreshTokenDTO);
                    
                    return new TokenResponse(newAccessToken, refreshTokenDTO);
                } catch (Exception e) {
                    System.err.println("Token yenileme hatası: " + e.getMessage());
                    // Yenileme başarısız olursa, eski token'ları sil
                    TokenSecureStorage.clearTokens();
                    return null;
                }
            } else {
                // Token'lar hala geçerli, DTO nesneleri oluştur
                LocalDateTime refreshExpiry = LocalDateTime.parse(tokenPair.getRefreshExpiry());
                
                TokenDTO accessTokenDTO = new TokenDTO(
                        tokenPair.getAccessToken(),
                        now.minusMinutes(5), // Tam olmayan issuedAt
                        accessExpiry,
                        now,
                        AuthApiClient.getPublicIpAddress(),
                        AuthApiClient.getDeviceInfo(),
                        TokenType.ACCESS
                );
                
                TokenDTO refreshTokenDTO = new TokenDTO(
                        tokenPair.getRefreshToken(),
                        now.minusHours(1), // Tam olmayan issuedAt
                        refreshExpiry,
                        now,
                        AuthApiClient.getPublicIpAddress(),
                        AuthApiClient.getDeviceInfo(),
                        TokenType.REFRESH
                );
                
                return new TokenResponse(accessTokenDTO, refreshTokenDTO);
            }
        } catch (Exception e) {
            System.err.println("Token'lar okunurken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // =================================================================
    // SUPERADMIN API METHODS - Backend Controller Integration
    // =================================================================
    
    /**
     * Bekleyen admin onay taleplerini getirir
     * GET /v1/api/superadmin/admin-requests/pending
     * 
     * API Response Format:
     * DataResponseMessage<List<AdminApprovalRequest>>
     */
    public static String getPendingAdminRequests(TokenDTO accessToken, int page, int size) throws IOException {
        String endpoint = BASE_URL + "/superadmin/admin-requests/pending";
        endpoint += "?page=" + page + "&size=" + size + "&sort=createdAt&direction=DESC";
        
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
            
            System.out.println("Admin istekleri yanıtı: " + response.toString());
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Admin istekleri alınamadı: " + code);
            }
        }
    }
    
    /**
     * Admin isteğini onaylar
     * POST /v1/api/admin-requests/{adminId}/approve
     */
    public static String approveAdminRequest(TokenDTO accessToken, Long adminId) throws IOException {
        // Düzeltilmiş endpoint adresi
        String endpoint = BASE_URL + "/superadmin/admin-requests/" + adminId + "/approve";
        
        System.out.println("Onaylama isteği gönderiliyor: " + endpoint);
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        // Boş bir JSON body gönder (Backend boş body bekliyorsa)
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = "{}".getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();
            System.out.println("Onay isteğine boş body gönderildi");
        }
        
        System.out.println("HTTP isteği gönderildi, yanıt bekleniyor...");
        int code = conn.getResponseCode();
        System.out.println("Onaylama isteği yanıt kodu: " + code);
        
        System.out.println("HTTP yanıt başlıkları:");
        conn.getHeaderFields().forEach((key, values) -> {
            if (key != null) {
                System.out.println(key + ": " + String.join(", ", values));
            }
        });
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                        "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            System.out.println("Onaylama işlemi tam yanıtı: " + response.toString());
            
            if (code == 200) {
                System.out.println("Onaylama işlemi başarılı, adminId=" + adminId);
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                System.err.println("Onaylama işlemi başarısız: " + errorMsg + ", HTTP Kodu: " + code);
                throw new IOException(errorMsg != null ? errorMsg : "Admin isteği onaylanamadı: " + code);
            }
        }
    }
    
    /**
     * Admin isteğini reddeder
     * POST /v1/api/admin-requests/{adminId}/reject
     */
    public static String rejectAdminRequest(TokenDTO accessToken, Long adminId) throws IOException {
        // Düzeltilmiş endpoint adresi
        String endpoint = BASE_URL + "/superadmin/admin-requests/" + adminId + "/reject";
        
        System.out.println("Reddetme isteği gönderiliyor: " + endpoint);
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        // Boş bir JSON body gönder (Backend boş body bekliyorsa)
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = "{}".getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();
            System.out.println("Red isteğine boş body gönderildi");
        }
        
        System.out.println("HTTP isteği gönderildi, yanıt bekleniyor...");
        int code = conn.getResponseCode();
        System.out.println("Reddetme isteği yanıt kodu: " + code);
        
        System.out.println("HTTP yanıt başlıkları:");
        conn.getHeaderFields().forEach((key, values) -> {
            if (key != null) {
                System.out.println(key + ": " + String.join(", ", values));
            }
        });
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                        "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            System.out.println("Reddetme işlemi tam yanıtı: " + response.toString());
            
            if (code == 200) {
                System.out.println("Reddetme işlemi başarılı, adminId=" + adminId);
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                System.err.println("Reddetme işlemi başarısız: " + errorMsg + ", HTTP Kodu: " + code);
                throw new IOException(errorMsg != null ? errorMsg : "Admin isteği reddedilemedi: " + code);
            }
        }
    }
    
    /**
     * Gelir özetini getirir (günlük, haftalık, aylık)
     * GET /v1/api/superadmin/income-summary
     */
    public static String getIncomeSummary(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL.replace("/v1/api", "/v1/api/superadmin") + "/income-summary";
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Gelir özeti alınamadı: " + code);
            }
        }
    }
    
    /**
     * Günlük otobüs gelirini getirir
     * GET /v1/api/superadmin/bus-income/daily
     */
    public static String getDailyBusIncome(TokenDTO accessToken, String date) throws IOException {
        String endpoint = BASE_URL.replace("/v1/api", "/v1/api/superadmin") + "/bus-income/daily?date=" + date;
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Günlük gelir verisi alınamadı: " + code);
            }
        }
    }
    
    /**
     * Haftalık otobüs gelirini getirir
     * GET /v1/api/superadmin/bus-income/weekly
     */
    public static String getWeeklyBusIncome(TokenDTO accessToken, String startDate, String endDate) throws IOException {
        String endpoint = BASE_URL.replace("/v1/api", "/v1/api/superadmin") + "/bus-income/weekly?startDate=" + startDate + "&endDate=" + endDate;
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Haftalık gelir verisi alınamadı: " + code);
            }
        }
    }
    
    /**
     * Aylık otobüs gelirini getirir
     * GET /v1/api/superadmin/bus-income/monthly
     */
    public static String getMonthlyBusIncome(TokenDTO accessToken, int year, int month) throws IOException {
        String endpoint = BASE_URL.replace("/v1/api", "/v1/api/superadmin") + "/bus-income/monthly?year=" + year + "&month=" + month;
        
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
            
            if (code == 200) {
                return response.toString();
            } else {
                String errorMsg = extractJsonMessage(response.toString());
                throw new IOException(errorMsg != null ? errorMsg : "Aylık gelir verisi alınamadı: " + code);
            }
        }
    }
    
    /**
     * Audit log geçmişini getirir
     * GET /v1/api/superadmin/audit-logs
     */
    public static String getAuditLogs(TokenDTO accessToken, String fromDate, String toDate, String action) throws IOException {
        String endpoint = BASE_URL.replace("/v1/api", "/v1/api/superadmin") + "/audit-logs?";
        
        if (fromDate != null && !fromDate.isEmpty()) {
            endpoint += "fromDate=" + fromDate + "&";
        }
        if (toDate != null && !toDate.isEmpty()) {
            endpoint += "toDate=" + toDate + "&";
        }
        if (action != null && !action.isEmpty()) {
            endpoint += "action=" + action + "&";
        }
        
        // Remove trailing &
        if (endpoint.endsWith("&")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        
        System.out.println("Audit logs endpoint: " + endpoint);
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            System.err.println("Geçersiz URL formatı: " + endpoint);
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedReader br = null;
        StringBuilder response = new StringBuilder();
        
        try {
            // Bağlantıyı oluştur
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setConnectTimeout(10000); // 10 saniye bağlantı zaman aşımı
            conn.setReadTimeout(10000);    // 10 saniye okuma zaman aşımı
            
            System.out.println("API isteği gönderiliyor: " + endpoint);
            
            // Yanıt kodunu al
            int code = conn.getResponseCode();
            System.out.println("API yanıt kodu: " + code);
            
            // Yanıt veya hata stream'ini al
            if (code == 200) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            
            // Stream null kontrolü
            if (inputStream == null) {
                System.err.println("API yanıt içeriği alınamadı (null stream)");
                return "{\"success\":false,\"message\":\"API yanıt içeriği alınamadı\",\"data\":[]}";
            }
            
            // Yanıtı oku
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("API yanıtı: " + (responseStr.length() > 100 ? responseStr.substring(0, 100) + "..." : responseStr));
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Audit log verileri alınamadı: " + code) + "\",\"data\":[]}";
            }
            
        } catch (Exception e) {
            System.err.println("API isteği sırasında hata: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API isteği başarısız: " + e.getMessage() + "\",\"data\":[]}";
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
     * Belirtilen tarih aralığındaki haberleri getirir
     */
    public static String getNewsBetweenDates(TokenDTO accessToken, LocalDate startDate, LocalDate endDate, String platform) throws IOException {
        System.out.println("📰 getNewsBetweenDates çağrıldı");
        System.out.println("   - StartDate: " + startDate);
        System.out.println("   - EndDate: " + endDate);
        System.out.println("   - Platform: " + platform);
        System.out.println("   - AccessToken: " + (accessToken != null ? "✅ Mevcut" : "❌ Null"));
        
        // Query parameters oluştur
        StringBuilder queryParams = new StringBuilder();
        queryParams.append("start=").append(startDate.toString());
        queryParams.append("&end=").append(endDate.toString());
        queryParams.append("&page=0&size=100"); // Büyük sayfa boyutu
        
        if (platform != null && !platform.isEmpty() && !platform.equals("Tümü")) {
            queryParams.append("&platform=").append(platform);
        }
        
        String endpoint = BASE_URL + "/news/between-dates?" + queryParams.toString();
        System.out.println("   - API Endpoint: " + endpoint);
        
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
        
        System.out.println("   - Authorization Header: Bearer " + accessToken.getToken().substring(0, Math.min(20, accessToken.getToken().length())) + "...");

        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        // Güvenli stream alma
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                // Error stream null ise, response message'ı al
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("   - Response Length: " + responseStr.length());
            System.out.println("   - Response Preview: " + responseStr.substring(0, Math.min(200, responseStr.length())) + "...");
            
            if (code == 200) {
                System.out.println("✅ Tarih aralığındaki haberler başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Tarih aralığındaki haberler alma hatası: " + code + " - " + responseStr);
                throw new IOException("Tarih aralığındaki haberler alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    // =================================================================
    // PAYMENT POINT API METHODS
    // =================================================================
    
    /**
     * Tüm ödeme noktalarını getirir
     */
    public static String getAllPaymentPoints(TokenDTO accessToken, int page, int size, String sort) throws IOException {
        System.out.println("💳 getAllPaymentPoints çağrıldı");
        System.out.println("   - Page: " + page + ", Size: " + size + ", Sort: " + sort);
        
        String endpoint = BASE_URL + "/payment-point?page=" + page + "&size=" + size + "&sort=" + sort;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("   - Response Length: " + responseStr.length());
            
            if (code == 200) {
                System.out.println("✅ Ödeme noktaları başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktaları alma hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktaları alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * ID'ye göre ödeme noktası getirir
     */
    public static String getPaymentPointById(TokenDTO accessToken, Long id) throws IOException {
        System.out.println("💳 getPaymentPointById çağrıldı - ID: " + id);
        
        String endpoint = BASE_URL + "/payment-point/" + id;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Ödeme noktası başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktası alma hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktası alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Yeni ödeme noktası oluşturur
     */
    public static String createPaymentPoint(TokenDTO accessToken, String name, Double latitude, Double longitude,
                                          String street, String district, String city, String postalCode,
                                          String contactNumber, String workingHours, List<String> paymentMethods,
                                          String description, boolean active) throws IOException {
        System.out.println("💳 createPaymentPoint çağrıldı");
        
        String endpoint = BASE_URL + "/payment-point";
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        // JSON payload oluştur
        StringBuilder jsonPayload = new StringBuilder();
        jsonPayload.append("{");
        jsonPayload.append("\"name\":\"").append(name).append("\",");
        jsonPayload.append("\"location\":{");
        jsonPayload.append("\"latitude\":").append(latitude).append(",");
        jsonPayload.append("\"longitude\":").append(longitude);
        jsonPayload.append("},");
        jsonPayload.append("\"address\":{");
        jsonPayload.append("\"street\":\"").append(street != null ? street : "").append("\",");
        jsonPayload.append("\"district\":\"").append(district != null ? district : "").append("\",");
        jsonPayload.append("\"city\":\"").append(city != null ? city : "").append("\",");
        jsonPayload.append("\"postalCode\":\"").append(postalCode != null ? postalCode : "").append("\"");
        jsonPayload.append("},");
        jsonPayload.append("\"contactNumber\":\"").append(contactNumber != null ? contactNumber : "").append("\",");
        jsonPayload.append("\"workingHours\":\"").append(workingHours != null ? workingHours : "").append("\",");
        jsonPayload.append("\"paymentMethods\":[");
        if (paymentMethods != null) {
            for (int i = 0; i < paymentMethods.size(); i++) {
                jsonPayload.append("\"").append(paymentMethods.get(i)).append("\"");
                if (i < paymentMethods.size() - 1) {
                    jsonPayload.append(",");
                }
            }
        }
        jsonPayload.append("},");
        jsonPayload.append("\"description\":\"").append(description != null ? description : "").append("\",");
        jsonPayload.append("\"active\":").append(active);
        jsonPayload.append("}");
        
        System.out.println("   - JSON Payload: " + jsonPayload.toString());
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes("UTF-8"));
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 201) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 201) {
                System.out.println("✅ Ödeme noktası başarıyla oluşturuldu");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktası oluşturma hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktası oluşturulamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme noktasını günceller
     */
    public static String updatePaymentPoint(TokenDTO accessToken, Long id, String name, Double latitude, Double longitude,
                                          String street, String district, String city, String postalCode,
                                          String contactNumber, String workingHours, List<String> paymentMethods,
                                          String description, boolean active) throws IOException {
        System.out.println("💳 updatePaymentPoint çağrıldı - ID: " + id);
        
        String endpoint = BASE_URL + "/payment-point/" + id;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        // JSON payload oluştur
        StringBuilder jsonPayload = new StringBuilder();
        jsonPayload.append("{");
        jsonPayload.append("\"name\":\"").append(name).append("\",");
        jsonPayload.append("\"location\":{");
        jsonPayload.append("\"latitude\":").append(latitude).append(",");
        jsonPayload.append("\"longitude\":").append(longitude);
        jsonPayload.append("},");
        jsonPayload.append("\"address\":{");
        jsonPayload.append("\"street\":\"").append(street != null ? street : "").append("\",");
        jsonPayload.append("\"district\":\"").append(district != null ? district : "").append("\",");
        jsonPayload.append("\"city\":\"").append(city != null ? city : "").append("\",");
        jsonPayload.append("\"postalCode\":\"").append(postalCode != null ? postalCode : "").append("\"");
        jsonPayload.append("},");
        jsonPayload.append("\"contactNumber\":\"").append(contactNumber != null ? contactNumber : "").append("\",");
        jsonPayload.append("\"workingHours\":\"").append(workingHours != null ? workingHours : "").append("\",");
        jsonPayload.append("\"paymentMethods\":[");
        if (paymentMethods != null) {
            for (int i = 0; i < paymentMethods.size(); i++) {
                jsonPayload.append("\"").append(paymentMethods.get(i)).append("\"");
                if (i < paymentMethods.size() - 1) {
                    jsonPayload.append(",");
                }
            }
        }
        jsonPayload.append("},");
        jsonPayload.append("\"description\":\"").append(description != null ? description : "").append("\",");
        jsonPayload.append("\"active\":").append(active);
        jsonPayload.append("}");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes("UTF-8"));
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Ödeme noktası başarıyla güncellendi");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktası güncelleme hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktası güncellenemedi: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme noktasını siler
     */
    public static String deletePaymentPoint(TokenDTO accessToken, Long id) throws IOException {
        System.out.println("💳 deletePaymentPoint çağrıldı - ID: " + id);
        
        String endpoint = BASE_URL + "/payment-point/" + id;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Ödeme noktası başarıyla silindi");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktası silme hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktası silinemedi: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme noktasının durumunu günceller (aktif/pasif)
     */
    public static String togglePaymentPointStatus(TokenDTO accessToken, Long id, boolean active) throws IOException {
        System.out.println("💳 togglePaymentPointStatus çağrıldı - ID: " + id + ", Active: " + active);
        
        String endpoint = BASE_URL + "/payment-point/" + id + "/status?active=" + active;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Ödeme noktası durumu başarıyla güncellendi");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme noktası durum güncelleme hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme noktası durumu güncellenemedi: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Şehir bazlı ödeme noktaları getirir
     */
    public static String getPaymentPointsByCity(TokenDTO accessToken, String city, int page, int size) throws IOException {
        System.out.println("💳 getPaymentPointsByCity çağrıldı - City: " + city);
        
        String endpoint = BASE_URL + "/payment-point/by-city/" + city + "?page=" + page + "&size=" + size;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Şehir bazlı ödeme noktaları başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Şehir bazlı ödeme noktaları alma hatası: " + code + " - " + responseStr);
                throw new IOException("Şehir bazlı ödeme noktaları alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme yöntemine göre ödeme noktaları getirir
     */
    public static String getPaymentPointsByPaymentMethod(TokenDTO accessToken, String paymentMethod, int page, int size) throws IOException {
        System.out.println("💳 getPaymentPointsByPaymentMethod çağrıldı - Payment Method: " + paymentMethod);
        
        String endpoint = BASE_URL + "/payment-point/by-payment-method?paymentMethod=" + paymentMethod + "&page=" + page + "&size=" + size;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Ödeme yöntemine göre ödeme noktaları başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Ödeme yöntemine göre ödeme noktaları alma hatası: " + code + " - " + responseStr);
                throw new IOException("Ödeme yöntemine göre ödeme noktaları alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Yakındaki ödeme noktalarını getirir
     */
    public static String getNearbyPaymentPoints(TokenDTO accessToken, double latitude, double longitude, double radiusKm, int page, int size) throws IOException {
        System.out.println("💳 getNearbyPaymentPoints çağrıldı");
        System.out.println("   - Latitude: " + latitude + ", Longitude: " + longitude + ", Radius: " + radiusKm + "km");
        
        String endpoint = BASE_URL + "/payment-point/nearby?latitude=" + latitude + "&longitude=" + longitude + 
                         "&radiusKm=" + radiusKm + "&page=" + page + "&size=" + size;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        if (accessToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Yakındaki ödeme noktaları başarıyla alındı");
                return responseStr;
            } else {
                System.err.println("❌ Yakındaki ödeme noktaları alma hatası: " + code + " - " + responseStr);
                throw new IOException("Yakındaki ödeme noktaları alınamadı: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme noktasına fotoğraf yükler
     */
    public static String uploadPaymentPointPhotos(TokenDTO accessToken, Long paymentPointId, 
                                                 List<byte[]> imageFiles, List<String> fileNames) throws IOException {
        System.out.println("💳 uploadPaymentPointPhotos çağrıldı - Payment Point ID: " + paymentPointId);
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String endpoint = BASE_URL + "/payment-point/" + paymentPointId + "/photos";
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                writeFileField(os, boundary, "files", fileNames.get(i), imageFiles.get(i));
            }
            
            // Form sonlandırma
            os.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Fotoğraflar başarıyla yüklendi");
                return responseStr;
            } else {
                System.err.println("❌ Fotoğraf yükleme hatası: " + code + " - " + responseStr);
                throw new IOException("Fotoğraflar yüklenemedi: " + code + " - " + responseStr);
            }
        }
    }
    
    /**
     * Ödeme noktasından fotoğraf siler
     */
    public static String deletePaymentPointPhoto(TokenDTO accessToken, Long paymentPointId, Long photoId) throws IOException {
        System.out.println("💳 deletePaymentPointPhoto çağrıldı - Payment Point ID: " + paymentPointId + ", Photo ID: " + photoId);
        
        String endpoint = BASE_URL + "/payment-point/" + paymentPointId + "/photos/" + photoId;
        System.out.println("   - API Endpoint: " + endpoint);
        
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        
        int code = conn.getResponseCode();
        System.out.println("   - HTTP Response Code: " + code);
        
        InputStream responseStream;
        if (code == 200) {
            responseStream = conn.getInputStream();
        } else {
            responseStream = conn.getErrorStream();
            if (responseStream == null) {
                String errorMsg = conn.getResponseMessage();
                System.err.println("❌ API Hatası: " + code + " - " + errorMsg);
                throw new IOException("API Hatası: " + code + " - " + errorMsg);
            }
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            
            if (code == 200) {
                System.out.println("✅ Fotoğraf başarıyla silindi");
                return responseStr;
            } else {
                System.err.println("❌ Fotoğraf silme hatası: " + code + " - " + responseStr);
                throw new IOException("Fotoğraf silinemedi: " + code + " - " + responseStr);
            }
        }
    }
}
