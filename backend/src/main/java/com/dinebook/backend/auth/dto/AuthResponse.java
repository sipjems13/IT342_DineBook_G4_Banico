package com.dinebook.backend.auth.dto;

public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String userId;
    private final String email;

    private AuthResponse(Builder builder) {
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.userId = builder.userId;
        this.email = builder.email;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String userId;
        private String email;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(this);
        }
    }
}
