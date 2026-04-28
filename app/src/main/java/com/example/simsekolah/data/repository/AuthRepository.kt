package com.example.simsekolah.data.repository

import com.example.simsekolah.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    suspend fun getUserProfile(uid: String): UserModel? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserProfileRealtime(uid: String): Flow<UserModel?> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(UserModel::class.java)
                trySend(user)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
