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
                // Gunakan fungsi login yang sesuai dengan role yang dipilih
                val loginFlow = if (role.equals("guru", ignoreCase = true)) {
                    repository.login(username, password)
                } else {
                    repository.loginSiswa(username, password)
                }

                loginFlow.collect { response ->
                    val userData = response.data.user
                    
                    _loginResult.value = Result.success(UserModel(
                        name = userData.name,
                        email = userData.email,
                        role = userData.status, 
                        age = userData.kelasId ?: 0,
                        token = response.data.token,
                        noPhone = userData.phone,
                        address = userData.address,
                        dateOfBirth = userData.birthDate,
                        // Gunakan field major untuk menyimpan NIS atau NIP
                        major = userData.nis ?: userData.nip ?: "-",
                        fatherName = userData.parentName
                    ))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(Exception(e.message ?: "Login gagal, periksa koneksi atau kredensial Anda"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
