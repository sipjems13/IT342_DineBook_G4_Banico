package com.dinebook.mobile.restaurant

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantService {

    @GET("restaurants")
    suspend fun browseRestaurants(
        @Header("Authorization") authorization: String,
        @Query("location") location: String = "Cebu City",
        @Query("cuisine") cuisine: String? = null,
        @Query("q") query: String? = null
    ): Response<List<Restaurant>>

    @GET("restaurants/{id}")
    suspend fun getRestaurant(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): Response<Restaurant>
}
