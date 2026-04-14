package com.example.simsekolah.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.adapter.ScheduleAdapter
import com.example.simsekolah.databinding.FragmentScheduleBinding
import com.example.simsekolah.ui.ViewModelFactory
import com.google.android.material.chip.Chip

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ViewModelFactory.getInstance()
    }

    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChipGroup()
        observeViewModel()

        viewModel.fetchSchedule()
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(emptyList())
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChipGroup() {
        binding.chipGroupDays.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val day = chip.text.toString()
                viewModel.filterByDay(day)
            } else {
                viewModel.filterByDay("All")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.scheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
