package com.example.simsekolah.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.data.remote.response.AttendanceResponse
import com.example.simsekolah.data.remote.response.UserResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserResponse?>(null)
    val userProfile: StateFlow<UserResponse?> = _userProfile.asStateFlow()

    private val _students = MutableStateFlow<List<UserResponse>>(emptyList())
    val students: StateFlow<List<UserResponse>> = _students.asStateFlow()

    private val _operationStatus = MutableSharedFlow<Result<Unit>>()
    val operationStatus = _operationStatus.asSharedFlow()

    private val _isAttendanceActive = MutableStateFlow(false)
    val isAttendanceActive: StateFlow<Boolean> = _isAttendanceActive.asStateFlow()

    init {
        val uid = schoolRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                schoolRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    if (user?.role == "guru") {
                        loadStudents(user.uid)
                    } else if (user?.role == "siswa") {
                        checkAttendanceTime()
                    }
                }
            }
        }
    }

    private fun loadStudents(guruId: String) {
        viewModelScope.launch {
            _students.value = schoolRepo.getStudentsForGuru(guruId)
        }
    }

    private fun checkAttendanceTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        _isAttendanceActive.value = hour >= 8
    }

    fun submitAttendanceGuru(attendances: List<AttendanceResponse>) {
        viewModelScope.launch {
            _operationStatus.emit(schoolRepo.saveAttendanceBatch(attendances))
        }
    }

    fun submitAttendanceSiswa(status: String? = null) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            
            val finalStatus = if (status != null) {
                status // Sick or Permission
            } else {
                if (hour == 8 && minute < 30) "Present" else "Late"
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val attendance = AttendanceResponse(
                studentId = user.uid,
                studentName = user.name,
                kelasId = user.kelasId ?: "",
                guruId = user.waliKelasId ?: "",
                status = finalStatus,
                date = date
            )
            
            _operationStatus.emit(schoolRepo.submitStudentAttendance(attendance))
        }
    }
}
