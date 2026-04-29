package com.example.simsekolah.data.repository

import android.net.Uri
import com.example.simsekolah.data.remote.response.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SchoolRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    // --- AUTH LOGIC ---
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    suspend fun getUserProfile(uid: String): UserResponse? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(UserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserProfileRealtime(uid: String): Flow<UserResponse?> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(UserResponse::class.java)
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

    // --- ASSIGNMENT & STORAGE LOGIC ---
    suspend fun uploadFile(uri: Uri, path: String): String {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("$path/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createAssignment(assignment: AssignmentResponse): Result<Unit> {
        return try {
            val docRef = firestore.collection("assignments").document()
            val newAssignment = assignment.copy(id = docRef.id)
            docRef.set(newAssignment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAssignmentsForGuru(guruId: String): Flow<List<AssignmentResponse>> = callbackFlow {
        val subscription = firestore.collection("assignments")
            .whereEqualTo("guruId", guruId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(AssignmentResponse::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    fun getAssignmentsForSiswa(waliKelasId: String): Flow<List<AssignmentResponse>> = callbackFlow {
        val subscription = firestore.collection("assignments")
            .whereEqualTo("guruId", waliKelasId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(AssignmentResponse::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getSubmissionsForStudent(studentId: String): List<SubmissionResponse> {
        return try {
            val snapshot = firestore.collection("submissions")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            snapshot.toObjects(SubmissionResponse::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitAssignment(submission: SubmissionResponse): Result<Unit> {
        return try {
            val docRef = firestore.collection("submissions").document()
            val newSubmission = submission.copy(id = docRef.id)
            docRef.set(newSubmission).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NOTIFICATION LOGIC ---
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
                val notification = NotificationResponse(
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

    fun getNotifications(uid: String): Flow<List<NotificationResponse>> = callbackFlow {
        val subscription = firestore.collection("users").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(NotificationResponse::class.java) ?: emptyList()
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

    // --- ATTENDANCE LOGIC ---
    suspend fun getStudentsForGuru(guruId: String): List<UserResponse> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "siswa")
                .whereEqualTo("waliKelasId", guruId)
                .get()
                .await()
            snapshot.toObjects(UserResponse::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAttendanceBatch(attendances: List<AttendanceResponse>): Result<Unit> {
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

    suspend fun submitStudentAttendance(attendance: AttendanceResponse): Result<Unit> {
        return try {
            val docRef = firestore.collection("attendances").document()
            firestore.collection("attendances").document(docRef.id).set(attendance.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SCHEDULE LOGIC ---
    fun getSchedulesByKelas(kelasId: String): Flow<List<ScheduleResponse>> = callbackFlow {
        val subscription = firestore.collection("schedules")
            .whereEqualTo("kelasId", kelasId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(ScheduleResponse::class.java) ?: emptyList()
                val daysOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
                val sortedList = list.sortedBy { daysOrder.indexOf(it.day) }
                trySend(sortedList)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateSchedule(schedule: ScheduleResponse): Result<Unit> {
        return try {
            if (schedule.id.isEmpty()) {
                val docRef = firestore.collection("schedules").document()
                firestore.collection("schedules").document(docRef.id).set(schedule.copy(id = docRef.id)).await()
            } else {
                firestore.collection("schedules").document(schedule.id).set(schedule).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- EVENT LOGIC ---
    fun getEvents(): Flow<List<EventResponse>> = callbackFlow {
        val subscription = firestore.collection("events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(EventResponse::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createEvent(event: EventResponse): Result<Unit> {
        return try {
            val docRef = firestore.collection("events").document()
            firestore.collection("events").document(docRef.id).set(event.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSchoolInfo(): Flow<SchoolInfoResponse?> = callbackFlow {
        val subscription = firestore.collection("school_info").document("main")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(SchoolInfoResponse::class.java))
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateSchoolInfo(info: SchoolInfoResponse): Result<Unit> {
        return try {
            firestore.collection("school_info").document("main").set(info).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
