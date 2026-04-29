package com.example.simsekolah.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simsekolah.data.local.entity.AssignmentEntity
import com.example.simsekolah.data.local.entity.NotificationModel
import com.example.simsekolah.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SekolahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationModel)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    // Assignments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Query("SELECT * FROM assignments_local ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments_local WHERE guruId = :guruId ORDER BY id DESC")
    fun getAssignmentsByGuru(guruId: Int): Flow<List<AssignmentEntity>>

    @Query("DELETE FROM assignments_local WHERE id = :id")
    suspend fun deleteAssignment(id: Int)

    // Submissions (Local)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    @Query("SELECT * FROM submissions_local WHERE assignmentId = :assignmentId")
    fun getSubmissionsByAssignment(assignmentId: Int): Flow<List<SubmissionEntity>>
}
