package com.example.simsekolah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.remote.retrofit.ApiConfig
import com.example.simsekolah.databinding.ActivityLoginBinding
import com.example.simsekolah.model.UserModel
import com.example.simsekolah.ui.main.MainActivity
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this)

        setupAction()
    }

    private fun setupAction() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val isGuru = binding.rbGuru.isChecked

            if (email.isEmpty()) {
                binding.etEmail.error = "Email/Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            loginProcess(email, password, isGuru)
        }
    }

    private fun loginProcess(email: String, password: String, isGuru: Boolean) {
        showLoading(true)
        
        val requestBody = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                
                if (isGuru) {
                    val response = apiService.loginGuru(requestBody)
                    val user = UserModel(
                        id = response.data.user.id,
                        name = response.data.user.name,
                        email = response.data.user.email,
                        phone = response.data.user.phone,
                        address = response.data.user.address,
                        role = "guru",
                        token = response.data.token,
                        extraInfo = response.data.user.nip
                    )
                    userPreference.saveSession(user)
                    navigateToHome()
                } else {
                    val response = apiService.loginSiswa(requestBody)
                    val user = UserModel(
                        id = response.data.user.id,
                        name = response.data.user.name,
                        email = response.data.user.email,
                        phone = response.data.user.phone,
                        address = response.data.user.address,
                        role = "siswa",
                        token = response.data.token,
                        extraInfo = response.data.user.nis
                    )
                    userPreference.saveSession(user)
                    navigateToHome()
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    if (errorBody != null && errorBody.startsWith("{")) {
                        JSONObject(errorBody).getString("msg")
                    } else {
                        "Gagal login: Respon server tidak valid (${e.code()})"
                    }
                } catch (jsonEx: Exception) {
                    "Gagal memproses data error dari server"
                }
                showToast(message)
                showLoading(false)
            } catch (e: Exception) {
                showToast("Koneksi gagal: ${e.localizedMessage}")
                showLoading(false)
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignIn.text = ""
            binding.btnSignIn.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.text = getString(R.string.sign_in)
            binding.btnSignIn.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
