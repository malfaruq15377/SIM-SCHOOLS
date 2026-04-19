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
            _loginResult.value = Result.failure(Exception("Username tidak boleh kosong"))
            return
        }
        if (password.isEmpty()) {
            _loginResult.value = Result.failure(Exception("Password tidak boleh kosong"))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Menggunakan repository.loginSiswa yang sekarang mengembalikan LoginResponse
                repository.loginSiswa(username, password).collect { response ->
                    val loginData = response.data
                    if (loginData.token.isNotEmpty()) {
                        val userData = loginData.user
                        
                        // Validasi role jika diperlukan, atau langsung gunakan role dari API
                        val userRole = userData.role ?: role
                        
                        _loginResult.value = Result.success(
                            UserModel(
                                name = userData.name,
                                email = userData.email,
                                role = userRole,
                                token = loginData.token
                            )
                        )
                    } else {
                        _loginResult.value = Result.failure(Exception("Login gagal: Token tidak ditemukan"))
                    }
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(Exception("Login gagal: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}