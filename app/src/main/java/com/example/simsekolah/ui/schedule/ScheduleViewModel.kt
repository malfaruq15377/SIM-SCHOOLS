package com.example.simsekolah.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<JadwalItem>>(emptyList())
    val schedules: StateFlow<List<JadwalItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSchedules(kelasId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.getJadwal(kelasId)
                .catch { _schedules.value = emptyList() }
                .collect { response ->
                    _schedules.value = response.data
                }
            _isLoading.value = false
        }
    }
}
