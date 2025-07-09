package com.bincard.bincard_superadmin.model;

/**
 * API'den gelen denetim kayıtları için aksiyon tipleri
 */
public enum ActionType {

    // 🔐 GİRİŞ / GÜVENLİK
    LOGIN,
    LOGOUT,
    RESET_PASSWORD,
    CHANGE_PASSWORD,
    VERIFICATION_SMS_SENT,
    PHONE_VERIFIED,
    EMAIL_VERIFIED,

    // 👤 KULLANICI HESABI
    SIGN_UP,
    COLLECTIVE_SIGN_UP,
    UPDATE_PROFILE,
    DELETE_USER,
    DEACTIVATE_ACCOUNT,
    ACTIVATE_ACCOUNT,
    UPLOAD_PROFILE_PHOTO,

    // 🛡️ YETKİLENDİRME / ADMIN
    APPROVE_ADMIN,
    BLOCK_USER,
    UNBLOCK_USER,
    PROMOTE_TO_ADMIN,
    DEMOTE_TO_USER,

    // 🚌 KART İŞLEMLERİ
    ADD_BUS_CARD,
    DELETE_BUS_CARD,
    UPDATE_BUS_CARD_ALIAS,
    FAVORITE_CARD_ADDED,
    FAVORITE_CARD_REMOVED,
    SET_LOW_BALANCE_ALERT,
    REMOVE_LOW_BALANCE_ALERT,
    BUS_CARD_TOP_UP,
    BUS_CARD_TRANSFER,

    // 🚏 ROTA VE GÜZERGAH
    FAVORITE_ROUTE_ADDED,
    FAVORITE_ROUTE_REMOVED,
    VIEW_ROUTE_DETAILS,

    // 🧭 KONUM / GEO
    LOCATION_UPDATED,
    ADD_GEO_ALERT,
    DELETE_GEO_ALERT,
    TRIGGERED_GEO_ALERT,

    // 👛 CÜZDAN VE ÖDEME
    CREATE_WALLET,
    DELETE_WALLET,
    WALLET_TOP_UP,
    AUTO_TOP_UP_ENABLED,
    AUTO_TOP_UP_DISABLED,
    AUTO_TOP_UP_CONFIG_ADDED,
    AUTO_TOP_UP_CONFIG_REMOVED,
    WALLET_TRANSFER,

    // 📢 BİLDİRİM VE TERCİHLER
    UPDATE_NOTIFICATION_PREFERENCES,
    NOTIFICATION_RECEIVED,
    NOTIFICATION_READ,

    // 📰 HABERLER
    VIEWED_NEWS,
    LIKED_NEWS,
    UNLIKED_NEWS,

    // 🔍 ARAMA VE GEÇMİŞ
    SEARCH_PERFORMED,
    SEARCH_HISTORY_CLEARED,

    // 🧪 DOĞRULAMA KODLARI
    SEND_PHONE_VERIFICATION_CODE,
    RESEND_PHONE_VERIFICATION_CODE,
    VERIFY_PHONE_CODE,

    // ⚙️ SİSTEM / GENEL
    SYSTEM_MAINTENANCE_START,
    SYSTEM_MAINTENANCE_END,
    SYSTEM_ALERT_ACKNOWLEDGED,

    // 📊 RAPOR VE ANALİZ
    EXPORT_USER_DATA,
    EXPORT_WALLET_HISTORY,
    EXPORT_LOGIN_HISTORY;
    
    /**
     * Enum değerini daha okunabilir formata dönüştürür
     * @return Kullanıcı dostu aksiyon ismi
     */
    public String getDisplayName() {
        // Enum değerini boşluklarla ayrılmış kelimeler haline getir
        String name = this.name().replace("_", " ");
        
        // Aksiyon kategorilerini görsel olarak ayırt etmek için ikon ekle
        switch (this) {
            // Giriş/Güvenlik
            case LOGIN: return "🔐 Giriş";
            case LOGOUT: return "🔐 Çıkış";
            case RESET_PASSWORD: return "🔐 Şifre Sıfırlama";
            case CHANGE_PASSWORD: return "🔐 Şifre Değiştirme";
            
            // Kullanıcı Hesabı
            case SIGN_UP: return "👤 Kayıt Olma";
            case UPDATE_PROFILE: return "👤 Profil Güncelleme";
            case DELETE_USER: return "👤 Kullanıcı Silme";
            
            // Yetkilendirme/Admin
            case APPROVE_ADMIN: return "🛡️ Admin Onaylama";
            case BLOCK_USER: return "🛡️ Kullanıcı Engelleme";
            case UNBLOCK_USER: return "🛡️ Kullanıcı Engelini Kaldırma";
            case PROMOTE_TO_ADMIN: return "🛡️ Admin Yetkilendirme";
            
            // Kart İşlemleri
            case ADD_BUS_CARD: return "🚌 Kart Ekleme";
            case DELETE_BUS_CARD: return "🚌 Kart Silme";
            case BUS_CARD_TOP_UP: return "🚌 Kart Yükleme";
            
            // Cüzdan ve Ödeme
            case WALLET_TOP_UP: return "👛 Cüzdan Yükleme";
            case WALLET_TRANSFER: return "👛 Cüzdan Transferi";
            
            // Haberler
            case VIEWED_NEWS: return "📰 Haber Görüntüleme";
            
            // Raporlama
            case EXPORT_USER_DATA: return "📊 Kullanıcı Verisi Dışa Aktarma";
            
            // Genel durum için varsayılan format
            default: return name;
        }
    }
    
    /**
     * ActionType enum değerini string'den elde eder, bulamazsa null döner
     */
    public static ActionType fromString(String value) {
        if (value == null) return null;
        
        try {
            return ActionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
