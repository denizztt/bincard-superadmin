package com.bincard.bincard_superadmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiClientFX {
    private static final String BASE_URL = "http://localhost:8080/v1/api";

    public static LoginResponse signup(String name, String surname, String telephone, String password, String email) throws IOException {
        URL url = new URL(BASE_URL + "/auth/superadmin-signup");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Sınır kontrollü değerler
        String ip = getPublicIpAddress();
        if (ip.length() > 50) ip = ip.substring(0, 50);

        String deviceInfo = getDeviceInfo();
        if (deviceInfo.length() > 50) deviceInfo = deviceInfo.substring(0, 50);

        String appVersion = "1.0";
        if (appVersion.length() > 20) appVersion = appVersion.substring(0, 20);

        String platform = "DESKTOP";
        if (platform.length() > 20) platform = platform.substring(0, 20);

        String jsonInput = String.format(
                "{\"name\":\"%s\",\"surname\":\"%s\",\"telephone\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"ipAddress\":\"%s\",\"deviceInfo\":\"%s\",\"appVersion\":\"%s\",\"platform\":\"%s\"}",
                name, surname, telephone, password, email, ip, deviceInfo, appVersion, platform
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code == 200 || code == 201 ? conn.getInputStream() : conn.getErrorStream(),
                        "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String resp = response.toString();
            System.out.println("Signup Response: " + resp);

            if (code == 200 || code == 201) {
                try {
                    boolean success = resp.contains("\"success\":true");
                    String message = resp.split("\"message\":\"")[1].split("\"")[0];
                    return new LoginResponse(success, message);
                } catch (Exception e) {
                    throw new IOException("Invalid response format: " + resp);
                }
            } else {
                String errorMsg = extractJsonMessage(resp);
                throw new IOException(errorMsg != null ? errorMsg : "Signup failed: " + code + " - " + resp);
            }
        }
    }

    public static LoginResponse login(String telephone, String password) throws IOException {
        URL url = new URL(BASE_URL + "/auth/superadmin-login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Sınır kontrollü değerler
        String ip = getPublicIpAddress();
        if (ip.length() > 50) ip = ip.substring(0, 50);
        
        String deviceInfo = getDeviceInfo();
        if (deviceInfo.length() > 50) deviceInfo = deviceInfo.substring(0, 50);
        
        String jsonInput = String.format(
                "{\"telephone\":\"%s\",\"password\":\"%s\",\"ipAddress\":\"%s\",\"deviceInfo\":\"%s\",\"appVersion\":\"1.0\",\"platform\":\"DESKTOP\"}",
                telephone, password, ip, deviceInfo
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
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

            String resp = response.toString();
            System.out.println("Login Response: " + resp);

            if (code == 200) {
                try {
                    boolean success = resp.contains("\"success\":true");
                    String message = resp.split("\"message\":\"")[1].split("\"")[0];
                    return new LoginResponse(success, message);
                } catch (Exception e) {
                    throw new IOException("Invalid response format: " + resp);
                }
            } else {
                throw new IOException("Login failed: " + code + " - " + resp);
            }
        }
    }

    public static TokenResponse phoneVerify(String telephone, String verificationCode) throws IOException {
        URL url = new URL(BASE_URL + "/auth/phone-verify");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
    
        // Sınır kontrollü değerler
        String ip = getPublicIpAddress();
        if (ip.length() > 50) ip = ip.substring(0, 50);
    
        String deviceInfo = getDeviceInfo();
        if (deviceInfo.length() > 50) deviceInfo = deviceInfo.substring(0, 50);
    
        String appVersion = "1.0";
        if (appVersion.length() > 20) appVersion = appVersion.substring(0, 20);
    
        String platform = "DESKTOP";
        if (platform.length() > 20) platform = platform.substring(0, 20);
    
        String jsonInput = String.format(
                "{\"telephone\":\"%s\",\"code\":\"%s\",\"ipAddress\":\"%s\",\"deviceInfo\":\"%s\",\"appVersion\":\"%s\",\"platform\":\"%s\"}",
                telephone, verificationCode, ip, deviceInfo, appVersion, platform
        );
    
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
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
    
            String resp = response.toString();
            System.out.println("Phone Verify Response: " + resp);
    
            if (code == 200) {
                try {
                    // Access Token
                    String accessToken = extractNestedValue(resp, "accessToken", "token");
                    String refreshToken = extractNestedValue(resp, "refreshToken", "token");
                    if (accessToken == null || refreshToken == null) {
                        // Backend'den gelen hata mesajını çek
                        String errorMsg = extractJsonMessage(resp);
                        throw new IOException(errorMsg != null ? errorMsg : "Doğrulama başarısız. Lütfen kodu ve telefon numarasını kontrol edin.");
                    }
                    LocalDateTime accessIssuedAt = parseDateTime(extractNestedValue(resp, "accessToken", "issuedAt"));
                    LocalDateTime accessExpiresAt = parseDateTime(extractNestedValue(resp, "accessToken", "expiresAt"));
                    String accessIpAddress = extractNestedValue(resp, "accessToken", "ipAddress");
                    String accessDeviceInfo = extractNestedValue(resp, "accessToken", "deviceInfo");
    
                    LocalDateTime refreshIssuedAt = parseDateTime(extractNestedValue(resp, "refreshToken", "issuedAt"));
                    LocalDateTime refreshExpiresAt = parseDateTime(extractNestedValue(resp, "refreshToken", "expiresAt"));
                    String refreshIpAddress = extractNestedValue(resp, "refreshToken", "ipAddress");
                    String refreshDeviceInfo = extractNestedValue(resp, "refreshToken", "deviceInfo");
    
                    TokenDTO accessTokenDTO = new TokenDTO(
                            accessToken,
                            accessIssuedAt,
                            accessExpiresAt,
                            accessIssuedAt,
                            accessIpAddress,
                            accessDeviceInfo,
                            TokenType.ACCESS
                    );
    
                    TokenDTO refreshTokenDTO = new TokenDTO(
                            refreshToken,
                            refreshIssuedAt,
                            refreshExpiresAt,
                            refreshIssuedAt,
                            refreshIpAddress,
                            refreshDeviceInfo,
                            TokenType.REFRESH
                    );
                    
                    // Token'ları güvenli bir şekilde sakla
                    try {
                        TokenSecureStorage.storeTokens(accessTokenDTO, refreshTokenDTO);
                        System.out.println("Token'lar güvenli bir şekilde saklandı.");
                    } catch (Exception e) {
                        System.err.println("Token'lar saklanırken bir hata oluştu: " + e.getMessage());
                        e.printStackTrace();
                        // Hata durumunda bile işleme devam et, kritik bir hata değil
                    }
    
                    return new TokenResponse(accessTokenDTO, refreshTokenDTO);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMsg = extractJsonMessage(resp);
                    throw new IOException(errorMsg != null ? errorMsg : "Invalid response format: " + resp);
                }
            } else {
                // Backend'den gelen hata mesajını çek
                String errorMsg = extractJsonMessage(resp);
                throw new IOException(errorMsg != null ? errorMsg : "Phone verification failed: " + code + " - " + resp);
            }
        }
    }
    
    public static TokenDTO refreshToken(String refreshToken) throws IOException {
        URL url = new URL(BASE_URL + "/auth/refresh");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInput = String.format(
                "{\"refreshToken\":\"%s\"}",
                refreshToken
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
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

            String resp = response.toString();
            System.out.println("Refresh Token Response: " + resp);

            if (code == 200) {
                try {
                    String token = resp.split("\"token\":\"")[1].split("\"")[0];
                    LocalDateTime issuedAt = parseDateTime(resp.split("\"issuedAt\":\"")[1].split("\"")[0]);
                    LocalDateTime expiresAt = parseDateTime(resp.split("\"expiresAt\":\"")[1].split("\"")[0]);
                    LocalDateTime lastUsedAt = parseDateTime(resp.split("\"lastUsedAt\":\"")[1].split("\"")[0]);
                    String ipAddress = resp.split("\"ipAddress\":\"")[1].split("\"")[0];
                    String deviceInfo = resp.split("\"deviceInfo\":\"")[1].split("\"")[0];
                    String tokenType = resp.split("\"tokenType\":\"")[1].split("\"")[0];
                    
                    return new TokenDTO(
                            token, 
                            issuedAt, 
                            expiresAt, 
                            lastUsedAt, 
                            ipAddress, 
                            deviceInfo, 
                            TokenType.valueOf(tokenType)
                    );
                } catch (Exception e) {
                    throw new IOException("Invalid response format: " + resp);
                }
            } else {
                throw new IOException("Token refresh failed: " + code + " - " + resp);
            }
        }
    }

    public static String getPublicIpAddress() {
        try {
            URL whatismyip = new URL("https://api.ipify.org");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            return in.readLine();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Cihaz bilgilerini otomatik olarak toplayan metot.
     * İşletim sistemi, Java versiyonu ve ekran çözünürlüğü gibi bilgileri içerir.
     */
    public static String getDeviceInfo() {
        StringBuilder deviceInfo = new StringBuilder();
        
        // İşletim sistemi bilgileri
        deviceInfo.append("OS: ").append(System.getProperty("os.name"))
                 .append(" ").append(System.getProperty("os.version"))
                 .append(", Arch: ").append(System.getProperty("os.arch"));
                 
        // Java versiyonu
        deviceInfo.append(", Java: ").append(System.getProperty("java.version"));
        
        // Kullanıcı bilgileri
        deviceInfo.append(", User: ").append(System.getProperty("user.name"));
        
        // JVM bilgileri
        deviceInfo.append(", JVM: ").append(System.getProperty("java.vm.name"));
        
        // Hostname
        try {
            deviceInfo.append(", Host: ").append(java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            // Hostname alınamadıysa, devam et
        }
        
        // Maksimum uzunluk kontrolü
        String result = deviceInfo.toString();
        return result.length() > 50 ? result.substring(0, 50) : result;
    }
    
    private static String extractNestedValue(String json, String parentKey, String childKey) {
        int parentStart = json.indexOf("\"" + parentKey + "\":{");
        if (parentStart == -1) return null;
        
        int childStart = json.indexOf("\"" + childKey + "\":\"", parentStart);
        if (childStart == -1) return null;
        
        childStart += childKey.length() + 4; // Skip over "key":"
        int childEnd = json.indexOf("\"", childStart);
        
        return json.substring(childStart, childEnd);
    }
    
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        // Gelen format: "2025-06-29T20:29:49.6046911"
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            // Alternatif format deneyin
            try {
                formatter = DateTimeFormatter.ISO_DATE_TIME;
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception ex) {
                try {
                    formatter = DateTimeFormatter.ISO_DATE_TIME;
                    return LocalDateTime.parse(dateTimeStr, formatter);
                } catch (Exception ex2) {
                    System.err.println("DateTime parse error: " + dateTimeStr);
                    return LocalDateTime.now();
                }
            }
        }
    }
    
    // Haber API Metotları
    
    public static String getAllNews(TokenDTO accessToken, String platform) throws IOException {
        String endpoint = BASE_URL + "/news/";
        if (platform != null && !platform.isEmpty() && !platform.equals("Tümü")) {
            endpoint += "?platform=" + platform;
        }
        
        URL url = new URL(endpoint);
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
                throw new IOException("Haberler alınamadı: " + code + " - " + response.toString());
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
        URL url = new URL(BASE_URL + "/news/create");
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
        URL url = new URL(BASE_URL + "/news/update");
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
        URL url = new URL(BASE_URL + "/news/" + id + "/soft-delete");
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
                    TokenDTO newAccessToken = refreshToken(tokenPair.getRefreshToken());
                    
                    // Yeni tokenları sakla (refresh token değişmediği için eski refresh token'ı kullan)
                    LocalDateTime refreshExpiry = LocalDateTime.parse(tokenPair.getRefreshExpiry());
                    TokenDTO refreshTokenDTO = new TokenDTO(
                            tokenPair.getRefreshToken(),
                            now.minusHours(1), // Tam olmayan issuedAt
                            refreshExpiry,
                            now,
                            getPublicIpAddress(),
                            getDeviceInfo(),
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
                        getPublicIpAddress(),
                        getDeviceInfo(),
                        TokenType.ACCESS
                );
                
                TokenDTO refreshTokenDTO = new TokenDTO(
                        tokenPair.getRefreshToken(),
                        now.minusHours(1), // Tam olmayan issuedAt
                        refreshExpiry,
                        now,
                        getPublicIpAddress(),
                        getDeviceInfo(),
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
}