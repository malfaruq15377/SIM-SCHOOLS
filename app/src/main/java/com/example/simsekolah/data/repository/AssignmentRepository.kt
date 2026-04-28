package com.example.simsekolah.data.repository

import android.net.Uri
import com.example.simsekolah.model.AssignmentModel
import com.example.simsekolah.model.SubmissionModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AssignmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadFile(uri: Uri, path: String): String {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("$path/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createAssignment(assignment: AssignmentModel): Result<Unit> {
        return try {
            val docRef = firestore.collection("assignments").document()
            val newAssignment = assignment.copy(id = docRef.id)
            docRef.set(newAssignment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAssignmentsForGuru(guruId: String): Flow<List<AssignmentModel>> = callbackFlow {
        val subscription = firestore.collection("assignments")
            .whereEqualTo("guruId", guruId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(AssignmentModel::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    fun getAssignmentsForSiswa(waliKelasId: String): Flow<List<AssignmentModel>> = callbackFlow {
        val subscription = firestore.collection("assignments")
            .whereEqualTo("guruId", waliKelasId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(AssignmentModel::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getSubmissionsForStudent(studentId: String): List<SubmissionModel> {
        return try {
            val snapshot = firestore.collection("submissions")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            snapshot.toObjects(SubmissionModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitAssignment(submission: SubmissionModel): Result<Unit> {
        return try {
            val docRef = firestore.collection("submissions").document()
            val newSubmission = submission.copy(id = docRef.id)
            docRef.set(newSubmission).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
