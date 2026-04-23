package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    val id: String = System.currentTimeMillis().toString(),
    val deadline: String = "",
    val time: String = "",
    val title: String = "",
    val description: String = "",
    val fileName: String? = null,
    val filePath: String? = null,
    var isDone: Boolean = false,
    val teacherId: String? = null,
    val kelasId: Int? = null,
    val submissions: List<SubmissionModel> = emptyList()
) : Parcelable

@Parcelize
data class SubmissionModel(
    val studentName: String = "",
    val studentId: String = "",
    val isCompleted: Boolean = false,
    val submittedAt: String? = null
) : Parcelable
