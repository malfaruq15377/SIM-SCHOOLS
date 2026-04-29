package com.example.simsekolah.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments_local")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: String,
    val fileUrl: String?,
    val guruId: Int,
    val kelasId: Int
)
