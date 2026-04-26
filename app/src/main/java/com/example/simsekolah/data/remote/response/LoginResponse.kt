package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: LoginData
)

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
    @SerializedName("status")
    val status: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,

    // Field khusus Siswa
    @SerializedName("nis")
    val nis: String? = null,
    @SerializedName("parentName")
    val parentName: String? = null,
    @SerializedName("kelasId")
    val kelasId: Int? = null,
    @SerializedName("deletedAt")
    val deletedAt: String? = null,

    // Field khusus Guru
    @SerializedName("nip")
    val nip: String? = null,
    @SerializedName("isWaliKelas")
    val isWaliKelas: Boolean? = null
)
