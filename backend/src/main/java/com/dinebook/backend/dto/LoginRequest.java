package com.dinebook.backend.dto;

public record LoginRequest(
        String email,
        String password
) {}