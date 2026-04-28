package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleModel(
    val id: String = "",
    val kelasId: String = "",
    val day: String = "", // "Senin", "Selasa", etc.
    val subjects: List<SubjectModel> = emptyList()
) : Parcelable

@Parcelize
data class SubjectModel(
    val name: String = "",
    val time: String = "",
    val guruName: String = ""
) : Parcelable
