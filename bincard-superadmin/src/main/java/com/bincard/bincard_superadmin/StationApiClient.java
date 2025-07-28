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
import java.net.URLEncoder;

/**
 * Durak (Station) API operasyonları için client
 * Base URL: http://localhost:8080/v1/api/station
 */
public class StationApiClient {
    private static final String BASE_URL = "http://localhost:8080/v1/api/station";
    
    /**
     * Yeni durak oluştur
     * POST /v1/api/station (Bearer token gerekli)
     */
    public static String createStation(TokenDTO accessToken, String stationJson) {
        return makeRequest("POST", BASE_URL, stationJson, accessToken, true);
    }
    
    /**
     * ID ile durak getir
     * GET /v1/api/station/{id} (Token gereksiz)
     */
    public static String getStationById(String stationId) {
        return makeRequest("GET", BASE_URL + "/" + stationId, null, null, false);
    }
    
    /**
     * Durak güncelle
     * PUT /v1/api/station (Bearer token gerekli)
     */
    public static String updateStation(TokenDTO accessToken, String stationJson) {
        return makeRequest("PUT", BASE_URL, stationJson, accessToken, true);
    }
    
    /**
     * Durak durumunu güncelle
     * PATCH /v1/api/station/{id}/status?active={boolean} (Bearer token gerekli)
     */
    public static String changeStationStatus(TokenDTO accessToken, String stationId, boolean active, String stationJson) {
        String url = BASE_URL + "/" + stationId + "/status?active=" + active;
        return makeRequest("PATCH", url, stationJson, accessToken, true);
    }
    
    /**
     * Durak sil
     * DELETE /v1/api/station/{id} (Bearer token gerekli)
     */
    public static String deleteStation(TokenDTO accessToken, String stationId, String stationJson) {
        return makeRequest("DELETE", BASE_URL + "/" + stationId, stationJson, accessToken, true);
    }
    
    /**
     * Durak arama (isim ile)
     * GET /v1/api/station/search?name={name}&page={page}&size={size} (Token gereksiz)
     */
    public static String searchStations(String name, int page, int size) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String url = BASE_URL + "/search?name=" + encodedName + "&page=" + page + "&size=" + size;
            return makeRequest("GET", url, null, null, false);
        } catch (Exception e) {
            return "{\"error\": \"Search failed: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Konum bazlı arama
     * POST /v1/api/station/search/nearby?page={page}&size={size} (Token gereksiz)
     */
    public static String searchNearbyStations(String locationJson, int page, int size) {
        String url = BASE_URL + "/search/nearby?page=" + page + "&size=" + size;
        return makeRequest("POST", url, locationJson, null, false);
    }
    
    /**
     * Anahtar kelime ile arama
     * GET /v1/api/station/keywords?query={query} (Token gereksiz)
     */
    public static String searchByKeywords(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = BASE_URL + "/keywords?query=" + encodedQuery;
            return makeRequest("GET", url, null, null, false);
        } catch (Exception e) {
            return "{\"error\": \"Keyword search failed: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Durağa ait rotalar
     * GET /v1/api/station/routes?stationId={stationId} (Token gereksiz)
     */
    public static String getStationRoutes(String stationId) {
        String url = BASE_URL + "/routes?stationId=" + stationId;
        return makeRequest("GET", url, null, null, false);
    }
    
    /**
     * Durağa ait gelecek araçlar
     * GET /v1/api/tracking/arrivals/station/{stationId}/route/{routeId} (Token gereksiz)
     */
    public static String getStationArrivals(String stationId, String routeId) {
        String url = "http://localhost:8080/v1/api/tracking/arrivals/station/" + stationId + "/route/" + routeId;
        return makeRequest("GET", url, null, null, false);
    }
    
    /**
     * Genel HTTP isteği yapma metodu
     */
    private static String makeRequest(String method, String urlString, String jsonBody, TokenDTO accessToken, boolean requiresAuth) {
        BufferedReader br = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        
        try {
            System.out.println("🌐 " + method + " API URL: " + urlString);
            
            URL url;
            try {
                url = new URI(urlString).toURL();
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URL: " + e.getMessage(), e);
            }
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            
            // Bearer token ekle (gerekirse)
            if (requiresAuth && accessToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
            }
            
            // JSON body gönder (varsa)
            if (jsonBody != null && !jsonBody.isEmpty()) {
                conn.setDoOutput(true);
                outputStream = conn.getOutputStream();
                outputStream.write(jsonBody.getBytes("UTF-8"));
                outputStream.flush();
                System.out.println("📤 Gönderilen JSON: " + jsonBody);
            }
            
            int code = conn.getResponseCode();
            System.out.println("📊 API yanıt kodu: " + code);
            
            inputStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseStr = response.toString();
            System.out.println("📝 API yanıtı: " + responseStr);
            
            if (code >= 200 && code < 300) {
                return responseStr;
            } else {
                return "{\"error\": \"HTTP " + code + "\", \"message\": \"" + responseStr + "\"}";
            }
            
        } catch (Exception e) {
            System.err.println("❌ API hatası: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Request failed\", \"message\": \"" + e.getMessage() + "\"}";
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (br != null) br.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                System.err.println("❌ Stream kapatma hatası: " + e.getMessage());
            }
        }
    }
}
