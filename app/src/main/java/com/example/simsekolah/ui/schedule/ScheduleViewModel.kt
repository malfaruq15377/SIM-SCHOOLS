package com.example.simsekolah.ui.schedule

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.GuruInfo
import com.example.simsekolah.data.remote.response.JadwalItem
import com.example.simsekolah.data.remote.response.MapelInfo
import com.example.simsekolah.data.repository.SchoolRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class ScheduleViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _dayScheduleList = MutableLiveData<List<DayScheduleAdapter.DaySchedule>>()
    val dayScheduleList: LiveData<List<DayScheduleAdapter.DaySchedule>> = _dayScheduleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchSchedule(context: Context, role: String?, kelasId: Int?) {
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
                    
                    // Ambil data dari Local (yang sudah di-edit)
                    val localData = getLocalSchedules(context)
                    
                    _dayScheduleList.value = groupSchedulesByDay(filtered, localData)
                }
            } catch (e: Exception) {
                _dayScheduleList.value = groupSchedulesByDay(emptyList(), getLocalSchedules(context))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getLocalSchedules(context: Context): Map<String, List<JadwalItem>> {
        val sharedPref = context.getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("local_schedules", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, List<JadwalItem>>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyMap()
        }
    }

    private fun groupSchedulesByDay(apiList: List<JadwalItem>, localMap: Map<String, List<JadwalItem>>): List<DayScheduleAdapter.DaySchedule> {
        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        return daysOrder.map { dayName ->
            // Jika ada data local untuk hari ini, gunakan itu. Jika tidak, gunakan API/Dummy
            val finalItems = if (localMap.containsKey(dayName)) {
                localMap[dayName]!!
            } else {
                val itemsFromApi = apiList.filter { it.hari.equals(dayName, ignoreCase = true) || it.hari.equals(getIndonesianDay(dayName), ignoreCase = true) }
                val tempItems = itemsFromApi.toMutableList()
                while (tempItems.size < 5) {
                    tempItems.add(createDummyItem(dayName, tempItems.size + 1))
                }
                tempItems.take(5)
            }
            DayScheduleAdapter.DaySchedule(dayName, finalItems, false)
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
