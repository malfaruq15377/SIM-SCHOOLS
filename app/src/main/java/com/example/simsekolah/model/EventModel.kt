package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val notifSent: Boolean = false
) : Parcelable

@Parcelize
data class SchoolInfoModel(
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val imageUrl: String? = null
) : Parcelable
