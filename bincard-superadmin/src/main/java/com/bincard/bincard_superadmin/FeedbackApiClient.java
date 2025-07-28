package com.bincard.bincard_superadmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Feedback (Geri Bildirim) API operasyonları için özel client
 * 
 * Bu client feedback API'lerini yönetir:
 * - Tüm geri bildirimleri getirme
 * - Filtreleme ve sayfalama
 */
public class FeedbackApiClient {
    private static final String BASE_URL = "http://localhost:8080/v1/api";
    
    /**
     * Tüm geri bildirimleri getirir - Sayfalama ve filtreleme desteği ile
     * GET /v1/api/feedback/admin/all
     * 
     * @param accessToken Authentication token
     * @param page Sayfa numarası (0-based)
     * @param size Sayfa boyutu
     * @param sort Sıralama parametresi (örn: "submittedAt,desc")
     * @param type Feedback tipi filtresi (opsiyonel)
     * @param source Kaynak filtresi (opsiyonel)
     * @param start Başlangıç tarihi (opsiyonel, yyyy-MM-dd formatında)
     * @param end Bitiş tarihi (opsiyonel, yyyy-MM-dd formatında)
     * @return JSON response string
     */
    public static String getAllFeedbacks(TokenDTO accessToken, int page, int size, String sort, 
                                       String type, String source, String start, String end) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/feedback/admin/all");
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(size);
            urlBuilder.append("&sort=").append(sort != null ? sort : "submittedAt,desc");
            
            // Opsiyonel filtreler
            if (type != null && !type.isEmpty() && !type.equals("Tümü")) {
                urlBuilder.append("&type=").append(type);
            }
            if (source != null && !source.isEmpty() && !source.equals("Tümü")) {
                urlBuilder.append("&source=").append(source);
            }
            if (start != null && !start.isEmpty()) {
                urlBuilder.append("&start=").append(start);
            }
            if (end != null && !end.isEmpty()) {
                urlBuilder.append("&end=").append(end);
            }
            
            System.out.println("🌐 Feedback API URL: " + urlBuilder.toString());
            
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
            System.out.println("📊 Feedback API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("📝 Feedback API yanıt (" + responseStr.length() + " karakter): " + 
                             (responseStr.length() > 200 ? responseStr.substring(0, 200) + "..." : responseStr));
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                System.err.println("❌ Feedback API hatası: " + errorMsg);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Geri bildirimler alınamadı: " + code) + "\",\"data\":{\"content\":[]}}";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Feedback API hatası: " + e.getMessage());
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
     * Belirli bir geri bildirimi getirir
     * GET /v1/api/feedback/admin/{id}
     */
    public static String getFeedbackById(TokenDTO accessToken, Long id) {
        BufferedReader br = null;
        InputStream inputStream = null;
        
        try {
            String endpoint = BASE_URL + "/feedback/admin/" + id;
            System.out.println("🌐 Feedback detay API URL: " + endpoint);
            
            URL url;
            try {
                url = new URI(endpoint).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            conn.setRequestProperty("Content-Type", "application/json");
            
            int code = conn.getResponseCode();
            System.out.println("📊 Feedback detay API yanıt kodu: " + code);
            
            inputStream = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("📝 Feedback detay API yanıtı: " + responseStr);
            
            if (code == 200) {
                return responseStr;
            } else {
                String errorMsg = extractJsonMessage(responseStr);
                return "{\"success\":false,\"message\":\"" + (errorMsg != null ? errorMsg : "Geri bildirim bulunamadı: " + code) + "\"}";
            }
            
        } catch (Exception e) {
            System.err.println("❌ Feedback detay API hatası: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"message\":\"API bağlantı hatası: " + e.getMessage() + "\"}";
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
