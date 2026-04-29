package com.example.simsekolah.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapelResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<MapelItem>? = null
) : Parcelable

@Parcelize
data class MapelItem(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("uuid")
    val uuid: String? = null,

    @SerializedName("kodeMapel")
    val kodeMapel: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("kategori")
    val kategori: String? = null,

    @SerializedName("guruId")
    val guruId: Int? = null,

    @SerializedName("kelasId")
    val kelasId: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable
