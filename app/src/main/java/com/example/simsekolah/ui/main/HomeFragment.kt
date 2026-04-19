package com.example.simsekolah.ui.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.simsekolah.R
import com.example.simsekolah.adapter.BannerAdapter
import com.example.simsekolah.adapter.TugasAdapter
import com.example.simsekolah.model.TugasModel
import com.example.simsekolah.databinding.FragmentHomeBinding
import com.example.simsekolah.UserPreference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.math.abs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var userPreference: UserPreference
    private val gson = Gson()
    private val tugasList = mutableListOf<TugasModel>()
    private lateinit var adapterTugas: TugasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())
        val user = userPreference.getUser()
        binding.tvUsername.text = if (user.name.isNullOrEmpty()) "User" else user.name

        setupBanner()
        setupMenu()
        loadProfileImage()
        setupAssignments()
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
        setupAssignments() // Muat ulang data setiap kali kembali ke Home
    }

    private fun loadProfileImage() {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val savedPath = sharedPref.getString("profile_path", null)
        if (savedPath != null) {
            val file = File(savedPath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }

    private fun setupAssignments() {
        loadTugasData()
        
        // Filter: Hanya tampilkan tugas yang BELUM selesai (isDone == false)
        val activeTugas = tugasList.filter { !it.isDone }

        // Update Visibility "NO ASSIGNMENT"
        if (activeTugas.isEmpty()) {
            binding.tvNoAssignment.visibility = View.VISIBLE
            binding.rvTugas.visibility = View.GONE
        } else {
            binding.tvNoAssignment.visibility = View.GONE
            binding.rvTugas.visibility = View.VISIBLE
        }

        adapterTugas = TugasAdapter(activeTugas)
        binding.rvTugas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterTugas
            isNestedScrollingEnabled = false
        }
    }

    private fun loadTugasData() {
        val sharedPref = requireActivity().getSharedPreferences("TugasPrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("list_tugas", null)
        tugasList.clear()
        if (json != null) {
            val type = object : TypeToken<MutableList<TugasModel>>() {}.type
            val savedList: MutableList<TugasModel> = gson.fromJson(json, type)
            tugasList.addAll(savedList)
        }
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
                    val itemCount = it.viewPagerBanner.adapter?.itemCount ?: 0
                    if (itemCount > 0) {
                        val nextItem = (it.viewPagerBanner.currentItem + 1) % itemCount
                        it.viewPagerBanner.setCurrentItem(nextItem, true)
                    }
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