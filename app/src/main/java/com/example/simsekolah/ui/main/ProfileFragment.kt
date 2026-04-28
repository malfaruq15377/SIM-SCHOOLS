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
                // Nama di bagian atas
                tvDisplayName.text = user.name
                
                // Logika Role: Guru Pengajar atau Wali Kelas
                if (user.role == "guru") {
                    tvDisplayRole.text = if (user.isWaliKelas) "Wali Kelas" else "Guru Pengajar"
                    tvDisplayMajor.text = "NIP: ${user.extraInfo}"
                    tvClassBadge.text = if (user.isWaliKelas) "Wali" else "Guru"
                    
                    // Tampilkan label Wali Kelas di card info jika true
                    if (user.isWaliKelas) {
                        layoutWaliKelas.visibility = View.VISIBLE
                        tvWaliKelas.text = "Ya (Wali Kelas)"
                    } else {
                        layoutWaliKelas.visibility = View.GONE
                    }
                } else {
                    tvDisplayRole.text = "Siswa"
                    tvDisplayMajor.text = "NIS: ${user.extraInfo}"
                    tvClassBadge.text = "Siswa" // Bisa diganti logic kelas jika ada datanya
                    layoutWaliKelas.visibility = View.GONE
                }
                
                // Isi bagian Identitas (Badge kanan)
                tvMajorBadge.text = user.gender ?: "-"
                
                // Isi Card Informasi Detail
                tvNama.text = user.name
                tvEmail.text = user.email
                tvPhone.text = user.phone
                tvAddress.text = user.address
                
                tvStatusBadge.text = "Aktif"
            }
        }
    }

    private fun setupAction() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            ivSetting.setOnClickListener {
                val intent = Intent(requireContext(), SettingActivity::class.java)
                startActivity(intent)
            }
            
            btnUpdate.setOnClickListener {
                // Temporary logout for testing
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
