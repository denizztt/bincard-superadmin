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
        
        System.out.println("\n🌐 API İSTEĞİ GÖNDERİLİYOR:");
        System.out.println("   📍 Endpoint: " + endpoint);
        System.out.println("   📄 Sayfa: " + page + " (0-tabanlı, backend formatı)");
        System.out.println("   📊 Boyut: " + size + " kayıt per sayfa");
        System.out.println("   🔢 Sıralama: " + sort);
        System.out.println("   🎯 Hedef: http://localhost:8080/v1/api/payment-point?page=" + page);
        
        URL url = createURL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        // Token gönderilmiyor - postman testine göre gerekli değil

        String response = executeRequest(conn, "Sayfalı ödeme noktaları alınamadı");
        
        System.out.println("✅ API YANITI ALINDI:");
        System.out.println("   📋 Yanıt uzunluğu: " + response.length() + " karakter");
        
        // Veriyi düzenli formatta yazdır
        printFormattedResponse(page, response);
        
        return response;
    }
    
    /**
     * API yanıtını düzenli formatta yazdırır
     */
    private static void printFormattedResponse(int page, String response) {
        try {
            System.out.println("\n📊 ====== SAYFA " + page + " VERİ ANALİZİ ======");
            
            if (response == null || response.trim().isEmpty()) {
                System.out.println("❌ Boş yanıt alındı!");
                return;
            }
            
            // Sayfalama bilgilerini çıkar
            int totalElements = extractIntFromJson(response, "totalElements");
            int totalPages = extractIntFromJson(response, "totalPages");
            int pageNumber = extractIntFromJson(response, "pageNumber");
            int pageSize = extractIntFromJson(response, "pageSize");
            boolean first = response.contains("\"first\":true");
            boolean last = response.contains("\"last\":true");
            
            System.out.println("📈 SAYFALAMA BİLGİLERİ:");
            System.out.println("   🔢 Toplam Kayıt: " + totalElements);
            System.out.println("   📄 Toplam Sayfa: " + totalPages);
            System.out.println("   📍 Mevcut Sayfa: " + pageNumber + " (0-tabanlı)");
            System.out.println("   📊 Sayfa Boyutu: " + pageSize);
            System.out.println("   ⏮️ İlk Sayfa mı: " + (first ? "✅ Evet" : "❌ Hayır"));
            System.out.println("   ⏭️ Son Sayfa mı: " + (last ? "✅ Evet" : "❌ Hayır"));
            
            // Content array'ini bul ve kayıt sayısını say
            int recordCount = 0;
            if (response.contains("\"content\":[")) {
                String contentStart = "\"content\":[";
                int startIndex = response.indexOf(contentStart);
                if (startIndex != -1) {
                    int endIndex = findArrayEnd(response, startIndex + contentStart.length());
                    if (endIndex != -1) {
                        String contentArray = response.substring(startIndex + contentStart.length(), endIndex);
                        // Basit object sayma - her { için bir kayıt
                        recordCount = countJsonObjects(contentArray);
                    }
                }
            }
            
            System.out.println("\n📋 SAYFA İÇERİĞİ:");
            System.out.println("   🎯 Bu sayfadaki kayıt: " + recordCount);
            System.out.println("   💾 Beklenen kayıt: " + Math.min(pageSize, Math.max(0, totalElements - (pageNumber * pageSize))));
            
            // Sayfa navigasyon durumu
            System.out.println("\n🧭 NAVİGASYON DURUMU:");
            System.out.println("   ◀️ Önceki sayfa (" + Math.max(0, pageNumber - 1) + "): " + (!first ? "✅ Mevcut" : "❌ Yok"));
            System.out.println("   ▶️ Sonraki sayfa (" + (pageNumber + 1) + "): " + (!last ? "✅ Mevcut" : "❌ Yok"));
            
            // API format doğrulama
            System.out.println("\n🔍 API FORMAT DOĞRULAMA:");
            if (response.contains("\"success\":true")) {
                System.out.println("   ✅ Standard API format: {\"success\": true, \"data\": {...}}");
            } else if (response.startsWith("[") && response.endsWith("]")) {
                System.out.println("   ⚠️ Direkt array format: [...]");
            } else if (response.contains("\"content\":[")) {
                System.out.println("   ✅ Spring Page format: {\"content\": [...], \"totalElements\": ...}");
            } else {
                System.out.println("   ❌ Tanınmayan format!");
            }
            
            System.out.println("📊 ====== SAYFA " + page + " ANALİZ BİTTİ ======\n");
            
        } catch (Exception e) {
            System.err.println("❌ Veri analizi hatası: " + e.getMessage());
        }
    }
    
    /**
     * JSON array'inin sonunu bulur
     */
    private static int findArrayEnd(String json, int startIndex) {
        int bracketCount = 0;
        boolean inString = false;
        boolean escapeNext = false;
        
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escapeNext) {
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                escapeNext = true;
                continue;
            }
            
            if (c == '"' && !escapeNext) {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    if (bracketCount == 0) {
                        return i;
                    }
                    bracketCount--;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * JSON array'indeki object sayısını sayar
     */
    private static int countJsonObjects(String jsonArray) {
        int objectCount = 0;
        int braceCount = 0;
        boolean inString = false;
        boolean escapeNext = false;
        
        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            
            if (escapeNext) {
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                escapeNext = true;
                continue;
            }
            
            if (c == '"' && !escapeNext) {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '{') {
                    if (braceCount == 0) {
                        objectCount++; // Yeni object başlıyor
                    }
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
            }
        }
        
        return objectCount;
    }
    
    /**
     * JSON'dan integer değer çıkarır
     */
    private static int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            // Hata durumunda 0 döndür
        }
        return 0;
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
        
        System.out.println("\n🔧 YENİ ÖDEME NOKTASI OLUŞTURMA:");
        System.out.println("   📍 Endpoint: " + endpoint);
        System.out.println("   🎯 JSON Body: " + jsonBody);
        System.out.println("   🔑 Token: " + accessToken.getToken().substring(0, 20) + "...");
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Response kodunu kontrol et
        int responseCode = conn.getResponseCode();
        System.out.println("   📊 Response Code: " + responseCode);
        
        if (responseCode >= 400) {
            // Error response'u oku
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            System.err.println("   ❌ Error Response: " + errorResponse.toString());
            throw new IOException("HTTP " + responseCode + ": " + errorResponse.toString());
        }

        return executeRequest(conn, "Ödeme noktası eklenemedi");
    }

    /**
     * PaymentPointUpdateDTO'dan JSON string oluşturur
     */
    private static String createPaymentPointUpdateJson(PaymentPointUpdateDTO data) {
        System.out.println("🔨 JSON OLUŞTURULMAYA BAŞLANIYOR...");
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Name
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
            json.append("\"name\":\"").append(escapeJsonString(data.getName())).append("\",");
            System.out.println("   - Name eklendi: " + data.getName());
        }
        
        // Location
        if (data.getLocation() != null) {
            System.out.println("   - Location var, latitude: " + data.getLocation().getLatitude() + ", longitude: " + data.getLocation().getLongitude());
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
        } else {
            System.out.println("   - Location null");
        }
        
        // Address
        if (data.getAddress() != null) {
            System.out.println("   - Address var: " + data.getAddress().getStreet() + ", " + data.getAddress().getCity());
            json.append("\"address\":{");
            if (data.getAddress().getStreet() != null && !data.getAddress().getStreet().trim().isEmpty()) {
                json.append("\"street\":\"").append(escapeJsonString(data.getAddress().getStreet())).append("\",");
            }
            if (data.getAddress().getDistrict() != null && !data.getAddress().getDistrict().trim().isEmpty()) {
                json.append("\"district\":\"").append(escapeJsonString(data.getAddress().getDistrict())).append("\",");
            }
            if (data.getAddress().getCity() != null && !data.getAddress().getCity().trim().isEmpty()) {
                json.append("\"city\":\"").append(escapeJsonString(data.getAddress().getCity())).append("\",");
            }
            if (data.getAddress().getPostalCode() != null && !data.getAddress().getPostalCode().trim().isEmpty()) {
                json.append("\"postalCode\":\"").append(escapeJsonString(data.getAddress().getPostalCode())).append("\",");
            }
            // Son virgülü kaldır
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("},");
        } else {
            System.out.println("   - Address null");
        }
        
        // Contact Number
        if (data.getContactNumber() != null && !data.getContactNumber().trim().isEmpty()) {
            json.append("\"contactNumber\":\"").append(escapeJsonString(data.getContactNumber())).append("\",");
            System.out.println("   - Contact Number eklendi: " + data.getContactNumber());
        }
        
        // Working Hours
        if (data.getWorkingHours() != null && !data.getWorkingHours().trim().isEmpty()) {
            json.append("\"workingHours\":\"").append(escapeJsonString(data.getWorkingHours())).append("\",");
            System.out.println("   - Working Hours eklendi: " + data.getWorkingHours());
        }
        
        // Payment Methods
        if (data.getPaymentMethods() != null && !data.getPaymentMethods().isEmpty()) {
            json.append("\"paymentMethods\":[");
            for (int i = 0; i < data.getPaymentMethods().size(); i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(data.getPaymentMethods().get(i)).append("\"");
            }
            json.append("],");
            System.out.println("   - Payment Methods eklendi: " + data.getPaymentMethods());
        }
        
        // Description
        if (data.getDescription() != null && !data.getDescription().trim().isEmpty()) {
            json.append("\"description\":\"").append(escapeJsonString(data.getDescription())).append("\",");
            System.out.println("   - Description eklendi: " + data.getDescription());
        }
        
        // Active
        json.append("\"active\":").append(data.isActive());
        System.out.println("   - Active eklendi: " + data.isActive());
        
        json.append("}");
        
        String result = json.toString();
        System.out.println("🔨 JSON OLUŞTURULDU: " + result);
        return result;
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
