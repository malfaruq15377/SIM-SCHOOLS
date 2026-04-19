package com.example.simsekolah.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.simsekolah.UserPreference
import com.example.simsekolah.databinding.ActivitySettingBinding
import com.example.simsekolah.databinding.DialogChangePasswordBinding
import com.example.simsekolah.ui.auth.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var mUserPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mUserPreference = UserPreference(this)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnChangePasswordMenu.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val newPassword = dialogBinding.etNewPassword.text.toString().trim()
            val confirmPassword = dialogBinding.etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty()) {
                dialogBinding.tlNewPassword.error = "Enter new password"
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                dialogBinding.tlNewPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (confirmPassword != newPassword) {
                dialogBinding.tlConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            updatePassword(newPassword, dialog)
        }

        dialog.show()
    }

    private fun updatePassword(password: String, dialog: AlertDialog) {
        lifecycleScope.launch {
            Toast.makeText(this@SettingActivity, "Updating password...", Toast.LENGTH_SHORT).show()
            
            // Simulasi proses API
            delay(2000) 

            Toast.makeText(this@SettingActivity, "Password updated successfully!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        mUserPreference.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}