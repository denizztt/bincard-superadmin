package com.bincard.bincard_superadmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Ödeme noktası API işlemleri için özel client sınıfı
 * Tüm payment point related API çağrıları bu sınıfta toplanmıştır
 */
public class PaymentPointApiClient {
    
    private static final String BASE_URL = "http://localhost:8080/v1/api";
    
    /**
     * Tüm ödeme noktalarını getirir (Token ile)
     * GET /payment-point
     */
    public static String getAllPaymentPoints(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktaları alınamadı");
    }

    /**
     * Tüm ödeme noktalarını sayfalama ile getirir (Token gerektirmez) - ANA METOD
     * GET /payment-point?page={page}&size={size}&sort={sort}
     */
    public static String getAllPaymentPoints(int page, int size, String sort) throws IOException {
        String endpoint = BASE_URL + "/payment-point?page=" + page + "&size=" + size + "&sort=" + sort;
        
        System.out.println("🌐 API İSTEĞİ GÖNDERİLİYOR:");
        System.out.println("   - Endpoint: " + endpoint);
        System.out.println("   - Sayfa: " + page + " (0-tabanlı, backend formatı)");
        System.out.println("   - Boyut: " + size);
        System.out.println("   - Sıralama: " + sort);
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        // Token gönderilmiyor - postman testine göre gerekli değil

        String response = executeRequest(conn, "Sayfalı ödeme noktaları alınamadı");
        System.out.println("✅ API YANITI ALINDI, uzunluk: " + response.length());
        return response;
    }

    /**
     * Ödeme noktası durumunu değiştirir
     * PATCH /payment-point/{id}/status?id={id}&active={active}
     */
    public static String updatePaymentPointStatus(Long id, boolean active, TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id + "/status?id=" + id + "&active=" + active;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası durumu güncellenemedi");
    }

    /**
     * Ödeme noktasını siler
     * DELETE /payment-point/{id}
     */
    public static String deletePaymentPoint(Long id, TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası silinemedi");
    }

    /**
     * Ödeme noktasını günceller
     * PUT /payment-point/{id}
     */
    public static String updatePaymentPoint(Long id, PaymentPointUpdateDTO paymentPointData, TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON body oluştur
        String jsonBody = createPaymentPointUpdateJson(paymentPointData);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Ödeme noktası güncellenemedi");
    }

    /**
     * Yeni ödeme noktası ekler
     * POST /payment-point
     */
    public static String createPaymentPoint(PaymentPointUpdateDTO paymentPointData, TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON body oluştur
        String jsonBody = createPaymentPointUpdateJson(paymentPointData);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Ödeme noktası eklenemedi");
    }

    /**
     * PaymentPointUpdateDTO'dan JSON string oluşturur
     */
    private static String createPaymentPointUpdateJson(PaymentPointUpdateDTO data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Name
        if (data.getName() != null) {
            json.append("\"name\":\"").append(escapeJsonString(data.getName())).append("\",");
        }
        
        // Location
        if (data.getLocation() != null) {
            json.append("\"location\":{");
            if (data.getLocation().getLatitude() != null) {
                json.append("\"latitude\":").append(data.getLocation().getLatitude()).append(",");
            }
            if (data.getLocation().getLongitude() != null) {
                json.append("\"longitude\":").append(data.getLocation().getLongitude());
            }
            // Son virgülü kaldır
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("},");
        }
        
        // Address
        if (data.getAddress() != null) {
            json.append("\"address\":{");
            if (data.getAddress().getStreet() != null) {
                json.append("\"street\":\"").append(escapeJsonString(data.getAddress().getStreet())).append("\",");
            }
            if (data.getAddress().getDistrict() != null) {
                json.append("\"district\":\"").append(escapeJsonString(data.getAddress().getDistrict())).append("\",");
            }
            if (data.getAddress().getCity() != null) {
                json.append("\"city\":\"").append(escapeJsonString(data.getAddress().getCity())).append("\",");
            }
            if (data.getAddress().getPostalCode() != null) {
                json.append("\"postalCode\":\"").append(escapeJsonString(data.getAddress().getPostalCode())).append("\",");
            }
            // Son virgülü kaldır
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("},");
        }
        
        // Contact Number
        if (data.getContactNumber() != null) {
            json.append("\"contactNumber\":\"").append(escapeJsonString(data.getContactNumber())).append("\",");
        }
        
        // Working Hours
        if (data.getWorkingHours() != null) {
            json.append("\"workingHours\":\"").append(escapeJsonString(data.getWorkingHours())).append("\",");
        }
        
        // Payment Methods
        if (data.getPaymentMethods() != null && !data.getPaymentMethods().isEmpty()) {
            json.append("\"paymentMethods\":[");
            for (int i = 0; i < data.getPaymentMethods().size(); i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(data.getPaymentMethods().get(i)).append("\"");
            }
            json.append("],");
        }
        
        // Description
        if (data.getDescription() != null) {
            json.append("\"description\":\"").append(escapeJsonString(data.getDescription())).append("\",");
        }
        
        // Active
        json.append("\"active\":").append(data.isActive());
        
        json.append("}");
        return json.toString();
    }

