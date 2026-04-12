package com.dinebook.mobile.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)
