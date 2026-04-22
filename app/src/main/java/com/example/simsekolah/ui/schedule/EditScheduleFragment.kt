package com.example.simsekolah.ui.schedule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.simsekolah.data.remote.response.GuruInfo
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.data.remote.response.MapelInfo
import com.example.simsekolah.databinding.FragmentEditScheduleBinding
import com.example.simsekolah.databinding.LayoutInputScheduleRowBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EditScheduleFragment : Fragment() {

    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dayName = arguments?.getString("day_name") ?: ""
        val scheduleJson = arguments?.getString("schedule_json") ?: ""
        
        binding.tvDayName.text = "Hari: $dayName"
        binding.toolbar.title = "Edit $dayName Schedule"

        val gson = Gson()
        val type = object : TypeToken<List<JadwalItem>>() {}.type
        val scheduleList: List<JadwalItem> = if (scheduleJson.isNotEmpty()) {
            gson.fromJson(scheduleJson, type)
        } else {
            emptyList()
        }

        val inputLayouts = listOf(
            binding.layoutMapel1,
            binding.layoutMapel2,
            binding.layoutMapel3,
            binding.layoutMapel4,
            binding.layoutMapel5
        )

        // Pre-fill data
        scheduleList.forEachIndexed { index, item ->
            if (index < inputLayouts.size) {
                val row = LayoutInputScheduleRowBinding.bind(inputLayouts[index].root)
                row.tvMapelLabel.text = "Mata Pelajaran ${index + 1}"
                row.etSubjectName.setText(item.mapel?.name ?: item.mapelId)
                row.etStartTime.setText(item.jamMulai)
                row.etEndTime.setText(item.jamSelesai)
                row.etTeacherName.setText(item.guru?.nama ?: "")
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            val newList = mutableListOf<JadwalItem>()
            
            inputLayouts.forEachIndexed { index, includeLayout ->
                val row = LayoutInputScheduleRowBinding.bind(includeLayout.root)
                val subject = row.etSubjectName.text.toString().trim()
                val start = row.etStartTime.text.toString().trim()
                val end = row.etEndTime.text.toString().trim()
                val teacher = row.etTeacherName.text.toString().trim()

                if (subject.isNotEmpty()) {
                    newList.add(
                        JadwalItem(
                            id = "local_${dayName}_$index",
                            hari = dayName,
                            jamMulai = start,
                            jamSelesai = end,
                            mapelId = subject,
                            mapel = MapelInfo(name = subject),
                            guru = GuruInfo(nama = teacher)
                        )
                    )
                }
            }

            if (newList.isNotEmpty()) {
                saveSchedulesToLocal(dayName, newList)
                Toast.makeText(requireContext(), "Jadwal $dayName berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Minimal isi satu mata pelajaran!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSchedulesToLocal(dayName: String, list: List<JadwalItem>) {
        val sharedPref = requireContext().getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        
        val json = sharedPref.getString("local_schedules", null)
        val type = object : TypeToken<MutableMap<String, List<JadwalItem>>>() {}.type
        val currentMap: MutableMap<String, List<JadwalItem>> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableMapOf()
        }

        currentMap[dayName] = list
        sharedPref.edit().putString("local_schedules", gson.toJson(currentMap)).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}