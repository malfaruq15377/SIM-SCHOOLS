package com.example.simsekolah.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.simsekolah.model.NotificationModel

@Database(entities = [NotificationModel::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
