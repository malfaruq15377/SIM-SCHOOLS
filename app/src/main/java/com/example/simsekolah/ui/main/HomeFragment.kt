package com.example.simsekolah.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.R
import com.example.simsekolah.adapter.BannerAdapter
import com.example.simsekolah.adapter.TugasAdapter
import com.example.simsekolah.data.model.TugasModel
import com.example.simsekolah.databinding.FragmentHomeBinding
import kotlin.math.abs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listDataTugas = listOf(
            TugasModel("Thursday, 22 April", "11:30", "Mathematics Exam", "Chapter 4: Algebra"),
            TugasModel("Friday, 23 April", "09:00", "English Essay", "Write about environment"),
            TugasModel("Monday, 26 April", "10:00", "Biology Quiz", "Human Anatomy")
        )

        // 2. Hubungkan ke RecyclerView (rvTugas)
        val adapterTugas = TugasAdapter(listDataTugas)
        binding.rvTugas.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = adapterTugas
            isNestedScrollingEnabled = false // Penting karena rvTugas di dalam ScrollView

        }
        setupBanner()
        setupMenu()
    }

    private fun setupBanner() {
        val bannerList = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )

        val adapter = BannerAdapter(bannerList)
        binding.viewPagerBanner.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPagerBanner)

        binding.viewPagerBanner.setPageTransformer { page, position ->
            val scale = 0.9f + (1 - abs(position)) * 0.1f
            page.scaleY = scale
            page.scaleX = scale
            page.alpha = 0.5f + (1 - abs(position)) * 0.5f
        }

        // AUTO SLIDE
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                _binding?.let {
                    val nextItem = (it.viewPagerBanner.currentItem + 1) % bannerList.size
                    it.viewPagerBanner.currentItem = nextItem
                    handler.postDelayed(this, 3000)
                }
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    private fun setupMenu() {
        binding.menuAssignments.setOnClickListener {
            findNavController().navigate(R.id.assignmentsFragment)
        }

        binding.menuEvent.setOnClickListener {
            findNavController().navigate(R.id.eventFragment)
        }

        binding.menuFees.setOnClickListener {
            findNavController().navigate(R.id.feesFragment)
        }

        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::handler.isInitialized && ::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        _binding = null
    }
}