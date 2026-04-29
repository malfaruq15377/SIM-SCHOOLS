package com.example.simsekolah.data.repository

import com.example.simsekolah.data.local.entity.AssignmentEntity
import com.example.simsekolah.data.local.entity.SubmissionEntity
import com.example.simsekolah.data.local.room.SekolahDao
import com.example.simsekolah.data.remote.response.*
import com.example.simsekolah.data.remote.retrofit.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SchoolRepository(
    private val apiService: ApiService,
    private val sekolahDao: SekolahDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    // --- ASSIGNMENTS via LOCAL (ROOM) ---
    suspend fun saveAssignmentLocal(assignment: AssignmentEntity) {
        sekolahDao.insertAssignment(assignment)
    }

    fun getLocalAssignments(guruId: Int): Flow<List<AssignmentItem>> {
        return sekolahDao.getAssignmentsByGuru(guruId).map { list ->
            list.map { entity ->
                AssignmentItem(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    dueDate = entity.dueDate,
                    fileUrl = entity.fileUrl,
                    guruId = entity.guruId,
                    kelasId = entity.kelasId
                )
            }
        }
    }

    // --- ASSIGNMENTS via FIRESTORE ---
    suspend fun saveAssignmentFirestore(assignment: AssignmentEntity) {
        val data = hashMapOf(
            "title" to assignment.title,
            "description" to assignment.description,
            "dueDate" to assignment.dueDate,
            "fileUrl" to assignment.fileUrl,
            "guruId" to assignment.guruId,
            "kelasId" to assignment.kelasId,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("assignments").add(data).await()
    }

    fun getAssignmentsFirestore(guruId: Int? = null, kelasId: Int? = null): Flow<List<AssignmentItem>> = callbackFlow {
        var query: Query = firestore.collection("assignments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        
        if (guruId != null) {
            query = query.whereEqualTo("guruId", guruId)
        }
        
        if (kelasId != null) {
            query = query.whereEqualTo("kelasId", kelasId)
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val list = snapshot?.map { doc ->
                AssignmentItem(
                    id = doc.id.hashCode(),
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    dueDate = doc.getString("dueDate") ?: "",
                    fileUrl = doc.getString("fileUrl"),
                    guruId = doc.getLong("guruId")?.toInt() ?: 0,
                    kelasId = doc.getLong("kelasId")?.toInt() ?: 0
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    // --- SUBMISSIONS via FIRESTORE ---
    suspend fun submitAssignmentFirestore(submission: SubmissionEntity) {
        val data = hashMapOf(
            "assignmentId" to submission.assignmentId,
            "siswaId" to submission.siswaId,
            "fileUrl" to submission.fileUrl,
            "submittedAt" to submission.submittedAt
        )
        firestore.collection("submissions").add(data).await()
    }

    fun getSubmissionsFirestore(assignmentId: Int): Flow<List<SubmissionItem>> = callbackFlow {
        val subscription = firestore.collection("submissions")
            .whereEqualTo("assignmentId", assignmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.map { doc ->
                    SubmissionItem(
                        id = doc.id.hashCode(),
                        assignmentId = doc.getLong("assignmentId")?.toInt() ?: 0,
                        siswaId = doc.getLong("siswaId")?.toInt() ?: 0,
                        fileUrl = doc.getString("fileUrl") ?: "",
                        submittedAt = doc.getString("submittedAt") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    fun getSubmissionsBySiswa(siswaId: Int): Flow<List<SubmissionItem>> = callbackFlow {
        val subscription = firestore.collection("submissions")
            .whereEqualTo("siswaId", siswaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.map { doc ->
                    SubmissionItem(
                        id = doc.id.hashCode(),
                        assignmentId = doc.getLong("assignmentId")?.toInt() ?: 0,
                        siswaId = doc.getLong("siswaId")?.toInt() ?: 0,
                        fileUrl = doc.getString("fileUrl") ?: "",
                        submittedAt = doc.getString("submittedAt") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    // --- REMOTE APIS ---
    fun getJadwal(kelasId: Int): Flow<BaseResponse<List<JadwalItem>>> = flow {
        emit(apiService.getJadwal(kelasId))
    }

    fun getPengumuman(): Flow<BaseResponse<List<PengumumanItem>>> = flow {
        emit(apiService.getPengumuman())
    }

    fun getSiswa(): Flow<BaseResponse<List<SiswaItem>>> = flow {
        emit(apiService.getSiswa())
    }

    fun getKelas(): Flow<KelasResponse> = flow {
        emit(apiService.getKelas())
    }

    fun getGuru(): Flow<BaseResponse<List<GuruItem>>> = flow {
        emit(apiService.getGuru())
    }
}
