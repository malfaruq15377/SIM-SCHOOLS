package com.example.simsekolah.data.model

data class EventModel(
    val day: String,
    val month: String,
    val title: String,
    val time: String,
    val location: String,
    val description: String = "",
    val color: Int = 0xFF4A90E2.toInt() // Default blue
)
