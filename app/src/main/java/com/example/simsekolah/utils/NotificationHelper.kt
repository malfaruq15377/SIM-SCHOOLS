package com.example.simsekolah.utils

import android.content.Context
import com.example.simsekolah.model.NotificationModel
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
    }

    fun hasUnread(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_UNREAD, false)
    }
}
