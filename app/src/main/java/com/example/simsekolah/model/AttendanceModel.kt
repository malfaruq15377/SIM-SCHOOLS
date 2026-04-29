package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttendanceModel(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val date: String = "",
    val status: String = "", // Present, Absent, Late, Sick, Permission
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
