package com.example.simsekolah.ui.auth

import android.content.Intent
import android.graphics.Color
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
            val usernameInput = binding.etUsername.text.toString().trim()
            val passwordInput = binding.etPassword.text.toString().trim()

            if (usernameInput.isEmpty()) {
                binding.etUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (passwordInput.isEmpty()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            val selectedRoleId = binding.rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbGuru) "guru" else "siswa"

            viewModel.login(usernameInput, passwordInput, role, this)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                userPreference.setUser(user)
                Toast.makeText(this, "Login berhasil sebagai ${user.role}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
                // Jika gagal, pastikan tombol kembali normal
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        if (isLoading) {
            binding.btnSignIn.text = "" // Hilangkan teks saat loading
            binding.btnSignIn.setBackgroundResource(R.drawable.bg_button_black) // Ganti warna jadi abu
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.text = getString(R.string.sign_in)
            binding.btnSignIn.setBackgroundResource(R.drawable.bg_button_black) // Kembalikan warna
            binding.progressBar.visibility = View.GONE
        }
    }
}
