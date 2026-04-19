package com.example.simsekolah.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.AbsensiItem
import com.example.simsekolah.SchoolRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _attendanceList = MutableLiveData<List<AbsensiItem>>()
    val attendanceList: LiveData<List<AbsensiItem>> = _attendanceList

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
                
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _postResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}
