package com.example.simsekolah.ui.assignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.local.entity.AssignmentEntity
import com.example.simsekolah.data.remote.response.AssignmentItem
import com.example.simsekolah.data.remote.response.SubmissionItem
import com.example.simsekolah.data.remote.response.SiswaItem
import com.example.simsekolah.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssignmentsViewModel(
    private val schoolRepo: SchoolRepository
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<AssignmentItem>>(emptyList())
    val assignments: StateFlow<List<AssignmentItem>> = _assignments.asStateFlow()

    private val _submissions = MutableStateFlow<List<SubmissionItem>>(emptyList())
    val submissions: StateFlow<List<SubmissionItem>> = _submissions.asStateFlow()

    private val _students = MutableStateFlow<List<SiswaItem>>(emptyList())
    val students: StateFlow<List<SiswaItem>> = _students.asStateFlow()

    private val _studentSubmissions = MutableStateFlow<List<SubmissionItem>>(emptyList())
    val studentSubmissions: StateFlow<List<SubmissionItem>> = _studentSubmissions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _createStatus = MutableSharedFlow<Boolean>()
    val createStatus: SharedFlow<Boolean> = _createStatus.asSharedFlow()

    fun loadAssignments(guruId: Int? = null, kelasId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.getAssignmentsFirestore(guruId, kelasId)
                .catch { 
                    _assignments.value = emptyList()
                    _isLoading.value = false
                }
                .collect { list ->
                    _assignments.value = list
                    _isLoading.value = false
                }
        }
    }

    fun loadSubmissions(assignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            schoolRepo.getSubmissionsFirestore(assignmentId)
                .catch { _submissions.value = emptyList() }
                .collect { list ->
                    _submissions.value = list
                }
            _isLoading.value = false
        }
    }

    fun loadStudentSubmissions(siswaId: Int) {
        viewModelScope.launch {
            schoolRepo.getSubmissionsBySiswa(siswaId)
                .catch { _studentSubmissions.value = emptyList() }
                .collect { list ->
                    _studentSubmissions.value = list
                }
        }
    }

    fun loadAllStudents() {
        viewModelScope.launch {
            schoolRepo.getSiswa()
                .catch { _students.value = emptyList() }
                .collect { response ->
                    _students.value = response.data
                }
        }
    }

    fun createAssignment(
        title: String,
        description: String,
        dueDate: String,
        fileUrl: String?,
        guruId: Int,
        kelasId: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entity = AssignmentEntity(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    fileUrl = fileUrl,
                    guruId = guruId,
                    kelasId = kelasId
                )
                schoolRepo.saveAssignmentLocal(entity)
                schoolRepo.saveAssignmentFirestore(entity) // Ini yang menyebabkan error PERMISSION_DENIED
                _createStatus.emit(true)
            } catch (e: Exception) {
                android.util.Log.e("AssignmentsViewModel", "Error create assignment: ${e.message}")
                _createStatus.emit(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
