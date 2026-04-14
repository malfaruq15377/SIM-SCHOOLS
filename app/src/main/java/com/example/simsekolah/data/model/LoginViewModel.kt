package com.example.simsekolah.data.model

import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.repository.SchoolRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    fun login(username: String, passwordInput: String, role: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (role == "guru") {
                    repository.getGuru().collect { response ->
                        if (response.success) {
                            // Cari guru yang email/nip cocok DAN password cocok
                            // Catatan: Jika password di API di-hash, ini butuh penyesuaian
                            val guru = response.data.find {
                                (it.email == username || it.nip == username) && it.password == passwordInput
                            }
                            if (guru != null) {
                                _loginResult.value = Result.success(UserModel(name = guru.nama, email = guru.email, role = "guru"))
                            } else {
                                _loginResult.value = Result.failure(Exception("Email atau Password salah"))
                            }
                        }
                    }
                } else {
                    repository.getSiswa().collect { response ->
                        if (response.success) {
                            // Cari murid yang email/nama cocok DAN password cocok
                            val murid = response.data.find {
                                (it.email == username || it.nama.equals(username, ignoreCase = true)) && it.password == passwordInput
                            }
                            if (murid != null) {
                                _loginResult.value = Result.success(UserModel(name = murid.nama, email = murid.email, role = "murid"))
                            } else {
                                _loginResult.value = Result.failure(Exception("Email atau Password salah"))
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