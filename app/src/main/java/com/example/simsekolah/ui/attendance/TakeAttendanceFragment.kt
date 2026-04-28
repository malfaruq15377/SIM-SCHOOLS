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
import com.example.simsekolah.data.local.preference.UserPreference
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

        val userPref = UserPreference(requireContext())
        val user = userPref.getUser()
        
        // Menggunakan age sebagai kelasId (sesuai mapping yang ada di project)
        val kelasId = user.age 

        val sessionDate = arguments?.getString("session_date") ?: "Unknown Date"
        binding.tvSessionDate.text = sessionDate

        viewModel.fetchSiswa(kelasId)

        setupRecyclerView()
        observeViewModel()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSaveAttendance.setOnClickListener {
            // Implementasi simpan kolektif jika diperlukan
            Toast.makeText(requireContext(), "Attendance Saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // Fetch siswa berdasarkan kelasId guru (relasi)
        viewModel.fetchSiswa(kelasId)
    }

    private fun setupRecyclerView() {
        binding.rvStudentAttendance.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.siswaList.observe(viewLifecycleOwner) { students ->
            // Menampilkan maksimal 10 siswa yang berelasi dengan guru
            val limitStudents = students.take(10)
            adapter = StudentMarkingAdapter(limitStudents)
            binding.rvStudentAttendance.adapter = adapter
            
            val markingItems = limitStudents.map { siswa ->
                StudentMarkingAdapter.StudentMarkingItem(
                    name = siswa.name,
                    email = siswa.email,
                    status = if (siswa.password.length <= 2) siswa.password else "P" 
                    // Note: password di-copy status-nya oleh fetchSiswa di ViewModel
                )
            }
            binding.rvStudentAttendance.adapter = adapter
            
            if (limitStudents.isEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada siswa di kelas Anda", Toast.LENGTH_SHORT).show()
            }
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
