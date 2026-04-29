package com.example.simsekolah.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.ActivitySettingBinding
import com.example.simsekolah.databinding.DialogChangePasswordBinding
import com.example.simsekolah.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userPreference = UserPreference.getInstance(this)

        setupAction()
    }

    private fun setupAction() {
        binding.apply {
            btnBack.setOnClickListener { finish() }
            
            btnChangePasswordMenu.setOnClickListener {
                showChangePasswordDialog()
            }
            
            btnLogout.setOnClickListener {
                showLogoutConfirmation()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            val newPass = dialogBinding.etNewPassword.text.toString()
            if (newPass.isNotEmpty()) {
                Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    userPreference.logout()
                    val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