    /**
     * JSON string'i escape eder
     */
    private static String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Belirli bir ödeme noktasını getirir (Token gerektirmez) - ANA METOD
     * GET /payment-point/{id}
     */
    public static String getPaymentPointById(Long id) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        // Token gönderilmiyor

        return executeRequest(conn, "Ödeme noktası alınamadı");
    }

    /**
     * Belirli bir ödeme noktasını getirir (Token ile)
     * GET /payment-point/{id}
     */
    public static String getPaymentPointById(TokenDTO accessToken, Long id) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası alınamadı");
    }

    /**
     * Şehre göre ödeme noktalarını getirir
     * GET /payment-point/by-city/{city}
     */
    public static String getPaymentPointsByCity(TokenDTO accessToken, String city) throws IOException {
        String endpoint = BASE_URL + "/payment-point/by-city/" + city;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Şehre göre ödeme noktaları alınamadı");
    }

    /**
     * Ödeme yöntemine göre ödeme noktalarını getirir
     * GET /payment-point/by-payment-method?paymentMethod={paymentMethod}
     */
    public static String getPaymentPointsByPaymentMethod(TokenDTO accessToken, String paymentMethod) throws IOException {
        String endpoint = BASE_URL + "/payment-point/by-payment-method?paymentMethod=" + paymentMethod;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme yöntemine göre ödeme noktaları alınamadı");
    }

    /**
     * Yakındaki ödeme noktalarını getirir
     * GET /payment-point/nearby?latitude={lat}&longitude={lng}&radiusKm={radius}
     */
    public static String getNearbyPaymentPoints(TokenDTO accessToken, double latitude, double longitude, double radiusKm) throws IOException {
        String endpoint = BASE_URL + "/payment-point/nearby?latitude=" + latitude + 
                         "&longitude=" + longitude + "&radiusKm=" + radiusKm;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Yakındaki ödeme noktaları alınamadı");
    }

    /**
     * Yeni ödeme noktası ekler
     * POST /payment-point
     */
    public static String addPaymentPoint(String paymentPointData, TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON verisini gönder
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = paymentPointData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Ödeme noktası eklenemedi", true);
    }

    /**
     * Ödeme noktasını günceller
     * PUT /payment-point/{id}
     */
    public static String updatePaymentPoint(TokenDTO accessToken, Long id, String paymentPointData) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON verisini gönder
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = paymentPointData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Ödeme noktası güncellenemedi");
    }

    /**
     * Ödeme noktası durumunu değiştirir (aktif/pasif)
     * PATCH /payment-point/{id}/status?active={active}
     */
    public static String togglePaymentPointStatus(TokenDTO accessToken, Long id, boolean active) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id + "/status?active=" + active;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası durumu değiştirilemedi");
    }

    /**
     * Ödeme noktasını siler
     * DELETE /payment-point/{id}
     */
    public static String deletePaymentPoint(TokenDTO accessToken, Long id) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası silinemedi");
    }

    /**
     * Ödeme noktası istatistiklerini getirir
     * GET /payment-point/statistics
     */
    public static String getPaymentPointStatistics(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/statistics";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası istatistikleri alınamadı");
    }

    /**
     * Ödeme noktalarını filtreleyerek getirir
     * GET /payment-point/filter?active={active}&city={city}&paymentMethod={method}
     */
    public static String getFilteredPaymentPoints(TokenDTO accessToken, Boolean active, String city, String paymentMethod) throws IOException {
        StringBuilder endpointBuilder = new StringBuilder(BASE_URL + "/payment-point/filter?");
        
        boolean hasParams = false;
        
        if (active != null) {
            endpointBuilder.append("active=").append(active);
            hasParams = true;
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (hasParams) endpointBuilder.append("&");
            endpointBuilder.append("city=").append(city);
            hasParams = true;
        }
        
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            if (hasParams) endpointBuilder.append("&");
            endpointBuilder.append("paymentMethod=").append(paymentMethod);
        }
        
        String endpoint = endpointBuilder.toString();
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Filtrelenmiş ödeme noktaları alınamadı");
    }

    /**
     * Ödeme noktası raporunu getirir
     * GET /payment-point/report?startDate={startDate}&endDate={endDate}
     */
    public static String getPaymentPointReport(TokenDTO accessToken, String startDate, String endDate) throws IOException {
        String endpoint = BASE_URL + "/payment-point/report?startDate=" + startDate + "&endDate=" + endDate;
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası raporu alınamadı");
    }

    /**
     * Ödeme noktasının kullanım geçmişini getirir
     * GET /payment-point/{id}/usage-history
     */
    public static String getPaymentPointUsageHistory(TokenDTO accessToken, Long id) throws IOException {
        String endpoint = BASE_URL + "/payment-point/" + id + "/usage-history";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme noktası kullanım geçmişi alınamadı");
    }

    /**
     * Ödeme noktası bulk işlemleri (çoklu ekleme)
     * POST /payment-point/bulk
     */
    public static String bulkAddPaymentPoints(TokenDTO accessToken, String paymentPointsData) throws IOException {
        String endpoint = BASE_URL + "/payment-point/bulk";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON verisini gönder
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = paymentPointsData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Toplu ödeme noktası ekleme başarısız", true);
    }

    /**
     * Ödeme noktası bulk güncelleme
     * PUT /payment-point/bulk
     */
    public static String bulkUpdatePaymentPoints(TokenDTO accessToken, String paymentPointsData) throws IOException {
        String endpoint = BASE_URL + "/payment-point/bulk";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());
        conn.setDoOutput(true);

        // JSON verisini gönder
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = paymentPointsData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return executeRequest(conn, "Toplu ödeme noktası güncelleme başarısız");
    }

    /**
     * Ödeme noktalarını şehre göre gruplar
     * GET /payment-point/group-by-city
     */
    public static String groupPaymentPointsByCity(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/group-by-city";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Şehir bazında gruplandırma başarısız");
    }

    /**
     * Ödeme noktalarını ödeme yöntemine göre gruplar
     * GET /payment-point/group-by-payment-method
     */
    public static String groupPaymentPointsByPaymentMethod(TokenDTO accessToken) throws IOException {
        String endpoint = BASE_URL + "/payment-point/group-by-payment-method";
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken.getToken());

        return executeRequest(conn, "Ödeme yöntemi bazında gruplandırma başarısız");
    }

    // =============================================================================
    // YARDIMCI METODLAR (Helper Methods)
    // =============================================================================

    /**
     * URL oluşturur - Java 20+ uyumlu
     */
    private static URL createURL(String endpoint) throws IOException {
        try {
            return new URI(endpoint).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Geçersiz URL formatı: " + endpoint, e);
        }
    }

    /**
     * HTTP isteğini çalıştırır ve yanıtı döndürür
     */
    private static String executeRequest(HttpURLConnection conn, String errorMessage) throws IOException {
        return executeRequest(conn, errorMessage, false);
    }

    /**
     * HTTP isteğini çalıştırır ve yanıtı döndürür
     * @param conn HTTP bağlantısı
     * @param errorMessage Hata durumunda gösterilecek mesaj
     * @param allowCreated 201 Created kodunu da başarılı kabul et
     */
    private static String executeRequest(HttpURLConnection conn, String errorMessage, boolean allowCreated) throws IOException {
        int code = conn.getResponseCode();
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        isSuccessCode(code, allowCreated) ? conn.getInputStream() : conn.getErrorStream(),
                        "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            if (isSuccessCode(code, allowCreated)) {
                return response.toString();
            } else {
                // Hata mesajını API yanıtından çıkarmaya çalış
                String apiErrorMessage = extractErrorMessage(response.toString());
                String finalMessage = apiErrorMessage != null ? apiErrorMessage : errorMessage;
                throw new IOException(finalMessage + " (HTTP " + code + ")");
            }
        }
    }

    /**
     * HTTP kodu başarılı mı kontrol eder
     */
    private static boolean isSuccessCode(int code, boolean allowCreated) {
        return code == 200 || (allowCreated && code == 201);
    }

    /**
     * API yanıtından hata mesajını çıkarır
     */
    private static String extractErrorMessage(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return null;
        }

        // Yaygın hata alanlarını kontrol et
        String[] errorFields = {"message", "error", "errorMessage", "detail", "description"};
        
        for (String field : errorFields) {
            String pattern = "\"" + field + "\":\"";
            int startIndex = jsonResponse.indexOf(pattern);
            if (startIndex != -1) {
                startIndex += pattern.length();
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return jsonResponse.substring(startIndex, endIndex);
                }
            }
        }

        return null;
    }
}
