package com.example.simsekolah.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference.getInstance(requireContext())

        setupHeader()
        setupMenu()
    }

    private fun setupHeader() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            if (user.isLogin) {
                // Mengupdate nama user dengan emoji lambaian tangan
                binding.tvUsername.text = "${user.name} 👋"
            }
        }
    }

    private fun setupMenu() {
        binding.apply {
            // Navigasi ke Assignments
            menuAssignments.setOnClickListener {
                findNavController().navigate(R.id.assignmentsFragment)
            }

            // Navigasi ke Event
            menuEvent.setOnClickListener {
                findNavController().navigate(R.id.eventFragment)
            }

            // Navigasi ke Information (sementara ke Fees atau sesuai nav_graph)
            menuInformation.setOnClickListener {
                findNavController().navigate(R.id.informasiFragment)
            }

            // Navigasi ke Notifikasi
            btnNotification.setOnClickListener {
                findNavController().navigate(R.id.notificationsFragment)
            }
            
            // Contoh aksi kalender (opsional)
            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Handle click kalender jika perlu
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
