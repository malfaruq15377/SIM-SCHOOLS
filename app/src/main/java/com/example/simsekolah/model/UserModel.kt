package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val role: String,
    val token: String,
    val isLogin: Boolean = false,
    val extraInfo: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val isWaliKelas: Boolean = false,
    val kelasId: Int? = null
) : Parcelable
