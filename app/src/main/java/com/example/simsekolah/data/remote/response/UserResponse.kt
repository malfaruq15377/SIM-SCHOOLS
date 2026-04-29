package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @field:SerializedName("id") val id: Int? = null,
    @field:SerializedName("uid") val uid: String = "",
    @field:SerializedName("name") val name: String = "",
    @field:SerializedName("email") val email: String = "",
    @field:SerializedName("phone") val phone: String? = null,
    @field:SerializedName("address") val address: String? = null,
    @field:SerializedName("role") val role: String = "",
    @field:SerializedName("token") val token: String? = null,
    @field:SerializedName("nis") val nis: String? = null,
    @field:SerializedName("nip") val nip: String? = null,
    @field:SerializedName("kelasId") val kelasId: Int? = null,
    @field:SerializedName("waliKelasId") val waliKelasId: Int? = null,
    @field:SerializedName("gender") val gender: String? = null,
    @field:SerializedName("birthDate") val birthDate: String? = null,
    @field:SerializedName("photoUrl") val photoUrl: String? = null,
    @field:SerializedName("backgroundUrl") val backgroundUrl: String? = null
)
