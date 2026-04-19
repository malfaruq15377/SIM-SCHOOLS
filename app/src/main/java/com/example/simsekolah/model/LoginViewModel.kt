package com.example.simsekolah.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.SchoolRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(usernameInput: String, passwordInput: String, role: String) {
        val username = usernameInput.trim()
        val password = passwordInput.trim()

        if (username.isEmpty()) {
            _loginResult.value = Result.failure(Exception("Email tidak boleh kosong"))
            return
        }
        if (password.isEmpty()) {
            _loginResult.value = Result.failure(Exception("Password tidak boleh kosong"))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (role.equals("siswa", ignoreCase = true)) {
                    repository.loginSiswa(username, password).collect { response ->
                        if (response.data.token.isNotEmpty()) {
                            _loginResult.value = Result.success(
                                UserModel(
                                    name = response.data.user.name,
                                    email = response.data.user.email,
                                    role = "siswa"
                                )
                            )
                        } else {
                            _loginResult.value = Result.failure(Exception("Login gagal: Token kosong"))
                        }
                    }
                } else {
                    // Implementasi login guru jika ada endpointnya, sementara pakai logic lama atau sesuaikan
                    _loginResult.value = Result.failure(Exception("Login untuk role $role belum diimplementasikan dengan API"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(Exception("Login gagal: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}