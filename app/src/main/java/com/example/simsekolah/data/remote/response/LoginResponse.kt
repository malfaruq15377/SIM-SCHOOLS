package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

// Kita hanya butuh Data dan User-nya saja, wrapper Response sudah dihandle BaseResponse
data class LoginData(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserData
)

data class UserData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("status")
    val status: String, // Ini yang jadi role (guru/siswa)
    @SerializedName("phone")
    val phone: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("birthDate")
    val birthDate: String,

    // Field Opsional
    @SerializedName("nis")
    val nis: String? = null,
    @SerializedName("nip")
    val nip: String? = null,
    @SerializedName("parentName")
    val parentName: String? = null,
    @SerializedName("kelasId")
    val kelasId: Int? = null
)
