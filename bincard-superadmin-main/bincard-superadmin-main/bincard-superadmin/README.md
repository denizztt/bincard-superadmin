# Bincard Superadmin Paneli

Bu proje, Bincard yönetim sisteminin **Superadmin** panelidir. Orijinal admin panelinden ayrılarak, sadece superadmin yetkilerine sahip kullanıcılar için özel olarak tasarlanmış, gelişmiş yetkilere sahip bir yönetim aracıdır. Modern, sade ve kullanıcı dostu arayüzü ile sistem yönetimini kolaylaştırır.

![Bincard Superadmin Panel](https://via.placeholder.com/800x400?text=Bincard+Superadmin+Panel)

## ✨ Son Güncellemeler

### 🔌 Backend API Entegrasyonu (Temmuz 2025)
- **Gelir Raporları**: Günlük, haftalık, aylık gelir API'leri entegre edildi
- **Denetim Kayıtları**: Audit logs sayfası ve filtreleme özellikleri eklendi
- **Admin Onayları**: Gerçek API ile onaylama/reddetme işlemleri
- **Dashboard İyileştirmeleri**: Canlı gelir kartları ve real-time veriler
- **Yeni Sayfalar**: IncomeReportsPage ve AuditLogsPage eklendi

### 🎨 UI/UX İyileştirmeleri (Temmuz 2025)
- **Header Optimizasyonu**: Dashboard ve alt sayfalardaki gereksiz UI elemanları kaldırıldı
- **Navigasyon İyileştirmeleri**: Geri dön butonu ikon olarak (←) değiştirildi
- **Bildirim Sistemi**: Dashboard'da çan ikonu ile bildirim butonu eklendi
- **Layout Düzenlemeleri**: Header yapısı spacer ile daha düzenli hizalandı
- **Modern Hover Efektleri**: Butonlara gelişmiş animasyonlar eklendi

## 🔥 Özellikler

### 🔐 Kimlik Doğrulama ve Güvenlik
- Superadmin girişi (telefon + şifre)
- SMS ile 6 haneli doğrulama kodu sistemi
- Token tabanlı güvenlik (JWT)
- Şifrelenmiş token saklama
- Otomatik giriş desteği (beni hatırla)
- Backend'den gelen hata mesajlarının kullanıcıya gösterilmesi

### 🎨 Arayüz ve Tasarım
- **Modern ve Temiz Header**: Gereksiz öğeler kaldırıldı
- **Dashboard**: Sol üstte "Ana Sayfa" başlığı, sağ üstte bildirim butonu
- **Alt Sayfalar**: Sol üstte ← geri dön ikonu, temiz layout
- **Responsive Design**: Tüm ekran boyutlarına uyumlu
- **Sürekli Tam Ekran**: Tam ekran modunda optimized çalışma
- **Açılır/Kapanır Menü**: Alt menü sistemi
- **Soft Renkli Tema**: Göze rahatlık sağlayan renk paleti

### 📊 Yönetim Özellikleri
- **Otobüs Yönetimi**
  - Otobüs Ekle/Sil/Düzenle/Görüntüle
  - Plaka bazlı arama ve filtreleme

- **Şoför Yönetimi**
  - Şoför Ekle/Sil/Düzenle/Görüntüle
  - Ad ve telefon bazlı arama

- **📰 Haber Yönetimi**
  - Haber Ekle/Sil/Düzenle/Görüntüle
  - Görsel upload desteği
  - Platform bazlı filtreleme
  - Tarih aralığı belirleme
  - Geri bildirim sistemi

- **🚌 Otobüs Rota Yönetimi**
  - Rota Ekle/Sil/Düzenle/Görüntüle
  - Durak bazlı rota planlama

- **🚏 Durak Yönetimi**
  - Durak Ekle/Sil/Düzenle/Görüntüle
  - Konum bazlı yönetim

- **👥 Kullanıcı Yönetimi**
  - Kullanıcı Ekle/Sil/Düzenle/Görüntüle
  - Yetki seviyesi belirleme

- **📈 Raporlama**
  - Günlük/Aylık/Yıllık Raporlar
  - İstatistiksel veriler

- **✅ Admin Onayları**
  - Bekleyen admin başvurularını görüntüleme
  - Onaylama/Reddetme işlemleri (Gerçek API entegreli)
  - Real-time durum güncellemeleri
  - JSON parse ve hata yönetimi

- **💰 Gelir Raporları**
  - Günlük/Haftalık/Aylık gelir analizi
  - Gelir dağılım grafikları (Pasta ve Çizgi)
  - Real-time API veri çekme
  - Dashboard'da canlı gelir kartları

- **📋 Denetim Kayıtları (Audit Logs)**
  - Tüm sistem aktivitelerini görüntüleme
  - Tarih aralığı ve aksiyon filtreleme
  - Renk kodlu aktivite türleri
  - IP adresi ve detaylı log bilgileri

- **📊 İstatistikler**
  - Dashboard ana sayfa kartları
  - Sistem geneli istatistikler
## 🛠️ Teknik Detaylar
- **Dil ve Framework:** Java 21+ ve JavaFX
- **Mimari:** Model-View-Controller (MVC)
- **API İletişimi:** RESTful API (JSON)
- **UI Kütüphaneleri:** 
  - FontAwesome 5 ikonları
  - Kordamp ikonli desteği
  - Modern CSS styling
- **Güvenlik:** 
  - JWT token authentication
  - Şifrelenmiş token storage
  - Secure API communication
- **Gereksinimler:**
  - Java 21 veya üzeri
  - JavaFX 21+ kütüphaneleri
  - Maven 3.8+
  - Backend API bağlantısı

## 📦 Kurulum

### Sistem Gereksinimleri
- ☑️ Java 21 veya üzeri
- ☑️ Maven 3.8 veya üzeri
- ☑️ JavaFX kütüphaneleri (otomatik indirilir)
- ☑️ En az 4GB RAM, 1GB boş disk alanı
- ☑️ Backend API erişimi (varsayılan: `http://localhost:8080/v1/api`)

### 🔧 Derleme
Projeyi derlemek için, proje klasöründe şu komutları kullanabilirsiniz:

```bash
# Maven ile derleme
mvn clean compile

# Tam paket oluşturma
mvn clean package

# Veya hazır script ile (Windows)
.\build.bat
```

### 🚀 Çalıştırma
```bash
# Maven ile çalıştırma
mvn javafx:run

# JAR dosyasını doğrudan çalıştırma
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/bincard-superadmin-1.0.jar

# Veya hazır script ile (Windows)
.\run.bat
```

## 📂 Proje Yapısı
```
bincard-superadmin/
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java (Java modül tanımları)
│       │   └── com/
│       │       └── bincard/
│       │           └── bincard_superadmin/
│       │               ├── 🏠 SuperadminDashboardFX.java (Ana dashboard)
│       │               ├── 🔐 SuperadminLoginFX.java (Giriş ekranı)
│       │               ├── 📄 SuperadminPageBase.java (Temel sayfa sınıfı)
│       │               ├── 🌐 ApiClientFX.java (API client)
│       │               ├── 🔧 TokenSecureStorage.java (Token yönetimi)
│       │               ├── 📊 MenuItem.java (Menü modeli)
│       │               ├── 🚌 BusesPage.java (Otobüsler)
│       │               ├── 👤 DriversPage.java (Şoförler)
│       │               ├── 📰 NewsPage.java (Haberler)
│       │               ├── ✅ AdminApprovalsPage.java (Admin onayları)
│       │               ├── 💰 IncomeReportsPage.java (Gelir raporları)
│       │               ├── 📋 AuditLogsPage.java (Denetim kayıtları)
│       │               └── ... (Diğer sayfalar)
│       └── resources/
├── target/ (Derleme çıktıları)
├── pom.xml (Maven yapılandırması)
├── build.bat (Windows derleme script'i)
├── run.bat (Windows çalıştırma script'i)
├── mvnw, mvnw.cmd (Maven wrapper)
└── README.md
```

## ⚙️ Yapılandırma

### Ortam Değişkenleri
```bash
# API temel URL'i
API_BASE_URL=http://localhost:8080/v1/api

# Java kurulum dizini
JAVA_HOME=/path/to/java

# JavaFX modül yolu (isteğe bağlı)
PATH_TO_FX=/path/to/javafx/lib
```

### API Endpoint'leri
```java
// Kimlik doğrulama
POST /auth/superadmin-login
POST /auth/phone-verify
POST /auth/refresh

// Admin yönetimi (SuperAdmin Controller)
GET /v1/api/superadmin/admin-requests/pending
POST /v1/api/superadmin/admin-requests/{adminId}/approve
POST /v1/api/superadmin/admin-requests/{adminId}/reject

// Gelir raporları
GET /v1/api/superadmin/income-summary
GET /v1/api/superadmin/bus-income/daily
GET /v1/api/superadmin/bus-income/weekly
GET /v1/api/superadmin/bus-income/monthly

// Denetim kayıtları
GET /v1/api/superadmin/audit-logs

// Haber yönetimi
GET /news/
POST /news/create
PUT /news/update
PUT /news/{id}/soft-delete
```

## 🔧 Geliştirme Notları

### Yeni Sayfa Ekleme
1. `SuperadminPageBase` sınıfını extend edin
2. `createContent()` metodunu implement edin
3. `SuperadminDashboardFX`'de navigasyona ekleyin

```java
public class YeniSayfa extends SuperadminPageBase {
    public YeniSayfa(Stage stage, TokenDTO accessToken, TokenDTO refreshToken) {
        super(stage, accessToken, refreshToken, "Yeni Sayfa");
    }
    
    @Override
    protected Node createContent() {
        // Sayfa içeriğini burada oluşturun
        return new VBox();
    }
}
```

### Yeni API Endpoint Ekleme
`ApiClientFX` sınıfına yeni metodlar ekleyin:

```java
public static String yeniApiCagrisi(TokenDTO accessToken, String parametre) throws IOException {
    URL url = new URL(BASE_URL + "/endpoint");
    // API çağrısını implement edin
}
```
## 🐛 Hata Giderme

### Yaygın Sorunlar ve Çözümleri

#### Java/Maven Hataları
```bash
# Java bulunamadı hatası
Error: JAVA_HOME not found

# Çözüm: JAVA_HOME'u ayarlayın
export JAVA_HOME=/path/to/java  # Linux/Mac
set JAVA_HOME=C:\Program Files\Java\jdk-21  # Windows
```

#### API Bağlantı Sorunları
```bash
# Backend bağlantı hatası
IOException: Connection refused

# Çözüm kontrol listesi:
✅ Backend API çalışıyor mu?
✅ URL doğru mu? (http://localhost:8080/v1/api)
✅ Firewall/antivirus engelliyor mu?
✅ Port 8080 kullanımda mı?
```

#### Token/Kimlik Doğrulama Hataları
```bash
# Token süresi dolmuş
TokenExpiredException

# Çözüm:
1. Çıkış yapın (Logout)
2. Tekrar giriş yapın
3. Veya uygulamayı yeniden başlatın
```

#### UI/Görüntü Sorunları
```bash
# Tam ekran problemi
# Çözüm: ESC tuşuna basın

# Font/İkon yüklenmeme
# Çözüm: İnternet bağlantısını kontrol edin

# 3 nokta simgesi görünüyor
# Bu sorun güncel sürümde düzeltilmiştir
```

## 🔄 Sürüm Geçmişi

### v1.3.0 (Temmuz 2025)
🔌 **Backend API Entegrasyonu**
- Gelir raporları sayfası ve API entegrasyonu
- Denetim kayıtları (audit logs) sayfası eklendi
- Admin onayları için gerçek API bağlantıları
- Dashboard'da canlı gelir verileri
- SuperAdminController endpoint'leri entegre edildi
- Asenkron veri çekme ve JSON parse işlemleri

### v1.2.0 (Temmuz 2025)
🎨 **UI/UX İyileştirmeleri**
- Header optimizasyonu ve gereksiz UI elemanları kaldırıldı
- Geri dön butonu ikon olarak (←) değiştirildi
- Dashboard'da bildirim (çan) butonu eklendi
- Layout spacer ile düzenli hizalandı
- Modern hover efektleri ve animasyonlar

### v1.1.0 (Haziran 2025)
📰 **Haber Yönetimi Genişletildi**
- Multipart form data ile görsel upload
- Platform bazlı filtreleme
- Tarih aralığı belirleme
- Soft delete işlemleri

### v1.0.0 (Mayıs 2025)
🚀 **İlk Resmi Sürüm**
- Açılır/kapanır alt menü sistemi
- Soft renkli tasarım ve tam ekran modu
- Token tabanlı güvenlik ve otomatik giriş
- Admin onayları sayfası
- Temel CRUD işlemleri

## 🤝 Katkı Sağlama

Bu proje özel bir kurum için geliştirilmiştir. Katkı sağlamak için:

1. 🍴 Fork edin
2. 🌟 Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. ✅ Değişikliklerinizi commit edin (`git commit -m 'feat: yeni özellik eklendi'`)
4. 📤 Branch'i push edin (`git push origin feature/yeni-ozellik`)
5. 🔄 Pull Request oluşturun

### Commit Mesaj Formatı
```
<type>(<scope>): <description>

feat(ui): header optimizasyonu ve navigasyon iyileştirmeleri
fix(api): token yenileme sorunu düzeltildi
docs(readme): kurulum kılavuzu güncellendi
```

## 📄 Lisans

Bu proje özel/kurumsal kullanım içindir. Daha fazla bilgi için proje sahibi ile iletişime geçin.

---

## 💡 Önemli Notlar

⚠️ **Güvenlik**: 
- Token'ları güvenli bir şekilde saklayın
- Superadmin şifrelerini düzenli olarak değiştirin
- API endpoint'lerini secure bağlantılar üzerinden kullanın

🔧 **Performans**:
- Uygulama varsayılan olarak tam ekran modunda başlar
- ESC tuşu ile pencere moduna geçilebilir
- Tüm sayfalar lazy loading ile optimize edilmiştir

🎨 **UI/UX**:
- Tüm sayfalar için soft renkli tema kullanılır
- Alt menüleri açmak için ana menü öğelerine tıklayın
- Hover efektleri ve animasyonlar UX'i geliştirir
- Header'lar temiz ve minimal tasarımla optimize edilmiştir

📱 **Responsive**:
- Tüm ekran boyutlarına uyumlu
- Minimum 1200x800 çözünürlük önerilir
- Tam ekran modunda optimum deneyim

---

**🌟 Son güncelleme: Temmuz 2025 - v1.3.0**