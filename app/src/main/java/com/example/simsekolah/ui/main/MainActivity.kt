package com.example.simsekolah.ui.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.ActivityMainBinding
import com.example.simsekolah.utils.DailyReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPref: UserPreference

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDummyTaskNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.bottomNavigation.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        userPref = UserPreference(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginActivity, R.id.registerActivity, R.id.takeAttendanceFragment, R.id.notificationsFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (item.itemId == R.id.homeFragment && navController.currentDestination?.id != R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false)
            }
            handled
        }

        checkReminderPermission()
    }

    private fun checkReminderPermission() {
        if (!userPref.isReminderAsked()) {
            AlertDialog.Builder(this)
                .setTitle("Enable Notifications?")
                .setMessage("Allow the app to send notifications for tasks and school info?")
                .setPositiveButton("Allow") { _, _ ->
                    userPref.setReminderAsked(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scheduleDummyTaskNotification()
                    }
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun scheduleDummyTaskNotification() {
        val targetDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.APRIL)
            set(Calendar.DAY_OF_MONTH, 23)
            set(Calendar.HOUR_OF_DAY, 10)
        }
        val delay = targetDate.timeInMillis - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(this).enqueue(workRequest)
        }
    }
}
