package com.example.simsekolah.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.adapter.ScheduleAdapter
import com.example.simsekolah.data.model.Schedule
import com.example.simsekolah.databinding.FragmentScheduleBinding
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyData = listOf(
            Schedule("08:00 - 09:30", "Mathematics", "Room 101", "Dr. Smith"),
            Schedule("10:00 - 11:30", "Physics", "Lab A", "Prof. Einstein"),
            Schedule("13:00 - 14:30", "English", "Language Center", "Mrs. Watson")
        )
        val scheduleAdapter = ScheduleAdapter(dummyData)
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
            setHasFixedSize(true)
        }

        binding.chipGroupDays.setOnCheckedStateChangeListener {
            group, checkedIds ->
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}