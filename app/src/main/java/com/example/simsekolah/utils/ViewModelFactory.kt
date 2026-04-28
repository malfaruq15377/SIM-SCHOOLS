package com.example.simsekolah.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simsekolah.data.repository.*
import com.example.simsekolah.ui.assignment.AssignmentsViewModel
import com.example.simsekolah.ui.attendance.AttendanceViewModel
import com.example.simsekolah.ui.home.HomeViewModel
import com.example.simsekolah.ui.notification.NotificationViewModel
import com.example.simsekolah.ui.schedule.ScheduleViewModel
import com.example.simsekolah.ui.main.ProfileViewModel
import com.example.simsekolah.ui.event.EventViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory private constructor(
    private val authRepo: AuthRepository,
    private val assignmentRepo: AssignmentRepository,
    private val notificationRepo: NotificationRepository,
    private val attendanceRepo: AttendanceRepository,
    private val scheduleRepo: ScheduleRepository,
    private val eventRepo: EventRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepo, assignmentRepo, eventRepo) as T
            }
            modelClass.isAssignableFrom(AssignmentsViewModel::class.java) -> {
                AssignmentsViewModel(assignmentRepo, authRepo, notificationRepo) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepo, authRepo) as T
            }
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> {
                AttendanceViewModel(attendanceRepo, authRepo) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                ScheduleViewModel(scheduleRepo, authRepo) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(authRepo, assignmentRepo) as T
            }
            modelClass.isAssignableFrom(EventViewModel::class.java) -> {
                EventViewModel(eventRepo, authRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    AuthRepository(),
                    AssignmentRepository(),
                    NotificationRepository(),
                    AttendanceRepository(),
                    ScheduleRepository(),
                    EventRepository()
                )
            }.also { instance = it }
    }
}
