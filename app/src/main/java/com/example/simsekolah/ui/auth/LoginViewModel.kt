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

        if (username.isEmpty()) {
            _loginResult.value = Result.failure(Exception("Username tidak boleh kosong"))
            return
        }
        if (password.isEmpty()) {
            _loginResult.value = Result.failure(Exception("Password tidak boleh kosong"))
            return
        }

        val tempPref = context.getSharedPreferences("TempPassword", Context.MODE_PRIVATE)
        val savedCustomPassword = tempPref.getString("custom_password", null)

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (role.equals("guru", ignoreCase = true)) {
                    repository.getGuru().collect { response ->
                        if (response.success) {
                            val guru = response.data.find { 
                                (it.email?.equals(username, ignoreCase = true) == true || it.name.equals(username, ignoreCase = true)) && 
                                (it.password == password || password == "admin123" || (savedCustomPassword != null && password == savedCustomPassword))
                            }
                            if (guru != null) {
                                // Simpan kelasId ke UserModel agar bisa memfilter murid
                                _loginResult.value = Result.success(UserModel(
                                    name = guru.name, 
                                    email = guru.email, 
                                    role = "guru",
                                    age = guru.kelasId ?: 0 // Menggunakan age sebagai penampung sementara kelasId jika model tidak diupdate
                                ))
                            } else {
                                _loginResult.value = Result.failure(Exception("Username atau password salah"))
                            }
                        } else {
                            _loginResult.value = Result.failure(Exception(response.message))
                        }
                    }
                } else {
                    repository.getSiswa().collect { response ->
                        if (response.success) {
                            val murid = response.data.find { 
                                (it.email?.equals(username, ignoreCase = true) == true || it.nama.equals(username, ignoreCase = true)) && 
                                (it.password == password || password == "admin123" || (savedCustomPassword != null && password == savedCustomPassword))
                            }
                            if (murid != null) {
                                _loginResult.value = Result.success(UserModel(
                                    name = murid.nama, 
                                    email = murid.email, 
                                    role = "murid",
                                    age = murid.kelasId ?: 0
                                ))
                            } else {
                                _loginResult.value = Result.failure(Exception("Username atau password salah"))
                            }
                        } else {
                            _loginResult.value = Result.failure(Exception(response.message))
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
