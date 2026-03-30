package com.example.simsekolah.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.simsekolah.R
import com.example.simsekolah.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. PINDAHKAN INI KE ATAS: Inflate dan Set Content View harus pertama kali
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. SETUP Navigation Component
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Hubungkan Bottom Navigation dengan NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Override Listener untuk menangani navigasi khusus (jika diperlukan)
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
    }
}