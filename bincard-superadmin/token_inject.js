// Token injection script - JavaFX tarafından oluşturulan dinamik token dosyası
// Bu dosya JavaFX uygulaması tarafından güncellenecek

console.log('🔑 Token injection script yüklendi');

// Global token değişkeni
let token = 'dummy-token'; // Varsayılan değer

// JavaFX tarafından güncellenecek token
// Bu kod JavaFX tarafından dinamik olarak oluşturulacak
try {
    // Firebase service account ile ilgili bilgiler
    window.FIREBASE_CONFIG = {
        projectId: 'bincard-9a335',
        apiKey: 'AIzaSyBRYfrvFsxgARSM_iE7JA1EHu1nSpaWAxc',
        serviceAccount: 'firebase-adminsdk-fbsvc@bincard-9a335.iam.gserviceaccount.com'
    };
    
    // Token localStorage'da varsa onu kullan
    const storedToken = localStorage.getItem('bincard_auth_token');
    if (storedToken && storedToken !== 'null' && storedToken !== 'undefined') {
        token = storedToken;
        console.log('✅ Token localStorage\'dan alındı');
    } else {
        console.warn('⚠️ localStorage\'da token bulunamadı, varsayılan token kullanılıyor');
    }
    
    // Token'ı global olarak kullanılabilir yap
    window.authToken = token;
    
    console.log('🔐 Aktif token:', token.substring(0, 20) + '...');
    
} catch (error) {
    console.error('❌ Token injection hatası:', error);
    window.authToken = 'dummy-token';
}

// Backend API base URL
window.API_BASE_URL = 'http://localhost:8080/v1/api';

// Google Maps API key (Firebase project'ten)
window.GOOGLE_MAPS_API_KEY = 'AIzaSyBRYfrvFsxgARSM_iE7JA1EHu1nSpaWAxc';

console.log('📡 API Base URL:', window.API_BASE_URL);
console.log('🗺️ Google Maps API Key hazır');
