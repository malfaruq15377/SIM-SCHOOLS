package com.example.simsekolah.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("Firebase", "Login Anonim Berhasil")
                } else {
                    android.util.Log.e("Firebase", "Login Anonim Gagal: ${task.exception}")
                }
            }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginActivity,
                R.id.registerActivity,
                R.id.editScheduleFragment,
                    -> {
                    binding.bottomNavContainer.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavContainer.visibility = View.VISIBLE
                }
            }
        }

        // Handle navigation from notification if activity is already running
        intent?.let { handleNotificationIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val page = intent.getStringExtra("page")
        if (page == "assignments") {
            navController.navigate(R.id.assignmentsFragment)
        }
    }
}