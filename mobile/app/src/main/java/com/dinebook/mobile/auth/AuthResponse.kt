package com.dinebook.mobile.auth

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: String?,
    val email: String?
)
