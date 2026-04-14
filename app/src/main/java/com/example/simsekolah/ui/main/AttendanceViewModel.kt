package com.example.simsekolah.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.repository.AbsensiItem
import com.example.simsekolah.data.remote.repository.SchoolRepository
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _attendanceList = MutableLiveData<List<AbsensiItem>>()
    val attendanceList: LiveData<List<AbsensiItem>> = _attendanceList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchAttendance() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAbsensi().collect { response ->
                    if (response.success) {
                        _attendanceList.value = response.data
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
}
