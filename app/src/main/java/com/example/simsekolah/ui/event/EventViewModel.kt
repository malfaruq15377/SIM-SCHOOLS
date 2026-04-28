package com.example.simsekolah.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.data.repository.EventRepository
import com.example.simsekolah.model.EventModel
import com.example.simsekolah.model.SchoolInfoModel
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepo: EventRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _events = MutableStateFlow<List<EventModel>>(emptyList())
    val events: StateFlow<List<EventModel>> = _events.asStateFlow()

    private val _schoolInfo = MutableStateFlow<SchoolInfoModel?>(null)
    val schoolInfo: StateFlow<SchoolInfoModel?> = _schoolInfo.asStateFlow()

    private val _operationStatus = MutableSharedFlow<Result<Unit>>()
    val operationStatus = _operationStatus.asSharedFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                authRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                }
            }
        }
        
        viewModelScope.launch {
            eventRepo.getEvents().collect { list ->
                _events.value = list
            }
        }

        viewModelScope.launch {
            eventRepo.getSchoolInfo().collect { info ->
                _schoolInfo.value = info
            }
        }
    }

    fun createEvent(title: String, description: String, date: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val event = EventModel(
                title = title,
                description = description,
                date = date,
                imageUrl = imageUrl
            )
            _operationStatus.emit(eventRepo.createEvent(event))
        }
    }

    fun updateSchoolInfo(name: String, address: String, description: String, imageUrl: String? = null) {
        viewModelScope.launch {
            val info = SchoolInfoModel(name, address, description, imageUrl)
            _operationStatus.emit(eventRepo.updateSchoolInfo(info))
        }
    }
}
