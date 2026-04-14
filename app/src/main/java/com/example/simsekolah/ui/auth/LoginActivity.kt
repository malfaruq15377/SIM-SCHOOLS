package com.example.simsekolah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.R
import com.example.simsekolah.data.local.UserPreference
import com.example.simsekolah.databinding.ActivityLoginBinding
import com.example.simsekolah.ui.ViewModelFactory
import com.example.simsekolah.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(this)

        // Sembunyikan navigasi register sesuai permintaan
        binding.layoutRegister.visibility = View.GONE

        setupAction()
        observeViewModel()

        binding.btnSignIn.setOnClickListener {
            val usernameInput = binding.etUsername.text.toString()
            val passwordInput = binding.etPassword.text.toString()
            // ... (validasi empty)

            val selectedRoleId = binding.rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbGuru) "guru" else "murid"

            // Kirim passwordInput juga
            viewModel.login(usernameInput, passwordInput, role)
        }
    }

    private fun setupAction() {
        binding.btnSignIn.setOnClickListener {
            val usernameInput = binding.etUsername.text.toString()
            val passwordInput = binding.etPassword.text.toString()

            if (usernameInput.isEmpty()) {
                binding.etUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (passwordInput.isEmpty()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            // Ambil role dari RadioGroup
            val selectedRoleId = binding.rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbGuru) "guru" else "murid"

            viewModel.login(usernameInput, role, role)
        }

        binding.createAccountRegis.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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
                Toast.makeText(this, "Role yang kamu masukan tidak sesuai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        // Jika ada progress bar di layout bisa ditambahkan di sini
    }
}
