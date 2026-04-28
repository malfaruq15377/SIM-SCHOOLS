package com.example.simsekolah.data.repository

import com.example.simsekolah.model.AttendanceModel
import com.example.simsekolah.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getStudentsForGuru(guruId: String): List<UserModel> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "siswa")
                .whereEqualTo("waliKelasId", guruId)
                .get()
                .await()
            snapshot.toObjects(UserModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAttendanceBatch(attendances: List<AttendanceModel>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            attendances.forEach { attendance ->
                val docRef = firestore.collection("attendances").document()
                batch.set(docRef, attendance.copy(id = docRef.id))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentAttendanceToday(studentId: String, date: String): AttendanceModel? {
        return try {
            val snapshot = firestore.collection("attendances")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("date", date)
                .get()
                .await()
            snapshot.toObjects(AttendanceModel::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitStudentAttendance(attendance: AttendanceModel): Result<Unit> {
        return try {
            val docRef = firestore.collection("attendances").document()
            firestore.collection("attendances").document(docRef.id).set(attendance.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
