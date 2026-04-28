package com.example.simsekolah.ui.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(usernameInput: String, passwordInput: String, role: String, context: Context) {
        val username = usernameInput.trim()
        val password = passwordInput.trim()

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.login(role, username, password).collect { response ->
                    val userData = response.data?.user
                    val token = response.data?.token

                    if (userData == null || token == null) {
                        _loginResult.value = Result.failure(Exception(response.msg ?: response.message ?: "Login gagal"))
                        _isLoading.value = false
                        return@collect
                    }

                    _loginResult.value = Result.success(UserModel(
                        name = userData.name,
                        email = userData.email,
                        role = userData.status,
                        token = token,
                        noPhone = userData.phone,
                        address = userData.address,
                        dateOfBirth = userData.birthDate,
                        major = userData.nip ?: userData.nis ?: "",
                        fatherName = userData.parentName ?: "",
                        age = userData.kelasId ?: 0,
                        id = userData.id
                    ))
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(Exception("Login gagal: ${e.message}"))
                _isLoading.value = false
            }
        }
    }
}
