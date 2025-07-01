# Bincard Admin - JavaFX Uygulaması

Bu proje, Bincard Admin panelinin JavaFX versiyonudur. Modern ve kullanıcı dostu bir arayüz ile admin girişi ve kayıt işlemlerini gerçekleştirir.

## Özellikler

- **Modern UI Tasarımı**: Gradient arka planlar, gölgeler ve yuvarlatılmış köşeler
- **Admin Girişi**: Telefon numarası ve şifre ile giriş
- **Admin Kayıt**: Yeni admin hesabı oluşturma
- **Gerçek Zamanlı API İletişimi**: HTTP istekleri ile backend iletişimi
- **Responsive Tasarım**: Farklı ekran boyutlarına uyumlu
- **Hover Efektleri**: Butonlarda mouse hover animasyonları

## Gereksinimler

- Java 17 veya üzeri
- JavaFX 17.0.6
- Maven 3.6 veya üzeri

## Kurulum

1. Projeyi klonlayın:
```bash
git clone <repository-url>
cd Bincard_Admin.fx
```

2. Maven ile bağımlılıkları yükleyin:
```bash
mvn clean install
```

3. Uygulamayı çalıştırın:
```bash
mvn javafx:run
```

## Kullanım

### Ana Menü
- **Admin Giriş**: Mevcut admin hesabı ile giriş yapın
- **Admin Kayıt**: Yeni admin hesabı oluşturun

### Admin Girişi
1. Telefon numaranızı girin (sadece rakamlar)
2. Şifrenizi girin
3. "Giriş Yap" butonuna tıklayın
4. Başarılı girişte Access Token ve Refresh Token görüntülenir

### Admin Kayıt
1. Telefon numaranızı girin (sadece rakamlar)
2. Şifrenizi girin (en az 6 karakter)
3. Şifrenizi tekrar girin
4. "Kayıt Ol" butonuna tıklayın
5. Başarılı kayıt sonrası onay mesajı görüntülenir

## API Endpoints

- **Login**: `POST /v1/api/auth/login`
- **Signup**: `POST /v1/api/admin/sign-up`

## Dosya Yapısı

```
src/main/java/com/bincard/bincard_admin/
├── HelloApplication.java      # Ana uygulama sınıfı
├── MainMenuFX.java           # Ana menü paneli
├── AdminLoginFX.java         # Giriş paneli
├── AdminSignupFX.java        # Kayıt paneli
├── ApiClientFX.java          # API iletişim sınıfı
└── TokenResponse.java        # Token response modeli
```

## Teknik Detaylar

### UI Bileşenleri
- **VBox**: Dikey düzen için
- **HBox**: Yatay düzen için
- **Button**: Etkileşimli butonlar
- **TextField**: Metin girişi
- **PasswordField**: Şifre girişi
- **TextArea**: Çok satırlı metin alanı

### Stil Özellikleri
- CSS benzeri stil tanımları
- Gradient arka planlar
- Gölge efektleri
- Hover animasyonları
- Responsive tasarım

### API İletişimi
- HTTP POST istekleri
- JSON formatında veri gönderimi
- Asenkron işlemler (Thread kullanımı)
- Platform.runLater() ile UI güncellemeleri

## Geliştirme

### Yeni Özellik Ekleme
1. Yeni JavaFX sınıfı oluşturun
2. UI bileşenlerini tanımlayın
3. Event handler'ları ekleyin
4. API iletişimi için gerekli metodları yazın

### Stil Değişiklikleri
- CSS benzeri stil tanımlarını düzenleyin
- Renk kodlarını değiştirin
- Font boyutlarını ayarlayın

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## İletişim

Sorularınız için: [email@example.com]