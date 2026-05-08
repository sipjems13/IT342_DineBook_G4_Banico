package com.dinebook.mobile.shared

import com.dinebook.mobile.auth.AuthService
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 is the special alias to the host loopback interface in Google Android emulators
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Supabase returns snake_case (access_token, refresh_token, user_id)
    // but our Kotlin models use camelCase (accessToken, refreshToken, userId)
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}
