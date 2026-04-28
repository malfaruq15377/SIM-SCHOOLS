package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    val uid: String = "",
    val name: String = "",
    val role: String = "", // "guru" or "siswa"
    val email: String = "",
    val photoUrl: String? = null,
    val backgroundUrl: String? = null,
    val kelasId: String? = null,
    val namaKelas: String? = null,
    val waliKelasId: String? = null, // uid guru
    val waliKelasName: String? = null
) : Parcelable
