package com.dinebook.mobile.shared

import com.dinebook.mobile.auth.AuthService
import com.dinebook.mobile.booking.DiningRequestService
import com.dinebook.mobile.restaurant.RestaurantService
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Physical phone over USB: run `adb reverse tcp:8080 tcp:8080` before testing.
    // Android emulator: use http://10.0.2.2:8080/
    // Physical phone over Wi-Fi: use your computer's LAN IP, e.g. http://192.168.1.22:8080/
    private const val BASE_URL = "http://127.0.0.1:8080/"

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

    val restaurantService: RestaurantService by lazy {
        retrofit.create(RestaurantService::class.java)
    }

    val diningRequestService: DiningRequestService by lazy {
        retrofit.create(DiningRequestService::class.java)
    }
}
