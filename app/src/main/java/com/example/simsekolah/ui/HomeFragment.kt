package com.example.simsekolah.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.simsekolah.R
import com.example.simsekolah.adapter.BannerAdapter
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

        setupBanner()
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        _binding = null
    }
}