package com.bincard.bincard_superadmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Bu sınıf, token bilgilerini güvenli bir şekilde şifreli olarak saklamak için kullanılır.
 */
public class TokenSecureStorage {
    private static final String STORAGE_FILE = System.getProperty("user.home") + File.separator + ".bincard" + File.separator + "tokens.properties";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String SALT_FILE = System.getProperty("user.home") + File.separator + ".bincard" + File.separator + ".salt";
    private static final String IV_FILE = System.getProperty("user.home") + File.separator + ".bincard" + File.separator + ".iv";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ACCESS_EXPIRY = "access_expiry";
    private static final String KEY_REFRESH_EXPIRY = "refresh_expiry";
    
    /**
     * Token'ları şifreleyerek saklar
     * 
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @throws Exception Şifreleme veya dosya işlemleri sırasında bir hata oluşursa
     */
    public static void storeTokens(TokenDTO accessToken, TokenDTO refreshToken) throws Exception {
        // Dizin yoksa oluştur
        File directory = new File(System.getProperty("user.home") + File.separator + ".bincard");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Salt ve IV oluştur veya oku
        byte[] salt = getSaltBytes();
        byte[] iv = getIvBytes();
        
        // Şifreleme anahtarını oluştur
        SecretKey key = generateKey("BincardSuperadmin", salt);
        
        // Token'ları şifrele
        String encryptedAccessToken = encrypt(accessToken.getToken(), key, iv);
        String encryptedRefreshToken = encrypt(refreshToken.getToken(), key, iv);
        String accessExpiryStr = accessToken.getExpiresAt().toString();
        String refreshExpiryStr = refreshToken.getExpiresAt().toString();
        
        // Dosyaya kaydet
        Properties properties = new Properties();
        properties.setProperty(KEY_ACCESS_TOKEN, encryptedAccessToken);
        properties.setProperty(KEY_REFRESH_TOKEN, encryptedRefreshToken);
        properties.setProperty(KEY_ACCESS_EXPIRY, accessExpiryStr);
        properties.setProperty(KEY_REFRESH_EXPIRY, refreshExpiryStr);
        
        try (FileOutputStream fos = new FileOutputStream(STORAGE_FILE)) {
            properties.store(fos, "Bincard Token Storage");
            System.out.println("Token'lar güvenli bir şekilde saklandı: " + STORAGE_FILE);
        }
    }
    
    /**
     * Şifrelenmiş token'ları okur ve çözer
     * 
     * @return Token'ları içeren bir TokenPair nesnesi, token'lar mevcut değilse null
     * @throws Exception Çözümleme veya dosya işlemleri sırasında bir hata oluşursa
     */
    public static TokenPair retrieveTokens() throws Exception {
        File file = new File(STORAGE_FILE);
        if (!file.exists()) {
            return null;
        }
        
        // Salt ve IV oku
        byte[] salt = getSaltBytes();
        byte[] iv = getIvBytes();
        
        // Şifreleme anahtarını oluştur
        SecretKey key = generateKey("BincardSuperadmin", salt);
        
        // Dosyadan oku
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(STORAGE_FILE)) {
            properties.load(fis);
        }
        
        // Değerleri al
        String encryptedAccessToken = properties.getProperty(KEY_ACCESS_TOKEN);
        String encryptedRefreshToken = properties.getProperty(KEY_REFRESH_TOKEN);
        String accessExpiryStr = properties.getProperty(KEY_ACCESS_EXPIRY);
        String refreshExpiryStr = properties.getProperty(KEY_REFRESH_EXPIRY);
        
        if (encryptedAccessToken == null || encryptedRefreshToken == null) {
            return null;
        }
        
        // Token'ları çöz
        String accessToken = decrypt(encryptedAccessToken, key, iv);
        String refreshToken = decrypt(encryptedRefreshToken, key, iv);
        
