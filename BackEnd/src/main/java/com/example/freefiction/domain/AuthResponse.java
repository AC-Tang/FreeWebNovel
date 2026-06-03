package com.example.freefiction.domain;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public AuthResponse(String newRefreshToken, String newAccessToken) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
    }
}
