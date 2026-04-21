package com.example.simsekolah.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.databinding.FragmentTeacherTakeAttendanceBinding

class TakeAttendanceFragment : Fragment() {

    private var _binding: FragmentTeacherTakeAttendanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherTakeAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionDate = arguments?.getString("session_date") ?: "Unknown Date"
        binding.tvSessionDate.text = sessionDate

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveAttendance.setOnClickListener {
            Toast.makeText(requireContext(), "Attendance Saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.rvStudentAttendance.layoutManager = LinearLayoutManager(requireContext())
        
        // Simulasi List Murid
        val students = listOf(
            StudentMarkingAdapter.StudentMarkingItem("Muhammad Alfaruq", "alfaruq@email.com"),
            StudentMarkingAdapter.StudentMarkingItem("Ahmad Saugi", "saugi@email.com"),
            StudentMarkingAdapter.StudentMarkingItem("Budi Doremi", "budi@email.com")
        )
        binding.rvStudentAttendance.adapter = StudentMarkingAdapter(students)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
