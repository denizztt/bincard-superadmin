package com.bincard.bincard_superadmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dedicated API client for authentication-related operations.
 * This class handles all authentication endpoints including signup, login, 
 * phone verification, token refresh, and token management.
 * 
 * Moved from ApiClientFX to maintain single responsibility principle
 * and better code organization.
 */
public class AuthApiClient {
    private static final String BASE_URL = "http://localhost:8080/v1/api";
    
    // =================================================================
    // AUTHENTICATION API METHODS
    // =================================================================
    
    /**
     * Superadmin kayıt işlemi
     * POST /v1/api/auth/superadmin-signup
     */
    public static LoginResponse signup(String name, String surname, String telephone, String password, String email) throws IOException {
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/auth/superadmin-signup").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
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

    /**
     * Superadmin giriş işlemi
     * POST /v1/api/auth/superadmin-login
     */
    public static LoginResponse login(String telephone, String password) throws IOException {
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/auth/superadmin-login").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
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

    /**
     * Telefon doğrulama işlemi
     * POST /v1/api/auth/phone-verify
     */
    public static TokenResponse phoneVerify(String telephone, String verificationCode) throws IOException {
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/auth/phone-verify").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
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
    
    /**
     * Token yenileme işlemi
     * POST /v1/api/auth/refresh
     */
    public static TokenDTO refreshToken(String refreshToken) throws IOException {
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(BASE_URL + "/auth/refresh").toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
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

    /**
     * Yeniden doğrulama kodu gönderilmesi için API'ye istek yapar
     * POST /v1/api/auth/resend-verify-code
     */
    public static String resendVerificationCode(String telephone) throws IOException {
        // Telefon numarasını URL parametresi olarak ekle
        String endpoint = BASE_URL + "/auth/resend-verify-code?telephone=" + telephone;
        
        // URL yapısını Java 20+ uyumlu şekilde oluştur
        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + e.getMessage(), e);
        }
        
        System.out.println("Yeniden doğrulama kodu gönderme URL: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST"); // POST metodu kullanıyoruz, URL parametresi ile birlikte
        conn.setRequestProperty("Content-Type", "application/json");
        
        // POST isteği body gerektirmez, URL parametresi ile çalışır

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
            System.out.println("Resend Verification Code Response: " + resp);

            // Durum kodu ne olursa olsun, yanıttaki mesajı al
            try {
                // message alanını doğrudan extract et
                String message = extractJsonMessage(resp);
                
                if (code == 200 && resp.contains("\"success\":true")) {
                    // Başarılı olduğunda mesajı doğrudan dön
                    return message != null ? message : "Doğrulama kodu gönderildi";
                } else {
                    // Başarısız olduğunda veya başka bir durum kodunda backend hatası ilet
                    throw new IOException(message != null ? message : "Backend hatası: " + code + " - " + resp);
                }
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else {
                    // Genel hata durumu
                    throw new IOException("Backend yanıtı işlenirken hata: " + resp);
                }
            }
        }
    }

    // =================================================================
    // TOKEN MANAGEMENT METHODS
    // =================================================================
    
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
    
    /**
     * Kaydedilmiş token'ları temizler
     * Çıkış işlemlerinde kullanılır
     */
    public static void clearSavedTokens() {
        try {
            TokenSecureStorage.clearTokens();
            System.out.println("Kaydedilmiş token'lar başarıyla temizlendi.");
        } catch (Exception e) {
            System.err.println("Token temizleme sırasında hata: " + e.getMessage());
            throw e;
        }
    }

    // =================================================================
    // UTILITY METHODS
    // =================================================================
    
    /**
     * Dış IP adresini alır
     */
    public static String getPublicIpAddress() {
        try {
            URL whatismyip = new URI("https://api.ipify.org").toURL();
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
    
    /**
     * JSON içinden nested değer çıkarır
     */
    private static String extractNestedValue(String json, String parentKey, String childKey) {
        int parentStart = json.indexOf("\"" + parentKey + "\":{");
        if (parentStart == -1) return null;
        
        int childStart = json.indexOf("\"" + childKey + "\":\"", parentStart);
        if (childStart == -1) return null;
        
        childStart += childKey.length() + 4; // Skip over "key":"
        int childEnd = json.indexOf("\"", childStart);
        
        return json.substring(childStart, childEnd);
    }
    
    /**
     * DateTime string'ini parse eder
     */
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
}
