package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceModel(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val kelasId: String = "",
    val guruId: String = "", // waliKelasId
    val status: String = "", // "Present", "Late", "Sick", "Permission"
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "" // formatted date "yyyy-MM-dd" for easy query
) : Parcelable
