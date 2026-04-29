package com.example.simsekolah.data.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationResponse(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "Assignment" or "Event"
    val referenceId: String = "", // assignmentId or eventId
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
