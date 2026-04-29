package com.example.simsekolah.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.data.remote.response.ScheduleResponse
import com.example.simsekolah.data.remote.response.UserResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserResponse?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _schedules = MutableStateFlow<List<ScheduleResponse>>(emptyList())
    val schedules: StateFlow<List<ScheduleResponse>> = _schedules.asStateFlow()

    private val _updateStatus = MutableSharedFlow<Result<Unit>>()
    val updateStatus = _updateStatus.asSharedFlow()

    init {
        val uid = schoolRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                schoolRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    user?.kelasId?.let { kelasId ->
                        loadSchedules(kelasId)
                    }
                }
            }
        }
    }

    private fun loadSchedules(kelasId: String) {
        viewModelScope.launch {
            schoolRepo.getSchedulesByKelas(kelasId).collect { list ->
                _schedules.value = list
            }
        }
    }

    fun updateSchedule(schedule: ScheduleResponse) {
        viewModelScope.launch {
            _updateStatus.emit(schoolRepo.updateSchedule(schedule))
        }
    }
}
