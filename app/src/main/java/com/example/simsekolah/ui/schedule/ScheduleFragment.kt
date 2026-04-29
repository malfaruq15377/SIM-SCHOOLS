package com.example.simsekolah.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.databinding.FragmentScheduleBinding
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.launch

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
        setupObservers()
    }

    private fun setupRecyclerView() {
        // We'll update the adapter once we know the user role
        scheduleAdapter = ScheduleAdapter(false) { schedule ->
            val bundle = Bundle().apply {
                putParcelable("schedule", schedule)
            }
            findNavController().navigate(R.id.editScheduleFragment, bundle)
        }
        
        binding.rvDays.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        user?.let {
                            val isGuru = it.role == "guru"
                            // Re-initialize adapter with correct role if it changes
                            scheduleAdapter = ScheduleAdapter(isGuru) { schedule ->
                                val bundle = Bundle().apply {
                                    putParcelable("schedule", schedule)
                                }
                                findNavController().navigate(R.id.editScheduleFragment, bundle)
                            }
                            binding.rvDays.adapter = scheduleAdapter
                            // Re-submit current list to new adapter
                            scheduleAdapter.submitList(viewModel.schedules.value)
                        }
                    }
                }
                
                launch {
                    viewModel.schedules.collect { list ->
                        binding.progressBar.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        scheduleAdapter.submitList(list)
                        if (list.isNotEmpty()) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
