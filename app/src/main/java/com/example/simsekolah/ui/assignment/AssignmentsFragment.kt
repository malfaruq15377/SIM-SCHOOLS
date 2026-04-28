package com.example.simsekolah.ui.assignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.databinding.FragmentAssignmentsBinding

class AssignmentsFragment : Fragment() {
    private var _binding: FragmentAssignmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Belum ada tombol back di XML fragment_assignments.xml
        // Jika kamu ingin menambahkannya, kamu perlu menambah ImageView back di layout-nya.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
