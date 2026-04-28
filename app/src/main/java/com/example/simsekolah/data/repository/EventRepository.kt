package com.example.simsekolah.data.repository

import com.example.simsekolah.model.EventModel
import com.example.simsekolah.model.SchoolInfoModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getEvents(): Flow<List<EventModel>> = callbackFlow {
        val subscription = firestore.collection("events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(EventModel::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createEvent(event: EventModel): Result<Unit> {
        return try {
            val docRef = firestore.collection("events").document()
            firestore.collection("events").document(docRef.id).set(event.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSchoolInfo(): Flow<SchoolInfoModel?> = callbackFlow {
        val subscription = firestore.collection("school_info").document("main")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(SchoolInfoModel::class.java))
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateSchoolInfo(info: SchoolInfoModel): Result<Unit> {
        return try {
            firestore.collection("school_info").document("main").set(info).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
