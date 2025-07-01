# Bincard Superadmin Paneli

Bu proje, Bincard yönetim sisteminin **Superadmin** panelidir. Orijinal admin panelinden ayrılarak, sadece superadmin yetkilerine sahip kullanıcılar için özel olarak tasarlanmıştır. Giriş ve doğrulama akışı yenilenmiş, modern bir arayüz ve hata yönetimi eklenmiştir.

## Özellikler
- Superadmin girişi (telefon + şifre)
- SMS ile 6 haneli doğrulama kodu
- Backend'den gelen hata mesajlarının kullanıcıya gösterilmesi
- Modern ve sade arayüz
- Admin kayıt olma özelliği kaldırıldı

## Kurulum

### Gereksinimler
- Java 11 veya üzeri
- Maven
- (Varsa) JavaFX kütüphaneleri
- Bir backend API (örnek: `http://localhost:8080/v1/api`)

### Derleme
```sh
mvn clean package
```

### Çalıştırma
```sh
mvn javafx:run
```
veya
```sh
java -jar target/bincard-superadmin-1.0.jar
```

## Projeyi GitHub'a Yükleme
1. Git başlat: `git init`
2. Dosyaları ekle: `git add .`
3. Commit: `git commit -m "İlk superadmin commit"`
4. GitHub'da repo oluştur, adresi ekle: `git remote add origin <repo-url>`
5. Gönder: `git push -u origin main`

## Katkı ve Lisans
Bu proje özel bir kurum içindir. Katkı için lütfen proje sahibine ulaşın.

---

**Not:**
- Doğrulama kodu hatası veya backend bağlantı sorunlarında, backend'in döndürdüğü hata mesajı ekranda gösterilir.
- Arayüzde "admin" geçen tüm metinler "superadmin" olarak değiştirilmiştir.
