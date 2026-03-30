package com.example.simsekolah.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.adapter.AttendanceAdapter
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.model.AttendanceHistory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDateTime()
        setupHistoryList()
        setupAction()
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        
        // Format Tanggal: Senin, 20 Mei 2024
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        // Format Jam: 08:00 AM
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        binding.tvCurrentTime.text = timeFormat.format(calendar.time)
    }

    private fun setupHistoryList() {
        // Mock Data Riwayat
        val list = listOf(
            AttendanceHistory("19 Mei 2024", "07:15", "Hadir Tepat Waktu"),
            AttendanceHistory("18 Mei 2024", "07:20", "Hadir Tepat Waktu"),
            AttendanceHistory("17 Mei 2024", "07:45", "Terlambat"),
            AttendanceHistory("16 Mei 2024", "07:10", "Hadir Tepat Waktu")
        )

        val adapter = AttendanceAdapter(list)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupAction() {
        binding.btnAttendance.setOnClickListener {
            // Simulasi Absen
            binding.tvStatus.apply {
                text = "Hadir"
                setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            
            Toast.makeText(requireContext(), "Absensi Berhasil!", Toast.LENGTH_SHORT).show()
            
            // Disable button setelah absen (simulasi)
            binding.btnAttendance.isEnabled = false
            binding.btnAttendance.alpha = 0.5f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}