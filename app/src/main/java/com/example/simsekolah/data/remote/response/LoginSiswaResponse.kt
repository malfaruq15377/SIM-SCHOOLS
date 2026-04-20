package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginSiswaResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<DataItem>
)

data class DataItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String?,

    @SerializedName("kelasId")
    val kelasId: Int?,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("kelas")
    val kelas: Kelas?,

    @SerializedName("nilais")
    val nilais: List<NilaisItem>?,

    @SerializedName("absensis")
    val absensis: List<AbsensisItem>?
)

data class Kelas(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("waliKelasId")
    val waliKelasId: Int?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class NilaisItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("nilai")
    val nilai: Int,

    @SerializedName("siswaId")
    val siswaId: Int,

    @SerializedName("guruId")
    val guruId: Int,

    @SerializedName("mapelId")
    val mapelId: Int,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class AbsensisItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("siswaId")
    val siswaId: Int,

    @SerializedName("jadwalId")
    val jadwalId: Int?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
