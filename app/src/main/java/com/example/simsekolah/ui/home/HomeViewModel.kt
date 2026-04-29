package com.example.simsekolah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.data.remote.response.AssignmentResponse
import com.example.simsekolah.data.remote.response.EventResponse
import com.example.simsekolah.data.remote.response.UserResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserResponse?>(null)
    val userProfile: StateFlow<UserResponse?> = _userProfile.asStateFlow()

    private val _assignments = MutableStateFlow<List<AssignmentResponse>>(emptyList())
    val assignments: StateFlow<List<AssignmentResponse>> = _assignments.asStateFlow()

    private val _events = MutableStateFlow<List<EventResponse>>(emptyList())
    val events: StateFlow<List<EventResponse>> = _events.asStateFlow()

    init {
        val uid = schoolRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                schoolRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    if (user != null) {
                        fetchDataForUser(user)
                    }
                }
            }
        }
    }

    private fun fetchDataForUser(user: UserResponse) {
        viewModelScope.launch {
            if (user.role == "guru") {
                schoolRepo.getAssignmentsForGuru(user.uid).collect { list ->
                    _assignments.value = list
                }
            } else if (user.role == "siswa" && user.waliKelasId != null) {
                schoolRepo.getAssignmentsForSiswa(user.waliKelasId).collect { list ->
                    _assignments.value = list
                }
            }
        }

        viewModelScope.launch {
            schoolRepo.getEvents().collect { list ->
                _events.value = list
            }
        }
    }
}
