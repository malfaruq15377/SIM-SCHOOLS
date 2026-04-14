package com.example.simsekolah.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.model.UserModel
import com.example.simsekolah.data.remote.repository.SchoolRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // C:/Users/PC-1/Documents/SIM sekolah/SIM Sekolah/SIM-Sekolah/app/src/main/java/com/example/simsekolah/ui/auth/LoginViewModel.kt

    fun login(username: String, passwordInput: String, role: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (role == "guru") {
                    repository.getGuru().collect { response ->
                        if (response.success) {
                            // Cari guru berdasarkan email atau NIP
                            val guru = response.data.find { it.email == username || it.nip == username }
                            if (guru != null) {
                                // Untuk bypass hash password sementara (untuk testing)
                                _loginResult.value = Result.success(UserModel(name = guru.nama, email = guru.email, role = "guru"))
                            } else {
                                _loginResult.value = Result.failure(Exception("Akun tidak ditemukan"))
                            }
                        }
                    }
                } else {
                    repository.getSiswa().collect { response ->
                        if (response.success) {
                            // GANTI it.nisn JADI it.email karena di API tidak ada nisn
                            val murid = response.data.find { it.email == username || it.nama.equals(username, ignoreCase = true) }
                            if (murid != null) {
                                _loginResult.value = Result.success(UserModel(name = murid.nama, email = murid.email, role = "murid"))
                            } else {
                                _loginResult.value = Result.failure(Exception("Akun murid tidak ditemukan"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
