package com.example.simsekolah.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentScheduleBinding
import com.example.simsekolah.utils.ViewModelFactory

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ViewModelFactory.Companion.getInstance(requireContext())
    }

    private lateinit var dayScheduleAdapter: DayScheduleAdapter
    private lateinit var userPreference: UserPreference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())
        setupUI()
        setupRecyclerView()
        observeViewModel()

        // Ambil data user terlebih dahulu baru panggil fetch
        val user = userPreference.getUser()
        viewModel.fetchSchedule(user.role, user.age) // Panggil sekali saja dengan parameter yang benar
    }



    private fun setupUI() {
        val user = userPreference.getUser()
        if (user.role?.equals("guru", ignoreCase = true) == true) {
            binding.btnEditSchedule.visibility = View.VISIBLE
        } else {
            binding.btnEditSchedule.visibility = View.GONE
        }

        binding.btnEditSchedule.setOnClickListener {
            // Toast for now, as the edit activity might not exist yet
            Toast.makeText(requireContext(), "Fitur Edit Jadwal (Khusus Guru)", Toast.LENGTH_SHORT).show()
            // Intent to EditScheduleActivity if you have one:
            // startActivity(Intent(requireContext(), EditScheduleActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        dayScheduleAdapter = DayScheduleAdapter(emptyList())
        binding.rvDays.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dayScheduleAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.dayScheduleList.observe(viewLifecycleOwner) { list ->
            dayScheduleAdapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}