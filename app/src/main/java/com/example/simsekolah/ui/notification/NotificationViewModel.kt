package com.example.simsekolah.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.data.repository.NotificationRepository
import com.example.simsekolah.model.NotificationModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepo: NotificationRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                notificationRepo.getNotifications(uid).collect { list ->
                    _notifications.value = list
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val uid = authRepo.getCurrentUserUid() ?: return
        viewModelScope.launch {
            notificationRepo.markAsRead(uid, notificationId)
        }
    }
}
