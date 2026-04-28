package com.example.simsekolah.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _postResult = MutableLiveData<Boolean>()
    val postResult: LiveData<Boolean> = _postResult

    private val database = FirebaseDatabase.getInstance("https://simsekolah-68fa2039-default-rtdb.firebaseio.com/").reference

    fun fetchAttendance(role: String, email: String) {
        _isLoading.value = true
        database.child("absensi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AbsensiItem>()
                for (data in snapshot.children) {
                    val item = data.getValue(AbsensiItem::class.java)
                    if (item != null) {
                        if (role.equals("guru", ignoreCase = true)) {
                            if (item.keterangan?.contains(email) == true) list.add(item)
                        } else {
                            if (item.muridId == email) list.add(item)
                        }
                    }
                }
                _attendanceList.value = list.sortedByDescending { it.tanggal }
                _isLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _errorMessage.value = error.message
            }
        })
    }

    fun fetchSiswa(kelasId: Int? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getSiswa().collect { response ->
                    if (response.success == true && response.data != null) {
                        database.child("absensi").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val allAbsensi = mutableListOf<AbsensiItem>()
                                for (data in snapshot.children) {
                                    val item = data.getValue(AbsensiItem::class.java)
                                    if (item != null) allAbsensi.add(item)
                                }

                                val students = response.data
                                val filteredStudents = if (kelasId != null && kelasId != 0) {
                                    students.filter { it.kelasId == kelasId }
                                } else {
                                    students
                                }

                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val updatedStudents = filteredStudents.map { student ->
                                    val studentAbsen = allAbsensi.find { 
                                        it.muridId == student.email && it.tanggal.startsWith(today) 
                                    }
                                    // Menggunakan status absen hari ini sebagai password sementara (untuk dibaca adapter)
                                    student.copy(status = studentAbsen?.status ?: "Belum Absen")
                                }
                                
                                _siswaList.value = updatedStudents
                                _isLoading.value = false
                            }
                            override fun onCancelled(error: DatabaseError) { 
                                _isLoading.value = false
                                _errorMessage.value = error.message
                            }
                        })
                    } else {
                        _errorMessage.value = response.message ?: response.msg ?: "Gagal mengambil data siswa"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
                _isLoading.value = false
            }
        }
    }

    fun generateSessionDays() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val days = mutableListOf<String>()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0..4) {
            days.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        _sessionDays.value = days
    }

    fun postAttendance(status: String, keterangan: String, userName: String, userEmail: String, teacherEmail: String) {
        _isLoading.value = true
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val id = System.currentTimeMillis().toString()
        
        val newItem = AbsensiItem(
            id = id,
            muridId = userEmail,
            tanggal = sdf.format(Date()),
            status = status,
            keterangan = "$userName|$keterangan|$teacherEmail"
        )

        database.child("absensi").child(id).setValue(newItem)
            .addOnSuccessListener {
                _postResult.value = true
                _isLoading.value = false
            }
            .addOnFailureListener {
                _postResult.value = false
                _isLoading.value = false
                _errorMessage.value = it.message
            }
    }
}
