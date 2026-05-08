package com.dinebook.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
