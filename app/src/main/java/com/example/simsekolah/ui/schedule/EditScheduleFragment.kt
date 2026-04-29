package com.example.simsekolah.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.databinding.FragmentEditScheduleBinding
import com.example.simsekolah.databinding.LayoutInputScheduleRowBinding
import com.example.simsekolah.model.ScheduleModel
import com.example.simsekolah.model.SubjectModel
import com.example.simsekolah.utils.ViewModelFactory
import kotlinx.coroutines.launch

class EditScheduleFragment : Fragment() {
    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ViewModelFactory.getInstance()
    }

    private var currentSchedule: ScheduleModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentSchedule = arguments?.getParcelable("schedule")
        
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        currentSchedule?.let { schedule ->
            binding.tvDayName.text = schedule.day
            
            val layouts = listOf(
                binding.layoutMapel1,
                binding.layoutMapel2,
                binding.layoutMapel3,
                binding.layoutMapel4,
                binding.layoutMapel5
            )

            schedule.subjects.forEachIndexed { index, subject ->
                if (index < layouts.size) {
                    val rowBinding = LayoutInputScheduleRowBinding.bind(layouts[index].root)
                    rowBinding.tvMapelLabel.text = "Subject ${index + 1}"
                    rowBinding.etSubjectName.setText(subject.name)
                    rowBinding.etTeacherName.setText(subject.guruName)
                    
                    val times = subject.time.split(" - ")
                    if (times.size == 2) {
                        rowBinding.etStartTime.setText(times[0])
                        rowBinding.etEndTime.setText(times[1])
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            saveSchedule()
        }
    }

    private fun saveSchedule() {
        val schedule = currentSchedule ?: return
        val layouts = listOf(
            binding.layoutMapel1,
            binding.layoutMapel2,
            binding.layoutMapel3,
            binding.layoutMapel4,
            binding.layoutMapel5
        )

        val updatedSubjects = mutableListOf<SubjectModel>()
        layouts.forEach { layout ->
            val rowBinding = LayoutInputScheduleRowBinding.bind(layout.root)
            val name = rowBinding.etSubjectName.text.toString()
            val startTime = rowBinding.etStartTime.text.toString()
            val endTime = rowBinding.etEndTime.text.toString()
            val teacher = rowBinding.etTeacherName.text.toString()

            if (name.isNotEmpty()) {
                updatedSubjects.add(SubjectModel(
                    name = name,
                    time = "$startTime - $endTime",
                    guruName = teacher
                ))
            }
        }

        val newSchedule = schedule.copy(subjects = updatedSubjects)
        viewModel.updateSchedule(newSchedule)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateStatus.collect { result ->
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Schedule updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update schedule: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
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
