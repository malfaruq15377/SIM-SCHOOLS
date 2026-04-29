package com.example.simsekolah.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "submissions_local")
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val assignmentId: Int,
    val siswaId: Int,
    val fileUrl: String,
    val submittedAt: String
)
