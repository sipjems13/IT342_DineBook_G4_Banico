package com.dinebook.mobile.auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)
