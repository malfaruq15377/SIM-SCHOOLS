package com.example.simsekolah.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.databinding.FragmentTeacherTakeAttendanceBinding
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TakeAttendanceFragment : Fragment() {
    private var _binding: FragmentTeacherTakeAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var adapter: StudentAttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherTakeAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupHeader()
        observeViewModel()
        
        viewModel.loadStudents() // Need to implement this in ViewModel
    }

    private fun setupRecyclerView() {
        adapter = StudentAttendanceAdapter()
        binding.rvStudentAttendance.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudentAttendance.adapter = adapter
        
        binding.btnSaveAttendance.setOnClickListener {
            val data = adapter.getAttendanceData()
            // Logic to save all attendance
            Toast.makeText(requireContext(), "Attendance saved for ${data.size} students", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHeader() {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.tvSessionDate.text = sdf.format(Date())
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.students.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
