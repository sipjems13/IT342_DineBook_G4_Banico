package com.dinebook.backend.dto;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {}