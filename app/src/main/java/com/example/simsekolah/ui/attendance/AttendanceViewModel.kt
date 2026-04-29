package com.example.simsekolah.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.data.remote.response.BaseResponse
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<SiswaItem>>(emptyList())
    val students: StateFlow<List<SiswaItem>> = _students.asStateFlow()

    private val _attendanceHistory = MutableStateFlow<List<AbsensiItem>>(emptyList())
    val attendanceHistory: StateFlow<List<AbsensiItem>> = _attendanceHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _attendanceResult = MutableSharedFlow<Result<BaseResponse<AbsensiItem>>>()
    val attendanceResult: SharedFlow<Result<BaseResponse<AbsensiItem>>> = _attendanceResult.asSharedFlow()

    private val _bulkAttendanceResult = MutableSharedFlow<Result<BaseResponse<String>>>()
    val bulkAttendanceResult: SharedFlow<Result<BaseResponse<String>>> = _bulkAttendanceResult.asSharedFlow()

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

    fun loadAttendanceHistory(siswaId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.getAbsensi(siswaId)
                .catch { _attendanceHistory.value = emptyList() }
                .collect { response ->
                    _attendanceHistory.value = response.data
                }
            _isLoading.value = false
        }
    }

    fun submitAttendance(siswaId: Int, status: String, tanggal: String, jamMasuk: String?, keterangan: String?, metode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.postAbsensi(siswaId, status, tanggal, jamMasuk, keterangan, metode)
                .catch { e ->
                    _attendanceResult.emit(Result.failure(e))
                }
                .collect { response ->
                    _attendanceResult.emit(Result.success(response))
                    loadAttendanceHistory(siswaId)
                }
            _isLoading.value = false
        }
    }

    fun saveBulkAttendance(data: List<StudentAttendanceAdapter.AttendancePostData>) {
        viewModelScope.launch {
            _isLoading.value = true
            val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attendanceJsonList = data.map {
                JsonObject().apply {
                    addProperty("siswaId", it.siswaId)
                    addProperty("status", it.status)
                    addProperty("tanggal", tanggal)
                    addProperty("metode", "guru")
                    addProperty("divalidasi", true)
                }
            }

            schoolRepo.postAbsensiBulk(attendanceJsonList)
                .catch { e ->
                    _bulkAttendanceResult.emit(Result.failure(e))
                }
                .collect { response ->
                    _bulkAttendanceResult.emit(Result.success(response))
                }
            _isLoading.value = false
        }
    }
}
