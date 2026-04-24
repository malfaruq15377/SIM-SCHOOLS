package com.example.simsekolah.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.simsekolah.R
import com.example.simsekolah.model.NotificationModel
import com.example.simsekolah.ui.main.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationHelper {
    private const val PREF_NAME = "NotifData"
    private const val KEY_LIST = "list_notif"
    private const val KEY_UNREAD = "has_unread"
    private val gson = Gson()

    fun addNotification(context: Context, title: String, message: String, type: String = "general") {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sharedPref.getString(KEY_LIST, null)
        val list: MutableList<NotificationModel> = if (json != null) {
            val listType = object : TypeToken<MutableList<NotificationModel>>() {}.type
            gson.fromJson(json, listType)
        } else mutableListOf()

        val newNotif = NotificationModel(
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        
        list.add(newNotif)
        sharedPref.edit().apply {
            putString(KEY_LIST, gson.toJson(list))
            putBoolean(KEY_UNREAD, true)
            apply()
        }

        // Tampilkan System Notification agar muncul di bilah status walaupun aplikasi ditutup
        showSystemNotification(context, title, message, type)
    }

    private fun showSystemNotification(context: Context, title: String, message: String, type: String) {
        val channelId = "school_notification_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SIM Sekolah", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val destinationId = when(type) {
            "tugas" -> R.id.assignmentsFragment
            "absensi" -> R.id.attendanceFragment
            "event" -> R.id.eventFragment
            else -> R.id.homeFragment
        }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_main)
            .setDestination(destinationId)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun hasUnread(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_UNREAD, false)
    }
    
    fun setRead(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_UNREAD, false).apply()
    }
}
