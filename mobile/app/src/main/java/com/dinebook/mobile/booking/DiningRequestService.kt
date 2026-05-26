package com.dinebook.mobile.booking

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DiningRequestService {

    @POST("dining-requests")
    suspend fun createDiningRequest(
        @Header("Authorization") authorization: String,
        @Body request: CreateDiningRequest
    ): Response<DiningRequest>

    @GET("dining-requests/my")
    suspend fun myRequests(
        @Header("Authorization") authorization: String
    ): Response<List<DiningRequest>>
}
