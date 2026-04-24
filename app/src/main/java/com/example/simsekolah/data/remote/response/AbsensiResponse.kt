package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class AbsensiResponse(
    @SerializedName("data")
    val data: List<AbsensiItem> = emptyList(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = ""
)

data class AbsensiItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("muridId")
    val muridId: String = "",
    @SerializedName("tanggal")
    val tanggal: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("keterangan")
    val keterangan: String? = null
)
