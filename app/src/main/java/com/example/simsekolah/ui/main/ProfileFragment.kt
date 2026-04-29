package com.example.simsekolah.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.remote.retrofit.ApiConfig
import com.example.simsekolah.databinding.FragmentProfileBinding
import com.example.simsekolah.ui.auth.LoginActivity
import com.example.simsekolah.ui.settings.SettingActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference.getInstance(requireContext())

        setupUserData()
        setupAction()
    }

    private fun setupUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            binding.apply {
                tvDisplayName.text = user.name
                tvNama.text = user.name
                tvEmail.text = user.email
                tvPhone.text = user.phone
                tvAddress.text = user.address
                tvStatusBadge.text = "Aktif"
                tvMajorBadge.text = user.gender ?: "-"

                if (user.role == "guru") {
                    tvDisplayRole.text = if (user.isWaliKelas) "Wali Kelas" else "Guru Pengajar"
                    tvDisplayMajor.text = "NIP: ${user.extraInfo}"
                    tvClassBadge.text = if (user.isWaliKelas) "Wali Kelas" else "Pengajar"
                    
                    if (user.isWaliKelas) {
                        layoutWaliKelas.visibility = View.VISIBLE
                        // Untuk guru wali kelas, dia membimbing kelasnya sendiri
                        tvWaliKelas.text = "Membimbing Kelas ${user.kelasId ?: "-"}"
                    }
                } else {
                    tvDisplayRole.text = "Siswa"
                    tvDisplayMajor.text = "NIS: ${user.extraInfo}"
                    tvClassBadge.text = "Kelas ${user.kelasId ?: "-"}"
                    
                    // CARA LIHAT WALI KELAS UNTUK SISWA
                    layoutWaliKelas.visibility = View.VISIBLE
                    fetchWaliKelasName(user.kelasId ?: 0)
                }
            }
        }
    }

    private fun fetchWaliKelasName(kelasId: Int) {
        if (kelasId == 0) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService(requireContext())
                val kelasResponse = apiService.getKelas()
                val targetKelas = kelasResponse.data.find { it.id == kelasId }
                
                if (targetKelas != null) {
                    val guruResponse = apiService.getGuru()
                    val waliKelas = guruResponse.data.find { it.id == targetKelas.waliKelasId }
                    binding.tvWaliKelas.text = waliKelas?.name ?: "Belum Ditentukan"
                }
            } catch (e: Exception) {
                binding.tvWaliKelas.text = "Gagal memuat data"
            }
        }
    }

    private fun setupAction() {
        binding.apply {
            btnBack.setOnClickListener { findNavController().navigateUp() }
            ivSetting.setOnClickListener {
                startActivity(Intent(requireContext(), SettingActivity::class.java))
            }
            btnUpdate.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    userPreference.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
