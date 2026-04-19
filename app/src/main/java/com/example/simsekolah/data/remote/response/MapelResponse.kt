package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class MapelResponse(
    @SerializedName("data")
    val data: List<MapelItem>,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class MapelItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)
