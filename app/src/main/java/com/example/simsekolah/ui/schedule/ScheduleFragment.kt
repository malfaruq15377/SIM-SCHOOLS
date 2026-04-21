package com.example.simsekolah.ui.schedule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simsekolah.data.local.preference.UserPreference
import com.example.simsekolah.data.remote.response.GuruInfo
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.data.remote.response.MapelInfo
import com.example.simsekolah.databinding.DialogAddScheduleBinding
import com.example.simsekolah.databinding.FragmentScheduleBinding
import com.example.simsekolah.databinding.LayoutInputScheduleRowBinding
import com.example.simsekolah.utils.ViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ViewModelFactory.Companion.getInstance(requireContext())
    }

    private lateinit var dayScheduleAdapter: DayScheduleAdapter
    private lateinit var userPreference: UserPreference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())
        setupRecyclerView()
        observeViewModel()

        val user = userPreference.getUser()
        viewModel.fetchSchedule(requireContext(), user.role, user.age)
    }

    private fun setupRecyclerView() {
        val user = userPreference.getUser()
        val isGuru = user.role?.equals("guru", ignoreCase = true) == true

        dayScheduleAdapter = DayScheduleAdapter(
            daySchedules = emptyList(),
            isGuru = isGuru,
            onEditClicked = { dayData ->
                showEditScheduleDialog(dayData)
            }
        )
        binding.rvDays.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dayScheduleAdapter
        }
    }

    private fun showEditScheduleDialog(dayData: DayScheduleAdapter.DaySchedule) {
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDialogDay.text = "Hari: ${dayData.dayName}"

        val inputLayouts = listOf(
            dialogBinding.layoutMapel1,
            dialogBinding.layoutMapel2,
            dialogBinding.layoutMapel3,
            dialogBinding.layoutMapel4,
            dialogBinding.layoutMapel5
        )

        // Pre-fill data
        dayData.items.forEachIndexed { index, item ->
            if (index < inputLayouts.size) {
                val row = LayoutInputScheduleRowBinding.bind(inputLayouts[index].root)
                row.tvMapelLabel.text = "Mata Pelajaran ${index + 1}"
                row.etSubjectName.setText(item.mapel?.name ?: item.mapelId)
                row.etStartTime.setText(item.jamMulai)
                row.etEndTime.setText(item.jamSelesai)
                row.etTeacherName.setText(item.guru?.nama ?: "")
            }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
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
                            id = "local_${dayData.dayName}_$index",
                            hari = dayData.dayName,
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
                saveSchedulesToLocal(dayData.dayName, newList)
                Toast.makeText(requireContext(), "Jadwal ${dayData.dayName} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                
                // Refresh UI
                val user = userPreference.getUser()
                viewModel.fetchSchedule(requireContext(), user.role, user.age)
            } else {
                Toast.makeText(requireContext(), "Minimal isi satu mata pelajaran!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveSchedulesToLocal(dayName: String, list: List<JadwalItem>) {
        val sharedPref = requireContext().getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        
        // Ambil map lama
        val json = sharedPref.getString("local_schedules", null)
        val type = object : TypeToken<MutableMap<String, List<JadwalItem>>>() {}.type
        val currentMap: MutableMap<String, List<JadwalItem>> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableMapOf()
        }

        // Update hari ini
        currentMap[dayName] = list
        
        // Simpan kembali
        sharedPref.edit().putString("local_schedules", gson.toJson(currentMap)).apply()
    }

    private fun observeViewModel() {
        viewModel.dayScheduleList.observe(viewLifecycleOwner) { list ->
            dayScheduleAdapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
