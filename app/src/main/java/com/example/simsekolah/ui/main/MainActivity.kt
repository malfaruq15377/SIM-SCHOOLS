package com.example.simsekolah.ui.main

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
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
            Toast.makeText(this, "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifikasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mengaktifkan Mode Edge-to-Edge agar konten bisa di bawah status bar/nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Menangani Insets (Status Bar & Navigation Bar) agar konten tidak terhalang kamera/notch
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Memberikan padding atas otomatis sesuai tinggi Status Bar (menghindari kamera depan)
            binding.navHostFragment.updatePadding(top = insets.top)
            
            // Memberikan padding bawah agar konten tidak terhalang Bottom Navigation yang melayang
            // (Padding disesuaikan agar item terakhir di list bisa di-scroll sampai ke atas BottomNav)
            binding.navHostFragment.updatePadding(bottom = insets.bottom)
            
            windowInsets
        }

        userPref = UserPreference(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Kontrol visibilitas Bottom Nav berdasarkan fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginActivity, R.id.registerActivity, R.id.takeAttendanceFragment -> {
                    binding.bottomNavContainer.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavContainer.visibility = View.VISIBLE
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

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        checkReminderPermission()
    }

    private fun checkReminderPermission() {
        if (!userPref.isReminderAsked()) {
            AlertDialog.Builder(this)
                .setTitle("Aktifkan Pengingat?")
                .setMessage("Apakah Anda ingin menerima notifikasi pengingat tugas harian?")
                .setPositiveButton("Ya") { _, _ ->
                    userPref.setReminderAsked(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scheduleDummyTaskNotification()
                    }
                }
                .setNegativeButton("Nanti") { _, _ ->
                    userPref.setReminderAsked(true)
                }
                .show()
        }
    }

    private fun scheduleDummyTaskNotification() {
        val targetDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.APRIL)
            set(Calendar.DAY_OF_MONTH, 23)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val delay = targetDate.timeInMillis - System.currentTimeMillis()

        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("dummy_task_reminder")
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)
            Toast.makeText(this, "Reminder dijadwalkan untuk 23 April 2026 jam 10:00", Toast.LENGTH_LONG).show()
        }
    }
}
