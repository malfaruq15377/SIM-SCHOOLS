package com.example.simsekolah.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<SiswaItem>>(emptyList())
    val students: StateFlow<List<SiswaItem>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.getSiswa()
                .catch { _students.value = emptyList() }
                .collect { response ->
                    _students.value = response.data
                }
            _isLoading.value = false
        }
    }

    fun submitAttendance(studentId: Int, status: String, date: String) {
        // Implementation for submitting attendance
    }
}
