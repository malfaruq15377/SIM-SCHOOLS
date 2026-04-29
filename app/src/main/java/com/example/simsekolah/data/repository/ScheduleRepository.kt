package com.example.simsekolah.data.repository

import com.example.simsekolah.model.ScheduleModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ScheduleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getSchedulesByKelas(kelasId: String): Flow<List<ScheduleModel>> = callbackFlow {
        val subscription = firestore.collection("schedules")
            .whereEqualTo("kelasId", kelasId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(ScheduleModel::class.java) ?: emptyList()
                val daysOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
                val sortedList = list.sortedBy { daysOrder.indexOf(it.day) }
                trySend(sortedList)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateSchedule(schedule: ScheduleModel): Result<Unit> {
        return try {
            if (schedule.id.isEmpty()) {
                val docRef = firestore.collection("schedules").document()
                val newSchedule = schedule.copy(id = docRef.id)
                docRef.set(newSchedule).await()
            } else {
                firestore.collection("schedules").document(schedule.id).set(schedule).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
