# Authentication API Client Refactoring

## Overview
The authentication-related API methods have been extracted from `ApiClientFX.java` into a dedicated `AuthApiClient.java` class to improve code organization and maintainability.

## Changes Made

### 1. Created AuthApiClient.java
A new dedicated API client class containing all authentication-related methods:
- `signup()` - Superadmin kayıt işlemi
- `login()` - Superadmin giriş işlemi
- `phoneVerify()` - Telefon doğrulama işlemi
- `refreshToken()` - Token yenileme işlemi
- `resendVerificationCode()` - Yeniden doğrulama kodu gönderme
- `getSavedTokens()` - Kayıtlı token'ları okuma
- `clearSavedTokens()` - Token temizleme

### 2. Utility Methods Moved
The following utility methods were also moved to AuthApiClient:
- `getPublicIpAddress()` - Dış IP adresini alma
- `getDeviceInfo()` - Cihaz bilgilerini alma
- `extractNestedValue()` - JSON'dan nested değer çıkarma
- `parseDateTime()` - DateTime string'ini parse etme
- `extractJsonMessage()` - JSON'dan hata mesajı çıkarma

### 3. Updated References
All references to authentication methods in existing files have been updated:
- `SuperadminDashboardFX.java` - clearSavedTokens() call
- `SuperadminLoginFX.java` - login(), phoneVerify(), refreshToken(), resendVerificationCode() calls
- `SuperadminSignupFX.java` - signup() call

### 4. Removed from ApiClientFX.java
- Removed 7 authentication API methods
- Removed 5 utility methods that were primarily used by authentication
- Updated getSavedTokens() to use AuthApiClient methods

### 5. Java 20+ Compatibility
All URL constructors have been updated to use the modern `URI.toURL()` pattern to avoid deprecation warnings.

## Benefits

1. **Single Responsibility**: AuthApiClient focuses exclusively on authentication operations
2. **Better Organization**: Authentication logic is separated from general API operations
3. **Improved Maintainability**: Easier to find and maintain authentication-related code
4. **Cleaner Architecture**: Follows the principle of separation of concerns
5. **Consistent Design**: Matches the pattern used for PaymentPointApiClient

## Usage Examples

### Before (using ApiClientFX)
```java
LoginResponse response = ApiClientFX.login(phone, password);
TokenResponse tokens = ApiClientFX.phoneVerify(phone, code);
ApiClientFX.clearSavedTokens();
```

### After (using AuthApiClient)
```java
LoginResponse response = AuthApiClient.login(phone, password);
TokenResponse tokens = AuthApiClient.phoneVerify(phone, code);
AuthApiClient.clearSavedTokens();
```

## Files Modified

1. **New File**: `AuthApiClient.java` - Complete authentication API client
2. **Modified**: `ApiClientFX.java` - Removed authentication methods, updated references
3. **Modified**: `SuperadminDashboardFX.java` - Updated clearSavedTokens() call
4. **Modified**: `SuperadminLoginFX.java` - Updated all authentication method calls
5. **Modified**: `SuperadminSignupFX.java` - Updated signup() call

## Testing Required

After this refactoring, the following functionality should be tested:
- Superadmin signup process
- Superadmin login process  
- Phone verification process
- Token refresh mechanism
- Resend verification code feature
- Token storage and retrieval
- Logout functionality (token clearing)

## Future Considerations

This refactoring establishes a pattern for creating dedicated API clients. Consider similar extraction for:
- NewsApiClient (for news-related operations)
- AdminApiClient (for admin management operations)
- ReportApiClient (for reporting operations)

This would further improve code organization and maintainability.
