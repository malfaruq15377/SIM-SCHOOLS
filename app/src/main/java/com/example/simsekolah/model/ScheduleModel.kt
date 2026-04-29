package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleModel(
    val id: String,
    val day: String,
    val subject: String,
    val time: String,
    val teacher: String,
    val room: String? = null
) : Parcelable
