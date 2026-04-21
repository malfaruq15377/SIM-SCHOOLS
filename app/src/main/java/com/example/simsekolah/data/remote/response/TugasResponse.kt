package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class TugasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<TugasItem>
)

data class TugasItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("guruId")
    val guruId: Int,
    @SerializedName("mapelId")
    val mapelId: Int,
    @SerializedName("namaUjian")
    val namaUjian: String,
    @SerializedName("deskripsi")
    val deskripsi: String,
    @SerializedName("file")
    val file: String? = null,
    @SerializedName("tipe")
    val tipe: String,
    @SerializedName("hari")
    val hari: String,
    @SerializedName("jamUpload")
    val jamUpload: String,
    @SerializedName("deadlineTanggal")
    val deadlineTanggal: Int,
    @SerializedName("deadlineBulan")
    val deadlineBulan: Int,
    @SerializedName("deadlineTahun")
    val deadlineTahun: Int,
    @SerializedName("deadlineJam")
    val deadlineJam: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("guru")
    val guru: GuruInfoTugas? = null,
    @SerializedName("mapel")
    val mapel: MapelInfoTugas? = null
)

data class GuruInfoTugas(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)

data class MapelInfoTugas(
    @SerializedName("name")
    val name: String
)
