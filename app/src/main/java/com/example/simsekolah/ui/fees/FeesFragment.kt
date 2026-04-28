package com.example.simsekolah.ui.fees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.simsekolah.R
import com.example.simsekolah.ui.fees.FeesImageAdapter
import com.example.simsekolah.databinding.FragmentFeesBinding

class FeesFragment : Fragment() {

    private var _binding: FragmentFeesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageSlider()
    }

    private fun setupImageSlider() {
        val imageList = listOf(
            R.drawable.img,
            R.drawable.img_1,
            R.drawable.img_2
        )

        val adapter = FeesImageAdapter(imageList)
        binding.viewPagerFees.adapter = adapter
        binding.dotsIndicatorFees.attachTo(binding.viewPagerFees)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}