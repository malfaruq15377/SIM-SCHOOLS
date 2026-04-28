package com.example.simsekolah.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simsekolah.data.repository.AssignmentRepository
import com.example.simsekolah.data.repository.AuthRepository
import com.example.simsekolah.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val authRepo: AuthRepository,
    private val assignmentRepo: AssignmentRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

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
    }

    fun updateProfileImage(uri: Uri, isBackground: Boolean = false) {
        val uid = authRepo.getCurrentUserUid() ?: return
        viewModelScope.launch {
            try {
                val path = if (isBackground) "backgrounds" else "profiles"
                val url = assignmentRepo.uploadFile(uri, path)
                val field = if (isBackground) "backgroundUrl" else "photoUrl"
                _operationStatus.emit(authRepo.updateProfile(uid, mapOf(field to url)))
            } catch (e: Exception) {
                _operationStatus.emit(Result.failure(e))
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPass).await()
                    _operationStatus.emit(Result.success(Unit))
                } else {
                    _operationStatus.emit(Result.failure(Exception("User not found")))
                }
            } catch (e: Exception) {
                _operationStatus.emit(Result.failure(e))
            }
        }
    }

    fun logout() {
        authRepo.signOut()
    }
}
