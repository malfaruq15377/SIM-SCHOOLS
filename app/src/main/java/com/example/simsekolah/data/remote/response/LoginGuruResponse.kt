package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginGuruResponse(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: LoginGuruData
)

data class LoginGuruData(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: GuruUser
)

data class GuruUser(
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
    @SerializedName("kelasId")
    val kelasId: Int,
    @SerializedName("mapelId")
    val mapelId: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)