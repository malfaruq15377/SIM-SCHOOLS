package com.example.simsekolah.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.simsekolah.data.local.entity.AssignmentEntity
import com.example.simsekolah.data.local.entity.NotificationModel
import com.example.simsekolah.data.local.entity.SubmissionEntity

@Database(
    entities = [NotificationModel::class, AssignmentEntity::class, SubmissionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SekolahDatabase : RoomDatabase() {
    
    abstract fun sekolahDao(): SekolahDao

    companion object {
        @Volatile
        private var INSTANCE: SekolahDatabase? = null

        fun getDatabase(context: Context): SekolahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SekolahDatabase::class.java,
                    "sekolah_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
