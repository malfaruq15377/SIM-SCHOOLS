package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

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
