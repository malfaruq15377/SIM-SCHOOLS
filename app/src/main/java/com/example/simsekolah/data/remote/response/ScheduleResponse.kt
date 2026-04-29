package com.example.simsekolah.data.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleResponse(
    val id: String = "",
    val kelasId: String = "",
    val day: String = "", // "Senin", "Selasa", etc.
    val subjects: List<SubjectItem> = emptyList()
) : Parcelable

@Parcelize
data class SubjectItem(
    val name: String = "",
    val time: String = "",
    val guruName: String = ""
) : Parcelable
