package com.example.simsekolah.ui

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Tetap gunakan ini untuk sinkronisasi awal
        binding.bottomNavigation.setupWithNavController(navController)

        // OVERRIDE Listener untuk menangani kasus sub-fragment
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // 1. Cek apakah kita beneran pindah menu atau cuma klik menu yang sama
            val handled = NavigationUI.onNavDestinationSelected(item, navController)

            // 2. Jika kita klik Home tapi saat ini lagi di sub-fragment (Event/Assignments/dll),
            // kita paksa balik ke Home asli.
            if (item.itemId == R.id.homeFragment && navController.currentDestination?.id != R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false)
            }

            handled
        }

        // Tambahkan ini juga supaya pas icon yang aktif diklik lagi, dia nge-reset stack
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.homeFragment) {
                // Balik ke root Home dan hapus semua fragment di atasnya
                navController.popBackStack(R.id.homeFragment, false)
            }
        }
    }
}