        // TokenDTO nesnelerini oluştur
        return new TokenPair(accessToken, refreshToken, accessExpiryStr, refreshExpiryStr);
    }
    
    /**
     * Saklanan token'ları siler
     */
    public static void clearTokens() {
        File file = new File(STORAGE_FILE);
        if (file.exists()) {
            file.delete();
            System.out.println("Token'lar silindi.");
        }
    }
    
    /**
     * Salt değerini oluşturur veya mevcut salt değerini okur
     */
    private static byte[] getSaltBytes() throws IOException {
        File saltFile = new File(SALT_FILE);
        byte[] salt;
        
        if (!saltFile.exists()) {
            salt = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);
            
            try (FileOutputStream fos = new FileOutputStream(saltFile)) {
                fos.write(salt);
            }
        } else {
            salt = new byte[(int) saltFile.length()];
            try (FileInputStream fis = new FileInputStream(saltFile)) {
                fis.read(salt);
            }
        }
        
        return salt;
    }
    
    /**
     * IV değerini oluşturur veya mevcut IV değerini okur
     */
    private static byte[] getIvBytes() throws IOException {
        File ivFile = new File(IV_FILE);
        byte[] iv;
        
        if (!ivFile.exists()) {
            iv = new byte[16]; // AES blok boyutu
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            
            try (FileOutputStream fos = new FileOutputStream(ivFile)) {
                fos.write(iv);
            }
        } else {
            iv = new byte[(int) ivFile.length()];
            try (FileInputStream fis = new FileInputStream(ivFile)) {
                fis.read(iv);
            }
        }
        
        return iv;
    }
    
    /**
     * Şifreleme anahtarı oluşturur
     */
    private static SecretKey generateKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Metni şifreler
     */
    private static String encrypt(String input, SecretKey key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }
    
    /**
     * Şifreli metni çözer
     */
    private static String decrypt(String cipherText, SecretKey key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
    
    /**
     * Access token'ın süresi dolmuş mu kontrol eder
     * 
     * @return true eğer access token süresi dolmuşsa, false aksi halde
     * @throws Exception Token okuma sırasında hata oluşursa
     */
    public static boolean isAccessTokenExpired() throws Exception {
        TokenPair tokens = retrieveTokens();
        if (tokens == null || tokens.getAccessExpiry() == null) {
            return true;
        }
        
        LocalDateTime expiry = LocalDateTime.parse(tokens.getAccessExpiry());
        return LocalDateTime.now().isAfter(expiry);
    }
    
    /**
     * Access token'ın süresi yenileme yapması gereken süreye geldi mi kontrol eder (2 dakika kala)
     * 
     * @return true eğer access token yenilenmesi gerekiyorsa, false aksi halde
     * @throws Exception Token okuma sırasında hata oluşursa
     */
    public static boolean shouldRefreshAccessToken() throws Exception {
        TokenPair tokens = retrieveTokens();
        if (tokens == null || tokens.getAccessExpiry() == null) {
            return true;
        }
        
        LocalDateTime expiry = LocalDateTime.parse(tokens.getAccessExpiry());
        LocalDateTime refreshThreshold = expiry.minusMinutes(2); // 2 dakika kala refresh yap
        return LocalDateTime.now().isAfter(refreshThreshold);
    }
    
    /**
     * Access token'ın ne kadar süre kaldığını dakika cinsinden döndürür
     * 
     * @return Kalan süre dakika cinsinden, token yoksa veya süresi dolmuşsa -1
     * @throws Exception Token okuma sırasında hata oluşursa
     */
    public static long getAccessTokenRemainingMinutes() throws Exception {
        TokenPair tokens = retrieveTokens();
        if (tokens == null || tokens.getAccessExpiry() == null) {
            return -1;
        }
        
        LocalDateTime expiry = LocalDateTime.parse(tokens.getAccessExpiry());
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(expiry)) {
            return -1; // Süresi dolmuş
        }
        
        return java.time.Duration.between(now, expiry).toMinutes();
    }
    
    /**
     * Refresh token'ın süresi dolmuş mu kontrol eder
     * 
     * @return true eğer refresh token süresi dolmuşsa, false aksi halde
     * @throws Exception Token okuma sırasında hata oluşursa
     */
    public static boolean isRefreshTokenExpired() throws Exception {
        TokenPair tokens = retrieveTokens();
        if (tokens == null || tokens.getRefreshExpiry() == null) {
            return true;
        }
        
        LocalDateTime expiry = LocalDateTime.parse(tokens.getRefreshExpiry());
        return LocalDateTime.now().isAfter(expiry);
    }
    
    /**
     * Access token'ı yeniler ve günceller
     * 
     * @param newAccessToken Yeni access token
     * @throws Exception Token güncelleme sırasında hata oluşursa
     */
    public static void updateAccessToken(TokenDTO newAccessToken) throws Exception {
        TokenPair currentTokens = retrieveTokens();
        if (currentTokens == null) {
            throw new Exception("Mevcut token'lar bulunamadı");
        }
        
        // Refresh token'ı mevcut bilgilerle oluştur
        TokenDTO refreshTokenDTO = new TokenDTO(
            currentTokens.getRefreshToken(),
            null, // issuedAt
            LocalDateTime.parse(currentTokens.getRefreshExpiry()),
            null, // lastUsedAt
            null, // ipAddress
            null, // deviceInfo
            TokenType.REFRESH
        );
        
        storeTokens(newAccessToken, refreshTokenDTO);
    }
    
    /**
     * Token çifti sınıfı
     */
    public static class TokenPair {
        private String accessToken;
        private String refreshToken;
        private String accessExpiry;
        private String refreshExpiry;
        
        public TokenPair(String accessToken, String refreshToken, String accessExpiry, String refreshExpiry) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessExpiry = accessExpiry;
            this.refreshExpiry = refreshExpiry;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public String getAccessExpiry() {
            return accessExpiry;
        }
        
        public String getRefreshExpiry() {
            return refreshExpiry;
        }
    }
}