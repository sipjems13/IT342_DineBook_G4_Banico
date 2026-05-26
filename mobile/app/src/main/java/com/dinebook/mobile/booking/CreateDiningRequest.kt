package com.dinebook.mobile.booking

import com.google.gson.annotations.SerializedName

data class CreateDiningRequest(
    @SerializedName("restaurantId")
    val restaurantId: Long,
    @SerializedName("requestedDateTime")
    val requestedDateTime: String,
    val guests: Int
)
