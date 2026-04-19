package com.example.simsekolah.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sekolah")
data class SekolahEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val type: String // e.g., "PENGUMUMAN", "EVENT", etc.
)
