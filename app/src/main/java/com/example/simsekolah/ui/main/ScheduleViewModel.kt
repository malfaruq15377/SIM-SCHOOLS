package com.example.simsekolah.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.adapter.DayScheduleAdapter
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.SchoolRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _dayScheduleList = MutableLiveData<List<DayScheduleAdapter.DaySchedule>>()
    val dayScheduleList: LiveData<List<DayScheduleAdapter.DaySchedule>> = _dayScheduleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchSchedule() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getJadwal().collect { response ->
                    if (response.success) {
                        val grouped = groupSchedulesByDay(response.data)
                        _dayScheduleList.value = grouped
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

    private fun groupSchedulesByDay(list: List<JadwalItem>): List<DayScheduleAdapter.DaySchedule> {
        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        
        return daysOrder.map { dayName ->
            val itemsForDay = list.filter { it.hari.equals(dayName, ignoreCase = true) }
            DayScheduleAdapter.DaySchedule(
                dayName = dayName,
                items = itemsForDay,
                isExpanded = false
            )
        }
    }
}
