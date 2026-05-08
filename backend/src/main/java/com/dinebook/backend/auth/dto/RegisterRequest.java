package com.dinebook.backend.auth.dto;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {}
