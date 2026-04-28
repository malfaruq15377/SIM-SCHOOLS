package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginGuruResponse(
    @field:SerializedName("msg")
    val msg: String,

    @field:SerializedName("data")
    val data: LoginGuruData
)

data class LoginGuruData(
    @field:SerializedName("user")
    val user: GuruUser,

    @field:SerializedName("token")
    val token: String
)

data class GuruUser(
    @field:SerializedName("address")
    val address: String,

    @field:SerializedName("gender")
    val gender: String,

    @field:SerializedName("isActive")
    val isActive: Boolean,

    @field:SerializedName("uuid")
    val uuid: String,

    @field:SerializedName("birthDate")
    val birthDate: String,

    @field:SerializedName("isWaliKelas")
    val isWaliKelas: Boolean,

    @field:SerializedName("createdAt")
    val createdAt: String,

    @field:SerializedName("password")
    val password: String,

    @field:SerializedName("nip")
    val nip: String,

    @field:SerializedName("phone")
    val phone: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("updatedAt")
    val updatedAt: String
)
