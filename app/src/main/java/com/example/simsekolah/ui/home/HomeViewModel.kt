package com.example.simsekolah.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AssignmentRepository
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.data.repository.EventRepository
import com.example.simsekolah.model.AssignmentModel
import com.example.simsekolah.model.EventModel
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepo: AuthRepository,
    private val assignmentRepo: AssignmentRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _assignments = MutableStateFlow<List<AssignmentModel>>(emptyList())
    val assignments: StateFlow<List<AssignmentModel>> = _assignments.asStateFlow()

    private val _events = MutableStateFlow<List<EventModel>>(emptyList())
    val events: StateFlow<List<EventModel>> = _events.asStateFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                authRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    if (user != null) {
                        fetchDataForUser(user)
                    }
                }
            }
        }
    }

    private fun fetchDataForUser(user: UserModel) {
        viewModelScope.launch {
            if (user.role == "guru") {
                assignmentRepo.getAssignmentsForGuru(user.uid).collect { list ->
                    _assignments.value = list
                }
            } else if (user.role == "siswa" && user.waliKelasId != null) {
                assignmentRepo.getAssignmentsForSiswa(user.waliKelasId).collect { list ->
                    _assignments.value = list
                }
            }
        }

        viewModelScope.launch {
            eventRepo.getEvents().collect { list ->
                _events.value = list
            }
        }
    }
}
