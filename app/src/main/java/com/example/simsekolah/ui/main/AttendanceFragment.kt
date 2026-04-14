package com.example.simsekolah.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.adapter.AttendanceAdapter
import com.example.simsekolah.databinding.FragmentAttendanceBinding
import com.example.simsekolah.ui.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.getInstance()
    }

    private lateinit var adapter: AttendanceAdapter

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
        setupRecyclerView()
        observeViewModel()
        setupAction()

        // Fetch data from API
        viewModel.fetchAttendance()
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        binding.tvCurrentTime.text = timeFormat.format(calendar.time)
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.attendanceList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAction() {
        binding.btnAttendance.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Absensi klik segera hadir!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
