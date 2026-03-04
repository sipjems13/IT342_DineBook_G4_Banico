package com.dinebook.backend.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String email
) {}