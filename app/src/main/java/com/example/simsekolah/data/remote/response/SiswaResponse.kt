package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class SiswaResponse(
    @SerializedName("data")
    val data: List<SiswaItem>,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class SiswaItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val nama: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("kelasId")
    val kelasId: Int? = null
)
