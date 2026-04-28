package com.example.simsekolah.ui.assignment

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AssignmentRepository
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.data.repository.NotificationRepository
import com.example.simsekolah.model.AssignmentModel
import com.example.simsekolah.model.SubmissionModel
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssignmentsViewModel(
    private val assignmentRepo: AssignmentRepository,
    private val authRepo: AuthRepository,
    private val notificationRepo: NotificationRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _assignments = MutableStateFlow<List<AssignmentModel>>(emptyList())
    val assignments: StateFlow<List<AssignmentModel>> = _assignments.asStateFlow()

    private val _uploadStatus = MutableSharedFlow<Result<String>>()
    val uploadStatus = _uploadStatus.asSharedFlow()

    private val _operationStatus = MutableSharedFlow<Result<Unit>>()
    val operationStatus = _operationStatus.asSharedFlow()

    init {
        val uid = authRepo.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                authRepo.getUserProfileRealtime(uid).collect { user ->
                    _userProfile.value = user
                    if (user != null) {
                        loadAssignments(user)
                    }
                }
            }
        }
    }

    private fun loadAssignments(user: UserModel) {
        viewModelScope.launch {
            if (user.role == "guru") {
                assignmentRepo.getAssignmentsForGuru(user.uid).collect { list ->
                    _assignments.value = list
                }
            } else if (user.role == "siswa" && user.waliKelasId != null) {
                val allAssignmentsFlow = assignmentRepo.getAssignmentsForSiswa(user.waliKelasId)
                val submissions = assignmentRepo.getSubmissionsForStudent(user.uid)
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
                    fileUrl = assignmentRepo.uploadFile(fileUri, "assignments")
                }
                
                val assignment = AssignmentModel(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    fileUrl = fileUrl,
                    guruId = user.uid,
                    kelasId = user.kelasId ?: ""
                )
                
                val result = assignmentRepo.createAssignment(assignment)
                if (result.isSuccess) {
                    // Trigger Notification
                    notificationRepo.createNotificationsForClass(
                        user.kelasId ?: "",
                        "Tugas Baru: $title",
                        "Guru Anda telah menambahkan tugas baru.",
                        "Assignment",
                        "" // In a real app, pass the new doc ID
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
                val fileUrl = assignmentRepo.uploadFile(fileUri, "submissions")
                val submission = SubmissionModel(
                    assignmentId = assignmentId,
                    studentId = user.uid,
                    fileUrl = fileUrl
                )
                val result = assignmentRepo.submitAssignment(submission)
                _operationStatus.emit(result)
                // Refresh list
                loadAssignments(user)
            } catch (e: Exception) {
                _operationStatus.emit(Result.failure(e))
            }
        }
    }
}
