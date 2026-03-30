package com.example.simsekolah.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.databinding.ActivityLoginBinding
import com.example.simsekolah.ui.main.MainActivity
import com.example.simsekolah.ui.auth.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        binding.btnSignIn.setOnClickListener {
            val usernameInput = binding.etUsername.text.toString()
            val passwordInput = binding.etPassword.text.toString()

            if (usernameInput.isEmpty()) {
                binding.etUsername.error = "Username tidak boleh kosong"
            } else if (passwordInput.isEmpty()) {
                binding.etPassword.error = "Password tidak boleh kosong"
            }
            else if (usernameInput != username || passwordInput != password) {
                binding.etUsername.error = "Username atau password salah"
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        binding.createAccountRegis.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}