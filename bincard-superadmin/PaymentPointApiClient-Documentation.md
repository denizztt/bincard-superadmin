# PaymentPointApiClient - API İstemci Ayrıştırması

## Genel Bakış

Bu refactoring işlemi ile tüm ödeme noktası (payment point) API çağrıları `ApiClientFX` sınıfından ayrıştırılarak özel bir `PaymentPointApiClient` sınıfına taşınmıştır. Bu yaklaşım kodun daha modüler, sürdürülebilir ve organize olmasını sağlar.

## Yapılan Değişiklikler

### 1. PaymentPointApiClient Sınıfı Oluşturuldu

#### Temel Özellikler:
- **Modüler Yapı**: Tüm payment point API çağrıları tek bir sınıfta toplanmıştır
- **Java 20+ Uyumlu**: URI.toURL() kullanarak deprecated URL constructor sorunları çözülmüştür
- **Hata Yönetimi**: Tutarlı ve detaylı hata mesajları
- **Flexible API**: Farklı filtreleme ve sorgulama seçenekleri

#### Mevcut API Metodları:

##### Temel CRUD İşlemleri:
- `getAllPaymentPoints()` - Tüm ödeme noktalarını getirir
- `getPaymentPointById(Long id)` - ID'ye göre ödeme noktası getirir
- `addPaymentPoint(String data)` - Yeni ödeme noktası ekler
- `updatePaymentPoint(Long id, String data)` - Ödeme noktasını günceller
- `deletePaymentPoint(Long id)` - Ödeme noktasını siler

##### Filtreleme ve Arama:
- `getPaymentPointsByCity(String city)` - Şehre göre filtreler
- `getPaymentPointsByPaymentMethod(String method)` - Ödeme yöntemine göre filtreler
- `getNearbyPaymentPoints(double lat, double lng, double radius)` - Yakındaki noktaları getirir
- `getFilteredPaymentPoints(Boolean active, String city, String method)` - Çoklu filtreleme

##### Durum Yönetimi:
- `togglePaymentPointStatus(Long id, boolean active)` - Aktif/pasif durumu değiştirir

##### Raporlama ve İstatistikler:
- `getPaymentPointStatistics()` - Genel istatistikleri getirir
- `getPaymentPointReport(String startDate, String endDate)` - Tarih aralığına göre rapor
- `getPaymentPointUsageHistory(Long id)` - Kullanım geçmişini getirir

##### Gruplandırma:
- `groupPaymentPointsByCity()` - Şehirlere göre gruplar
- `groupPaymentPointsByPaymentMethod()` - Ödeme yöntemlerine göre gruplar

##### Bulk İşlemler:
- `bulkAddPaymentPoints(String data)` - Toplu ekleme
- `bulkUpdatePaymentPoints(String data)` - Toplu güncelleme

### 2. ApiClientFX'den Temizleme

Aşağıdaki metodlar `ApiClientFX` sınıfından kaldırılmıştır:
- `getAllPaymentPoints()`
- `getPaymentPointById()`
- `getPaymentPointsByCity()`
- `getPaymentPointsByPaymentMethod()`
- `getNearbyPaymentPoints()`
- `addPaymentPoint()`
- `togglePaymentPointStatus()`
- `deletePaymentPoint()`

### 3. Kullanım Güncellemeleri

#### PaymentPointAddPage:
```java
// Eski kullanım:
String response = ApiClientFX.addPaymentPoint(paymentPointData, accessToken);

// Yeni kullanım:
String response = PaymentPointApiClient.addPaymentPoint(paymentPointData, accessToken);
```

#### PaymentPointsTablePage:
```java
// Eski kullanım:
String response = ApiClientFX.getAllPaymentPoints(accessToken);

// Yeni kullanım:
String response = PaymentPointApiClient.getAllPaymentPoints(accessToken);
```

### 4. Hata Yönetimi İyileştirmeleri

#### JSON Hata Mesajı Çıkarma:
```java
private static String extractErrorMessage(String jsonResponse) {
    String[] errorFields = {"message", "error", "errorMessage", "detail", "description"};
    // API yanıtından en uygun hata mesajını çıkarır
}
```

#### HTTP Durum Kodu Kontrolü:
```java
private static boolean isSuccessCode(int code, boolean allowCreated) {
    return code == 200 || (allowCreated && code == 201);
}
```

## Avantajlar

### 1. **Modülerlik**
- Her API kategorisi için ayrı client sınıfı
- Kod organize ve bulması kolay
- Bakım ve geliştirme kolaylığı

### 2. **Tek Sorumluluk İlkesi**
- Her sınıf sadece kendi alanından sorumlu
- `ApiClientFX` artık sadece genel API işlemleri için kullanılıyor

### 3. **Genişletilebilirlik**
- Yeni payment point API metodları kolayca eklenebilir
- Mevcut kodu bozmadan yeni özellikler eklenebilir

### 4. **Hata Yönetimi**
- Tutarlı hata mesajları
- Daha detaylı hata raporlaması
- API yanıtlarından otomatik hata çıkarma

### 5. **Java 20+ Uyumlu**
- Deprecated URL constructor kullanımı kaldırıldı
- Modern Java standartlarına uygun

## Gelecek Geliştirmeler

### 1. **Diğer API Kategorileri**
- `NewsApiClient` - Haber API'leri için
- `UserApiClient` - Kullanıcı API'leri için
- `AdminApiClient` - Admin API'leri için

### 2. **Async/Reactive Destek**
- CompletableFuture ile asenkron API çağrıları
- Reactive streams desteği

### 3. **Caching Mekanizması**
- API yanıtlarını cache'leme
- Performans optimizasyonu

### 4. **Retry Mekanizması**
- Başarısız API çağrıları için otomatik yeniden deneme
- Exponential backoff stratejisi

## Kullanım Örnekleri

### Basit Ödeme Noktası Getirme:
```java
try {
    String response = PaymentPointApiClient.getAllPaymentPoints(accessToken);
    // Response'u işle
} catch (IOException e) {
    // Hata durumunu handle et
}
```

### Filtrelenmiş Arama:
```java
try {
    String response = PaymentPointApiClient.getFilteredPaymentPoints(
        true,           // Sadece aktif olanlar
        "İstanbul",     // Şehir filtresi
        "CREDIT_CARD"   // Ödeme yöntemi filtresi
    );
    // Response'u işle
} catch (IOException e) {
    // Hata durumunu handle et
}
```

### Yakındaki Noktaları Bulma:
```java
try {
    String response = PaymentPointApiClient.getNearbyPaymentPoints(
        41.0082,    // Latitude
        28.9784,    // Longitude
        5.0         // 5 km radius
    );
    // Response'u işle
} catch (IOException e) {
    // Hata durumunu handle et
}
```

## Sonuç

Bu refactoring işlemi ile proje daha modüler, sürdürülebilir ve ölçeklenebilir bir yapıya kavuşmuştur. Payment point API çağrıları artık tek bir yerden yönetilmekte ve gelecek geliştirmeler için solid bir temel oluşturulmuştur.
