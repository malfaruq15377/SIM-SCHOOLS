package com.example.simsekolah.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var userPreference: UserPreference
    private var userRole: String = ""
    private var userId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference.getInstance(requireContext())

        setupUIByRole()
        setupClock()
    }

    private fun setupUIByRole() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            userRole = user.role.lowercase()
            userId = user.id

            if (userRole == "guru") {
                setupTeacherView()
            } else {
                setupStudentView()
            }
        }
    }

    private fun setupTeacherView() {
        binding.tvTitle.text = "Teacher Attendance"
        binding.tvSubtitle.text = "Select a day to take attendance"
        binding.btnAttendance.visibility = View.GONE
        binding.tvLocation.visibility = View.GONE
        binding.cardInfo.visibility = View.GONE

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        val adapter = AttendanceDayAdapter(days) { day ->
            findNavController().navigate(R.id.takeAttendanceFragment)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
        binding.tvHistoryHeader.text = "Attendance Days"
    }

    private fun setupStudentView() {
        binding.btnAttendance.setOnClickListener {
            handleStudentCheckIn()
        }
    }

    private fun handleStudentCheckIn() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (hour < 8) {
            Toast.makeText(requireContext(), "Absensi baru dibuka jam 08:00", Toast.LENGTH_SHORT).show()
            return
        }

        val status = if (hour == 8 && minute <= 30) "Present" else "Late"
        
        val options = arrayOf("Present (Auto)", "Permission", "Sick")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Pilih Status Kehadiran")
            .setItems(options) { _, which ->
                val finalStatus = when (which) {
                    1 -> "Permission"
                    2 -> "Sick"
                    else -> status
                }
                submitAttendance(finalStatus)
            }.show()
    }

    private fun submitAttendance(status: String) {
        Toast.makeText(requireContext(), "Absen berhasil: $status", Toast.LENGTH_SHORT).show()
    }

    private fun setupClock() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.tvCurrentTime.text = sdf.format(Date())
        binding.tvCurrentDate.text = dateSdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
