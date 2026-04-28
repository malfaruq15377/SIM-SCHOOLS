package com.example.simsekolah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.ActivityLoginBinding
import com.example.simsekolah.utils.ViewModelFactory
import com.example.simsekolah.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(this)

        setupAction()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnSignIn.setOnClickListener {
            val identifierInput = binding.etEmail.text.toString().trim()
            val passwordInput = binding.etPassword.text.toString().trim()

            // 1. Validasi field kosong
            if (identifierInput.isEmpty()) {
                binding.etEmail.error = getString(R.string.email_or_username_empty_error)
                Toast.makeText(this, getString(R.string.email_or_username_empty_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (passwordInput.isEmpty()) {
                binding.etPassword.error = getString(R.string.password_empty_error)
                Toast.makeText(this, getString(R.string.password_empty_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoleId = binding.rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbGuru) "guru" else "siswa"

            viewModel.login(identifierInput, passwordInput, role, this)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                // Cek apakah role yang login sesuai dengan RadioButton yang dipilih
                val selectedRoleId = binding.rgRole.checkedRadioButtonId
                val selectedRole = if (selectedRoleId == R.id.rbGuru) "guru" else "siswa"

                // Normalisasi role dari backend (kadang "aktif" di siswa, pastikan kita cek dengan benar)
                val userRole = user.role?.lowercase() ?: ""

                if ((selectedRole == "guru" && userRole.contains("guru")) ||
                    (selectedRole == "siswa" && (userRole.contains("siswa") || userRole.contains("aktif")))) {

                    userPreference.setUser(user)
                    Toast.makeText(this, getString(R.string.login_success, user.role), Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // 2. Validasi RadioButton tidak sesuai dengan akun
                    Toast.makeText(this, "Akun ini bukan terdaftar sebagai ${if (selectedRole == "guru") "Guru" else "Siswa"}", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }

            }.onFailure { exception ->
                // 3. Validasi Password/Username salah (dari backend)
                val errorMessage = exception.message ?: getString(R.string.login_failed)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        if (isLoading) {
            binding.btnSignIn.text = ""
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.text = getString(R.string.sign_in)
            binding.progressBar.visibility = View.GONE
        }
    }
}
