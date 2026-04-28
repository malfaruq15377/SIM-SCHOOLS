package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignmentModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val fileUrl: String? = null,
    val guruId: String = "",
    val kelasId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SubmissionModel(
    val id: String = "",
    val assignmentId: String = "",
    val studentId: String = "",
    val fileUrl: String = "",
    val submittedAt: Long = System.currentTimeMillis()
) : Parcelable
