package com.dinebook.mobile.booking

import com.google.gson.annotations.SerializedName

data class DiningRequest(
    val id: Long,
    @SerializedName("restaurantId")
    val restaurantId: Long,
    @SerializedName("restaurantName")
    val restaurantName: String,
    @SerializedName("dinerEmail")
    val dinerEmail: String,
    @SerializedName("requestedDateTime")
    val requestedDateTime: String,
    val guests: Int,
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String
)
