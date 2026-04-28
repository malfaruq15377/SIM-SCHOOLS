package com.example.simsekolah.ui.assignment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.launch

class AssignmentsViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _siswaList = MutableLiveData<List<SiswaItem>>()
    val siswaList: LiveData<List<SiswaItem>> = _siswaList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String> = _errorMessage as LiveData<String>

    fun fetchSiswa() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getSiswa().collect { response ->
                    if (response.success == true) {
                        // Ambil 10 siswa saja sesuai permintaan
                        _siswaList.value = response.data?.take(10)
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

    fun fetchGuru() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getGuru().collect { response ->
                    if (response.success == true) {
                        // Ambil 10 guru saja sesuai permintaan
                        _siswaList.value = response.data?.take(10) as List<SiswaItem>?
                    } else {
                        _errorMessage.value = response.message
                    }
                }
                    }
            catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
