package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class MapelItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("kodeMapel")
    val kodeMapel: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("deskripsi")
    val deskripsi: String,
    @SerializedName("kategori")
    val kategori: String,
    @SerializedName("guruId")
    val guruId: Int,
    @SerializedName("kelasId")
    val kelasId: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)
