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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.R
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.databinding.FragmentHomeBinding
import com.example.simsekolah.ui.assignment.TugasAdapter
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var tugasAdapter: TugasAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext())
        
        setupRecyclerView()
        setupBanner()
        setupButtons()
        observeViewModel()

        fetchData()
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            binding.tvUsername.text = user.name
            
            // Mengambil data home termasuk tugas terbaru
            viewModel.fetchHomeData()
        }
    }

    private fun setupRecyclerView() {
        tugasAdapter = TugasAdapter { assignment ->
            // Saat tugas di klik, arahkan ke menu Assignments
            findNavController().navigate(R.id.assignmentsFragment)
        }
        binding.rvTugas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tugasAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBanner() {
        bannerAdapter = BannerAdapter()
        binding.viewPagerBanner.adapter = bannerAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerBanner)
    }

    private fun setupButtons() {
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        binding.menuAssignments.setOnClickListener {
            findNavController().navigate(R.id.assignmentsFragment)
        }

        binding.menuEvent.setOnClickListener {
            findNavController().navigate(R.id.eventFragment)
        }

        binding.menuInformation.setOnClickListener {
            findNavController().navigate(R.id.informasiFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.assignments.collect { list ->
                        tugasAdapter.submitList(list)
                        binding.tvNoAssignment.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.pengumuman.collect { list ->
                        bannerAdapter.setItems(list)
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        // Bisa ditambahkan ProgressBar di layout jika ingin menampilkan loading
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
