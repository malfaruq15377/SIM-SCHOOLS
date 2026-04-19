package com.example.simsekolah.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.simsekolah.data.local.entity.SekolahEntity

@Database(entities = [SekolahEntity::class], version = 1, exportSchema = false)
abstract class SekolahDatabase : RoomDatabase() {
    abstract fun sekolahDao(): SekolahDao

    companion object {
        @Volatile
        private var INSTANCE: SekolahDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): SekolahDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SekolahDatabase::class.java, "sekolah_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
