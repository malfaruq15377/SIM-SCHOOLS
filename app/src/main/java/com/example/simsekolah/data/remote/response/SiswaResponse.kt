package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class SiswaItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("nis")
    val nis: String,
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
    @SerializedName("parentName")
    val parentName: String,
    @SerializedName("kelasId")
    val kelasId: Int,
    @SerializedName("status")
    var status: String,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("deletedAt")
    val deletedAt: String?
)
