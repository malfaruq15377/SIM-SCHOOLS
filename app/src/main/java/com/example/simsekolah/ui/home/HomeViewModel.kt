package com.example.simsekolah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.remote.response.AssignmentItem
import com.example.simsekolah.data.remote.response.PengumumanItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<AssignmentItem>>(emptyList())
    val assignments: StateFlow<List<AssignmentItem>> = _assignments.asStateFlow()

    private val _pengumuman = MutableStateFlow<List<PengumumanItem>>(emptyList())
    val pengumuman: StateFlow<List<PengumumanItem>> = _pengumuman.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchHomeData(guruId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            // Menghapus kelasId agar semua siswa bisa melihat semua tugas (broadcast)
            schoolRepo.getAssignmentsFirestore(guruId)
                .combine(schoolRepo.getPengumuman()) { firestoreAssignments, remotePengumumanRes ->
                    Pair(firestoreAssignments, remotePengumumanRes.data)
                }
                .catch { e ->
                    _isLoading.value = false
                }
                .collect { (assignmentList, pengumumanList) ->
                    _assignments.value = assignmentList
                    _pengumuman.value = pengumumanList ?: emptyList()
                    _isLoading.value = false
                }
        }
    }
}
