package com.dinebook.mobile.models

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: String?,
    val email: String?
)
