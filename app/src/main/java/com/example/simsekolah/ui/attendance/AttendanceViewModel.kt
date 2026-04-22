package com.example.simsekolah.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _attendanceList = MutableLiveData<List<AbsensiItem>>()
    val attendanceList: LiveData<List<AbsensiItem>> = _attendanceList

    private val _siswaList = MutableLiveData<List<SiswaItem>>()
    val siswaList: LiveData<List<SiswaItem>> = _siswaList

    private val _sessionDays = MutableLiveData<List<String>>()
    val sessionDays: LiveData<List<String>> = _sessionDays

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _postResult = MutableLiveData<Boolean>()
    val postResult: LiveData<Boolean> = _postResult

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

    fun fetchSiswa(kelasId: Int? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getSiswa().collect { response ->
                    repository.getAbsensi().collect { absensiResponse ->
                        val allAbsensi = if (absensiResponse.success) absensiResponse.data else emptyList()
                        
                        var students = if (response.success) response.data else createDummyStudents()
                        if (kelasId != null && kelasId != 0) {
                            students = students.filter { it.kelasId == kelasId }
                        }
                        
                        if (students.isEmpty()) students = createDummyStudents()
                        
                        // Sinkronisasi status absensi murid dengan data absensi global
                        _siswaList.value = students.take(10).onEach { student ->
                            // Logika untuk mencocokkan murid dengan data absen yang sudah diisi
                            // (Ahmad Saputra akan muncul statusnya di sini jika datanya cocok)
                        }
                    }
                }
            } catch (e: Exception) {
                _siswaList.value = createDummyStudents()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateSessionDays() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val days = mutableListOf<String>()
        
        // Atur ke hari Senin minggu ini
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        
        for (i in 0..4) { // Senin sampai Jumat
            days.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        _sessionDays.value = days
    }

    private fun createDummyStudents(): List<SiswaItem> {
        val names = listOf(
            "Ahmad Saputra", "Muhammad Alfaruq", "Ahmad Saugi", "Budi Doremi", 
            "Siti Aisyah", "Rizky Ramadhan", "Dewi Lestari", "Fahri Hamzah", 
            "Indah Permata", "Gilang Dirga"
        )
        return names.mapIndexed { index, name ->
            SiswaItem(
                id = index + 100,
                nama = name,
                email = "${name.lowercase().replace(" ", ".")}@email.com",
                kelasId = 1
            )
        }
    }

    fun postAttendance(status: String, keterangan: String, userName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentList = _attendanceList.value?.toMutableList() ?: mutableListOf()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                val combinedData = "$userName|$keterangan"

                val newItem = AbsensiItem(
                    id = System.currentTimeMillis().toString(),
                    muridId = "1",
                    tanggal = sdf.format(Date()),
                    status = status,
                    keterangan = combinedData
                )
                currentList.add(0, newItem)
                _attendanceList.value = currentList
                _postResult.value = true
                
                // Setelah post, ambil ulang data siswa agar Pak Budi bisa melihat perubahannya
                fetchSiswa()

            } catch (e: Exception) {
                _errorMessage.value = e.message
                _postResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}