package com.bincard.bincard_superadmin;

public class TokenResponse {
    private TokenDTO accessToken;
    private TokenDTO refreshToken;

    public TokenResponse(TokenDTO accessToken, TokenDTO refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public TokenDTO getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(TokenDTO accessToken) {
        this.accessToken = accessToken;
    }

    public TokenDTO getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(TokenDTO refreshToken) {
        this.refreshToken = refreshToken;
    }
} 