package com.example.simsekolah.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.simsekolah.data.local.UserPreference
import com.example.simsekolah.databinding.FragmentAssignmentsBinding

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAssignmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val userPref = UserPreference(requireContext())
        val user = userPref.getUser()

        // Logika pemisahan fitur berdasarkan role
        if (user.role == "guru") {
            binding.btnAdd.visibility = View.VISIBLE // Guru bisa melihat tombol tambah
            binding.btnAdd.setOnClickListener {
                // Tambahkan aksi untuk guru di sini
            }
        } else {
            binding.btnAdd.visibility = View.GONE // Murid tidak bisa melihat tombol tambah
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}