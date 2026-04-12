package com.dinebook.mobile.api

import com.dinebook.mobile.models.AuthResponse
import com.dinebook.mobile.models.LoginRequest
import com.dinebook.mobile.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
