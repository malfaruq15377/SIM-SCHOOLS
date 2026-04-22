package com.example.simsekolah.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.databinding.FragmentTeacherTakeAttendanceBinding
import com.example.simsekolah.utils.ViewModelFactory

class TakeAttendanceFragment : Fragment() {

    private var _binding: FragmentTeacherTakeAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        ViewModelFactory.Companion.getInstance(requireContext())
    }

    private lateinit var adapter: StudentMarkingAdapter

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
        observeViewModel()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveAttendance.setOnClickListener {
            Toast.makeText(requireContext(), "Attendance Saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // Ambil data siswa (wali murid Pak Budi)
        viewModel.fetchSiswa()
    }

    private fun setupRecyclerView() {
        binding.rvStudentAttendance.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.siswaList.observe(viewLifecycleOwner) { students ->
            // Map SiswaItem ke StudentMarkingItem
            val markingItems = students.map { siswa ->
                StudentMarkingAdapter.StudentMarkingItem(
                    name = siswa.nama,
                    email = siswa.email ?: "-",
                    status = "" // Default Kosong sesuai permintaan
                )
            }
            adapter = StudentMarkingAdapter(markingItems)
            binding.rvStudentAttendance.adapter = adapter
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}