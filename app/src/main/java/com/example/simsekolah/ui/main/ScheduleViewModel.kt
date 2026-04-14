package com.example.simsekolah.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.repository.JadwalItem
import com.example.simsekolah.data.remote.repository.SchoolRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _scheduleList = MutableLiveData<List<JadwalItem>>()
    val scheduleList: LiveData<List<JadwalItem>> = _scheduleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var allSchedules: List<JadwalItem> = emptyList()

    fun fetchSchedule() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getJadwal().collect { response ->
                    if (response.success) {
                        allSchedules = response.data
                        _scheduleList.value = allSchedules
                    } else {
                        _errorMessage.value = response.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByDay(day: String) {
        _scheduleList.value = if (day.isEmpty() || day.lowercase() == "all") {
            allSchedules
        } else {
            allSchedules.filter { it.hari.equals(day, ignoreCase = true) }
        }
    }
}
