package com.example.simsekolah.utils

import android.content.Context
import com.example.simsekolah.data.local.entity.NotificationModel
import com.example.simsekolah.data.local.room.SekolahDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {
    fun addNotification(context: Context, title: String, body: String, type: String) {
        val database = SekolahDatabase.getDatabase(context)
        val notification = NotificationModel(
            title = title,
            body = body,
            type = type
        )
        CoroutineScope(Dispatchers.IO).launch {
            database.sekolahDao().insertNotification(notification)
        }
    }
}
