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
            val emailInput = binding.etEmail.text.toString().trim()
            val passwordInput = binding.etPassword.text.toString().trim()

            if (emailInput.isEmpty()) {
                binding.etEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }
            if (passwordInput.isEmpty()) {
                binding.etPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            val selectedRoleId = binding.rgRole.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.rbGuru) "guru" else "siswa"

            viewModel.login(emailInput, passwordInput, role, this)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                userPreference.setUser(user)
                Toast.makeText(this, "Login successful as ${user.role}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        if (isLoading) {
            binding.btnSignIn.text = ""
            binding.btnSignIn.setBackgroundResource(R.drawable.bg_button_black)
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.text = getString(R.string.sign_in)
            binding.btnSignIn.setBackgroundResource(R.drawable.bg_button_black)
            binding.progressBar.visibility = View.GONE
        }
    }
}
