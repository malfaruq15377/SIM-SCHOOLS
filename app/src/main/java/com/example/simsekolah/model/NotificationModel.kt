package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "Assignment" or "Event"
    val referenceId: String = "", // assignmentId or eventId
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
