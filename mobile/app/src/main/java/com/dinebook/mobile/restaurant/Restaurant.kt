package com.dinebook.mobile.restaurant

import com.google.gson.annotations.SerializedName

data class Restaurant(
    val id: Long,
    val name: String,
    val location: String,
    val cuisine: String,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    val rating: Double?
)
