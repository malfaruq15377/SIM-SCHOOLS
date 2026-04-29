package com.example.simsekolah.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.data.remote.response.NotificationResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    init {
        val uid = schoolRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                schoolRepo.getNotifications(uid).collect { list ->
                    _notifications.value = list
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val uid = schoolRepo.getCurrentUserUid() ?: return
        viewModelScope.launch {
            schoolRepo.markAsRead(uid, notificationId)
        }
    }
}
