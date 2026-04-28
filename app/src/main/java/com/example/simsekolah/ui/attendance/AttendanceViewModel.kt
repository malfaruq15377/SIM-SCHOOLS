package com.example.simsekolah.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AttendanceRepository
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.model.AttendanceModel
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(
    private val attendanceRepo: AttendanceRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _students = MutableStateFlow<List<UserModel>>(emptyList())
    val students: StateFlow<List<UserModel>> = _students.asStateFlow()

    private val _operationStatus = MutableSharedFlow<Result<Unit>>()
    val operationStatus = _operationStatus.asSharedFlow()

    private val _isAttendanceActive = MutableStateFlow(false)
    val isAttendanceActive: StateFlow<Boolean> = _isAttendanceActive.asStateFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                authRepo.getUserProfileRealtime(uid).collect { user ->
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
            _students.value = attendanceRepo.getStudentsForGuru(guruId)
        }
    }

    private fun checkAttendanceTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        _isAttendanceActive.value = hour >= 8
    }

    fun submitAttendanceGuru(attendances: List<AttendanceModel>) {
        viewModelScope.launch {
            _operationStatus.emit(attendanceRepo.saveAttendanceBatch(attendances))
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
            
            val attendance = AttendanceModel(
                studentId = user.uid,
                studentName = user.name,
                kelasId = user.kelasId ?: "",
                guruId = user.waliKelasId ?: "",
                status = finalStatus,
                date = date
            )
            
            _operationStatus.emit(attendanceRepo.submitStudentAttendance(attendance))
        }
    }
}
