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
                tvDisplayName.text = user.name
                tvDisplayRole.text = if (user.role == "guru") "Guru Pengajar" else "Siswa"
                tvDisplayMajor.text = if (user.role == "guru") "NIP: ${user.extraInfo}" else "NIS: ${user.extraInfo}"
                
                tvNama.text = user.name
                tvEmail.text = user.email
                tvPhone.text = user.phone
                tvAddress.text = user.address
                
                tvStatusBadge.text = "Aktif"
                // Misal kita set kelas badge dari extraInfo atau default
                tvClassBadge.text = if (user.role == "siswa") "XII-A" else "Guru" 
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
                // Logout logic for testing or update profile
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
