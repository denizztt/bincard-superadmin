# Bincard Superadmin Paneli

Bu proje, Bincard yönetim sisteminin **Superadmin** panelidir. Orijinal admin panelinden ayrılarak, sadece superadmin yetkilerine sahip kullanıcılar için özel olarak tasarlanmış, gelişmiş yetkilere sahip bir yönetim aracıdır. Modern, sade ve kullanıcı dostu arayüzü ile sistem yönetimini kolaylaştırır.

![Bincard Superadmin Panel](https://via.placeholder.com/800x400?text=Bincard+Superadmin+Panel)

## Özellikler

### Kimlik Doğrulama ve Güvenlik
- Superadmin girişi (telefon + şifre)
- SMS ile 6 haneli doğrulama kodu sistemi
- Token tabanlı güvenlik (JWT)
- Şifrelenmiş token saklama
- Otomatik giriş desteği (beni hatırla)
- Backend'den gelen hata mesajlarının kullanıcıya gösterilmesi

### Arayüz ve Tasarım
- Modern ve sade soft renkli arayüz
- Sürekli tam ekran modunda çalışma
- Açılır/kapanır alt menü sistemi
- Her bölümde mantıklı alt başlıklar (Ekle, Sil, Düzenle, Görüntüle)
- Responsive tasarım
- Kullanıcı deneyimi odaklı gezinme

### Yönetim Özellikleri
- **Otobüs Yönetimi**
  - Otobüs Ekle
  - Otobüsleri Görüntüle
  - Otobüs Düzenle
  - Otobüs Sil

- **Şoför Yönetimi**
  - Şoför Ekle
  - Şoförleri Görüntüle
  - Şoför Düzenle
  - Şoför Sil

- **Haber Yönetimi**
  - Haber Ekle
  - Haberleri Görüntüle
  - Haber Düzenle
  - Haber Sil

- **Otobüs Rota Yönetimi**
  - Rota Ekle
  - Rotaları Görüntüle
  - Rota Düzenle
  - Rota Sil

- **Durak Yönetimi**
  - Durak Ekle
  - Durakları Görüntüle
  - Durak Düzenle
  - Durak Sil

- **Kullanıcı Yönetimi**
  - Kullanıcı Ekle
  - Kullanıcıları Görüntüle
  - Kullanıcı Düzenle
  - Kullanıcı Sil

- **Raporlama**
  - Günlük Raporlar
  - Aylık Raporlar
  - Yıllık Raporlar

- **Admin Onayları**
  - Bekleyen Admin Başvurularını Görüntüleme
  - Admin Başvurularını Onaylama/Reddetme

- **İstatistikler**
  - Sistem İstatistikleri Görüntüleme

## Teknik Detaylar
- **Dil ve Framework:** Java ve JavaFX
- **Mimari:** Model-View-Controller (MVC)
- **API İletişimi:** RESTful API (JSON)
- **Gereksinimler:**
  - Java 11 veya üzeri
  - JavaFX kütüphaneleri
  - Maven
  - Backend API bağlantısı

## Kurulum

### Sistem Gereksinimleri
- Java 11 veya üzeri
- Maven 3.6 veya üzeri
- JavaFX kütüphaneleri
- En az 4GB RAM, 1GB boş disk alanı
- Bir backend API (varsayılan: `http://localhost:8080/v1/api`)

### Derleme
Projeyi derlemek için, proje klasöründe şu komutları kullanabilirsiniz:

```sh
# Maven ile derleme
mvn clean package

# Veya hazır script ile (Windows)
.\build.bat
```

### Çalıştırma
```sh
# Maven ile çalıştırma
mvn javafx:run

# JAR dosyasını doğrudan çalıştırma
java -jar target/bincard-superadmin-1.0.jar

# Veya hazır script ile (Windows)
.\run.bat
```

## Proje Yapısı
```
bincard-superadmin/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bincard/
│       │           └── bincard_superadmin/
│       │               ├── SuperadminDashboardFX.java (Ana dashboard)
│       │               ├── SuperadminLoginFX.java (Giriş ekranı)
│       │               ├── SuperadminPageBase.java (Tüm sayfaların temel sınıfı)
│       │               ├── ApiClientFX.java (API istekleri)
│       │               ├── MenuItem.java (Menü modeli)
│       │               ├── BusesPage.java (Otobüsler sayfası)
│       │               ├── DriversPage.java (Şoförler sayfası)
│       │               ├── ...
│       └── resources/
├── pom.xml (Maven yapılandırması)
├── build.bat (Windows için derleme script'i)
├── run.bat (Windows için çalıştırma script'i)
└── README.md
```

## Ortam Değişkenleri
- `API_BASE_URL`: Backend API'nin temel URL'i (varsayılan: http://localhost:8080/v1/api)
- `JAVA_HOME`: Java kurulum dizini

## Geliştirme için Notlar
- Yeni bir sayfa eklemek için `SuperadminPageBase` sınıfını extend edin
- Yeni bir menü öğesi eklemek için `MenuItem` sınıfını kullanın
- Backend API endpoint'leri için `ApiClientFX` sınıfını kullanın
- Token yönetimi `TokenDTO` sınıfı ile yapılır

## Projeyi GitHub'a Yükleme
1. Git başlat: `git init`
2. Dosyaları ekle: `git add .`
3. Commit: `git commit -m "İlk superadmin commit"`
4. GitHub'da repo oluştur, adresi ekle: `git remote add origin <repo-url>`
5. Gönder: `git push -u origin main`

## Sürüm Geçmişi
- **1.0.0** (Temmuz 2025)
  - İlk resmi sürüm
  - Açılır/kapanır alt menü sistemi
  - Soft renkli tasarım ve tam ekran modu
  - Token tabanlı güvenlik ve otomatik giriş
  - Admin onayları sayfası

## Hata Giderme
- **Java Bulunamadı Hatası**: `build.bat` veya `run.bat` dosyasında JAVA_HOME ayarlanmıştır
- **Backend Bağlantı Hataları**: API endpoint'in doğru ayarlandığından emin olun
- **Token Hatası**: Çıkış yapıp tekrar giriş yapın

## Katkı ve Lisans
Bu proje özel bir kurum içindir. Katkı için lütfen proje sahibine ulaşın.

---

**Not:**
- Doğrulama kodu hatası veya backend bağlantı sorunlarında, backend'in döndürdüğü hata mesajı ekranda gösterilir.
- Uygulama varsayılan olarak tam ekran modunda başlar, ESC tuşu ile pencere moduna geçilebilir.
- Tüm sayfalar için soft renkli bir tema kullanılmıştır.
- Alt menüleri açmak için ana menü öğelerine tıklayın.