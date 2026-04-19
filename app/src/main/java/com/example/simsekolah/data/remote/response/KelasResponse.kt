package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class KelasResponse(
    @SerializedName("data")
    val data: List<KelasItem>,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class KelasItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("uuid")
    val uuid: String? = null
)
