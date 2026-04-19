package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    val id: String = System.currentTimeMillis().toString(),
    val deadline: String,
    val time: String,
    val title: String,
    val description: String,
    val fileName: String? = null,
    val filePath: String? = null,
    var isDone: Boolean = false,
    val teacherId: String? = null, // Email atau ID Guru
    val kelasId: Int? = null,      // ID Kelas agar hanya murid di kelas ini yang bisa lihat
    val submissions: List<SubmissionModel> = emptyList()
) : Parcelable

@Parcelize
data class SubmissionModel(
    val studentName: String,
    val studentId: String,
    val isCompleted: Boolean = false,
    val submittedAt: String? = null
) : Parcelable
