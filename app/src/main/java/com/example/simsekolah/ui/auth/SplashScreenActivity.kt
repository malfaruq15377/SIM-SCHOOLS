package com.example.simsekolah.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.simsekolah.R
import com.example.simsekolah.data.local.UserPreference

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val userPreference = UserPreference(this)
        val user = userPreference.getUser()

        Handler(Looper.getMainLooper()).postDelayed({
            // Cek apakah user sudah login atau belum
            // Jika role kosong, artinya belum login, arahkan ke LoginActivity
            if (user.role.isNullOrEmpty()) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Jika sudah ada data user (sudah login), langsung ke MainActivity
                val intent = Intent(this, com.example.simsekolah.ui.main.MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 2000)
    }
}