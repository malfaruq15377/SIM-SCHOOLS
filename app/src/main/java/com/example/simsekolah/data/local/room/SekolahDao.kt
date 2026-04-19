package com.example.simsekolah.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simsekolah.data.local.entity.SekolahEntity

@Dao
interface SekolahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSekolah(sekolah: List<SekolahEntity>)

    @Query("SELECT * FROM sekolah")
    fun getAllSekolah(): LiveData<List<SekolahEntity>>

    @Query("SELECT * FROM sekolah WHERE type = :type")
    fun getSekolahByType(type: String): LiveData<List<SekolahEntity>>

    @Query("DELETE FROM sekolah")
    suspend fun deleteAll()
}
