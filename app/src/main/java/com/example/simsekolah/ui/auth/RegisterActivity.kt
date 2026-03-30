package com.example.simsekolah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
           val username = binding.etUsername.text.toString().trim()
           val email = binding.etEmail.text.toString().trim()
           val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua field", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
                Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
            }
        }
    }
}