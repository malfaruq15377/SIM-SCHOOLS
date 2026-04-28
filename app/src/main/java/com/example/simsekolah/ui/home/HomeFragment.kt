package com.example.simsekolah.ui.home

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
import com.example.simsekolah.R
import com.example.simsekolah.databinding.FragmentHomeBinding
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupMenu()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        user?.let {
                            binding.tvUsername.text = "${it.name} 👋"
                            // Update other UI elements based on profile
                        }
                    }
                }
                launch {
                    viewModel.assignments.collect { assignments ->
                        // Update summary tugas
                        // binding.tvTaskSummary.text = "Anda memiliki ${assignments.size} tugas"
                    }
                }
                launch {
                    viewModel.events.collect { events ->
                        // Update summary event
                    }
                }
            }
        }
    }

    private fun setupMenu() {
        binding.apply {
            menuAssignments.setOnClickListener {
                findNavController().navigate(R.id.assignmentsFragment)
            }
            menuEvent.setOnClickListener {
                findNavController().navigate(R.id.eventFragment)
            }
            menuInformation.setOnClickListener {
                findNavController().navigate(R.id.informasiFragment)
            }
            btnNotification.setOnClickListener {
                findNavController().navigate(R.id.notificationsFragment)
            }
            // Add navigation for Schedule and Attendance if they exist in nav_graph
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
