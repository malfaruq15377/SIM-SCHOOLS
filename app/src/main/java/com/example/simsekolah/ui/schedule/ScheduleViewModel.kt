package com.example.simsekolah.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.data.repository.ScheduleRepository
import com.example.simsekolah.model.ScheduleModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val scheduleRepo: ScheduleRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<ScheduleModel>>(emptyList())
    val schedules: StateFlow<List<ScheduleModel>> = _schedules.asStateFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                authRepo.getUserProfileRealtime(uid).collect { user ->
                    user?.kelasId?.let { kelasId ->
                        loadSchedules(kelasId)
                    }
                }
            }
        }
    }

    private fun loadSchedules(kelasId: String) {
        viewModelScope.launch {
            scheduleRepo.getSchedulesByKelas(kelasId).collect { list ->
                _schedules.value = list
            }
        }
    }
}
