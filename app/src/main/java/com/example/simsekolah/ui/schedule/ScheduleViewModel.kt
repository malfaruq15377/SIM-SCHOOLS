// File: C:/Users/PC-1/Documents/SIM sekolah/SIM Sekolah/SIM-Sekolah/app/src/main/java/com/example/simsekolah/ui/schedule/ScheduleViewModel.kt

package com.example.simsekolah.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.GuruInfo
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.data.remote.response.MapelInfo
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _dayScheduleList = MutableLiveData<List<DayScheduleAdapter.DaySchedule>>()
    val dayScheduleList: LiveData<List<DayScheduleAdapter.DaySchedule>> = _dayScheduleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchSchedule(role: String?, kelasId: Int?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getJadwal().collect { response ->
                    val apiData = if (response.success) response.data else emptyList()
                    val filtered = if (role?.equals("admin", ignoreCase = true) == true) {
                        apiData
                    } else {
                        apiData.filter { it.kelasId == kelasId?.toString() }
                    }
                    _dayScheduleList.value = groupSchedulesByDay(filtered)
                }
            } catch (e: Exception) {
                _dayScheduleList.value = groupSchedulesByDay(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun groupSchedulesByDay(list: List<JadwalItem>): List<DayScheduleAdapter.DaySchedule> {
        val daysOrder = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        return daysOrder.map { dayName ->
            val itemsFromApi = list.filter { it.hari.equals(dayName, ignoreCase = true) || it.hari.equals(getIndonesianDay(dayName), ignoreCase = true) }
            val finalItems = itemsFromApi.toMutableList()
            // Melengkapi hingga 5 data per hari
            while (finalItems.size < 5) {
                finalItems.add(createDummyItem(dayName, finalItems.size + 1))
            }
            DayScheduleAdapter.DaySchedule(dayName, finalItems.take(5), false)
        }
    }

    private fun getIndonesianDay(day: String) = when(day) {
        "Sunday" -> "Minggu"; "Monday" -> "Senin"; "Tuesday" -> "Selasa"; "Wednesday" -> "Rabu"
        "Thursday" -> "Kamis"; "Friday" -> "Jumat"; "Saturday" -> "Sabtu"; else -> day
    }

    private fun createDummyItem(day: String, index: Int): JadwalItem {
        val mapels = listOf("Matematika", "Bahasa Inggris", "Fisika", "Biologi", "Sejarah", "Kimia", "Ekonomi")
        val gurus = listOf("Drs. Mulyadi", "Siti Aminah, M.Pd", "Budi Santoso", "Lestari Putri", "Joko Susilo", "Dewi Sartika")

        return JadwalItem(
            id = "dummy_${day}_$index",
            hari = day,
            jamMulai = "0${7+index}:00",
            jamSelesai = "0${8+index}:00",
            mapelId = "M${index}",
            mapel = MapelInfo(name = mapels[index % mapels.size]),
            guru = GuruInfo(nama = gurus[index % gurus.size])
        )
    }
}