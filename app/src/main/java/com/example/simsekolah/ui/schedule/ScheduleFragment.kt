package com.example.simsekolah.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentScheduleBinding
import com.example.simsekolah.utils.ViewModelFactory
import com.google.gson.Gson

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
        setupRecyclerView()
        observeViewModel()

        val user = userPreference.getUser()
        viewModel.fetchSchedule(requireContext(), user.role, user.age)
    }

    private fun setupRecyclerView() {
        val user = userPreference.getUser()
        val isGuru = user.role?.equals("guru", ignoreCase = true) == true

        dayScheduleAdapter = DayScheduleAdapter(
            daySchedules = emptyList(),
            isGuru = isGuru,
            onEditClicked = { dayData ->
                navigateToEditSchedule(dayData)
            }
        )
        binding.rvDays.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dayScheduleAdapter
        }
    }

    private fun navigateToEditSchedule(dayData: DayScheduleAdapter.DaySchedule) {
        val bundle = Bundle().apply {
            putString("day_name", dayData.dayName)
            putString("schedule_json", Gson().toJson(dayData.items))
        }
        findNavController().navigate(R.id.action_scheduleFragment_to_editScheduleFragment, bundle)
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