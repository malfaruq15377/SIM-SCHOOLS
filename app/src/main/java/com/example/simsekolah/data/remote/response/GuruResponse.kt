package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class GuruResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<GuruItem>
)

data class GuruItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("nip")
    val nip: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("birthDate")
    val birthDate: String,
    @SerializedName("isWaliKelas")
    val isWaliKelas: Boolean,
    @SerializedName("status")
    val status: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    // Field tambahan yang mungkin dibutuhkan untuk relasi lokal (opsional)
    @SerializedName("kelasId")
    val kelasId: Int? = null
)
