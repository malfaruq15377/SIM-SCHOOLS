package com.example.simsekolah.data.repository

import com.example.simsekolah.model.NotificationModel
import com.example.simsekolah.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createNotificationsForClass(kelasId: String, title: String, message: String, type: String, referenceId: String) {
        try {
            val students = firestore.collection("users")
                .whereEqualTo("role", "siswa")
                .whereEqualTo("kelasId", kelasId)
                .get()
                .await()

            val batch = firestore.batch()
            for (studentDoc in students) {
                val notifRef = firestore.collection("users").document(studentDoc.id).collection("notifications").document()
                val notification = NotificationModel(
                    id = notifRef.id,
                    title = title,
                    message = message,
                    type = type,
                    referenceId = referenceId
                )
                batch.set(notifRef, notification)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNotifications(uid: String): Flow<List<NotificationModel>> = callbackFlow {
        val subscription = firestore.collection("users").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(NotificationModel::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markAsRead(uid: String, notificationId: String) {
        try {
            firestore.collection("users").document(uid).collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
