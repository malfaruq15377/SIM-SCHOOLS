package com.example.simsekolah.ui.assignment

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.SchoolRepository
import com.example.simsekolah.data.remote.response.AssignmentResponse
import com.example.simsekolah.data.remote.response.SubmissionResponse
import com.example.simsekolah.data.remote.response.UserResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssignmentsViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserResponse?>(null)
    val userProfile: StateFlow<UserResponse?> = _userProfile.asStateFlow()

    private val _assignments = MutableStateFlow<List<AssignmentResponse>>(emptyList())
    val assignments: StateFlow<List<AssignmentResponse>> = _assignments.asStateFlow()

    private val _operationStatus = MutableSharedFlow<Result<Unit>>()
    val operationStatus = _operationStatus.asSharedFlow()

    init {
        val uid = schoolRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                schoolRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    if (user != null) {
                        loadAssignments(user)
                    }
                }
            }
        }
    }

    private fun loadAssignments(user: UserResponse) {
        viewModelScope.launch {
            if (user.role == "guru") {
                schoolRepo.getAssignmentsForGuru(user.uid).collect { list ->
                    _assignments.value = list
                }
            } else if (user.role == "siswa" && user.waliKelasId != null) {
                val allAssignmentsFlow = schoolRepo.getAssignmentsForSiswa(user.waliKelasId)
                val submissions = schoolRepo.getSubmissionsForStudent(user.uid)
                val submittedIds = submissions.map { it.assignmentId }.toSet()
                
                allAssignmentsFlow.collect { list ->
                    _assignments.value = list.filter { it.id !in submittedIds }
                }
            }
        }
    }

    fun createAssignment(title: String, description: String, dueDate: String, fileUri: Uri?) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            try {
                var fileUrl: String? = null
                if (fileUri != null) {
                    fileUrl = schoolRepo.uploadFile(fileUri, "assignments")
                }
                
                val assignment = AssignmentResponse(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    fileUrl = fileUrl,
                    guruId = user.uid,
                    kelasId = user.kelasId ?: ""
                )
                
                val result = schoolRepo.createAssignment(assignment)
                if (result.isSuccess) {
                    schoolRepo.createNotificationsForClass(
                        user.kelasId ?: "",
                        "Tugas Baru: $title",
                        "Guru Anda telah menambahkan tugas baru.",
                        "Assignment",
                        ""
                    )
                }
                _operationStatus.emit(result)
            } catch (e: Exception) {
                _operationStatus.emit(Result.failure(e))
            }
        }
    }

    fun submitAssignment(assignmentId: String, fileUri: Uri) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            try {
                val fileUrl = schoolRepo.uploadFile(fileUri, "submissions")
                val submission = SubmissionResponse(
                    assignmentId = assignmentId,
                    studentId = user.uid,
                    fileUrl = fileUrl
                )
                val result = schoolRepo.submitAssignment(submission)
                _operationStatus.emit(result)
                loadAssignments(user)
            } catch (e: Exception) {
                _operationStatus.emit(Result.failure(e))
            }
        }
    }
}
