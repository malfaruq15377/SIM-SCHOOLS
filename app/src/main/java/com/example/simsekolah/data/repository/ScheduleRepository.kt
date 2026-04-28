package com.example.simsekolah.data.repository

import com.example.simsekolah.model.ScheduleModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }
}
