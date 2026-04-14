package com.example.simsekolah.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simsekolah.data.remote.repository.SchoolRepository
import com.example.simsekolah.data.remote.retrofit.ApiConfig
import com.example.simsekolah.ui.auth.LoginViewModel
import com.example.simsekolah.ui.main.AttendanceViewModel
import com.example.simsekolah.ui.main.ScheduleViewModel

class ViewModelFactory(private val repository: SchoolRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(): ViewModelFactory =
            instance ?: synchronized(this) {
                val apiService = ApiConfig.getApiService()
                val repository = SchoolRepository.getInstance(apiService)
                instance ?: ViewModelFactory(repository)
            }.also { instance = it }
    }
}
