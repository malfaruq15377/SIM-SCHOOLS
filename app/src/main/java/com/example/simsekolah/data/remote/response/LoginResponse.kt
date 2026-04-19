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
    @SerializedName("kelasId")
    val kelasId: Int?,
    @SerializedName("role")
    val role: String? = "siswa",
    @SerializedName("password")
    val password: String? = null// Default ke siswa berdasarkan JSON Anda
)
