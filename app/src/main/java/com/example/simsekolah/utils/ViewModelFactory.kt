package com.example.simsekolah.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.ui.assignment.AssignmentsViewModel
import com.example.simsekolah.ui.attendance.AttendanceViewModel
import com.example.simsekolah.ui.home.HomeViewModel
import com.example.simsekolah.ui.notification.NotificationViewModel
import com.example.simsekolah.ui.schedule.ScheduleViewModel
import com.example.simsekolah.ui.main.ProfileViewModel
import com.example.simsekolah.ui.event.EventViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory private constructor(
    private val schoolRepo: SchoolRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(AssignmentsViewModel::class.java) -> {
                AssignmentsViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> {
                AttendanceViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                ScheduleViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(schoolRepo) as T
            }
            modelClass.isAssignableFrom(EventViewModel::class.java) -> {
                EventViewModel(schoolRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(SchoolRepository())
            }.also { instance = it }
    }
}
