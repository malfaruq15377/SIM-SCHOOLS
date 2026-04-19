package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class JadwalResponse(
    @SerializedName("data")
    val data: List<JadwalItem>,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class JadwalItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("hari")
    val hari: String,
    @SerializedName("jamMulai")
    val jamMulai: String,
    @SerializedName("jamSelesai")
    val jamSelesai: String,
    @SerializedName("mapelId")
    val mapelId: String,
    @SerializedName("kelasId")
    val kelasId: String? = null,
    @SerializedName("guruId")
    val guruId: String? = null,
    @SerializedName("mapel")
    val mapel: MapelInfo? = null,
    @SerializedName("guru")
    val guru: GuruInfo? = null,
    @SerializedName("ruangan")
    val ruangan: String? = null
)

data class MapelInfo(
    @SerializedName("name")
    val name: String
)

data class GuruInfo(
    @SerializedName("nama")
    val nama: String
)
