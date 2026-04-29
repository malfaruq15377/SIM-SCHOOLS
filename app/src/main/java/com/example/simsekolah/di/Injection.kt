package com.example.simsekolah.di

import android.content.Context
import com.example.simsekolah.data.local.room.SekolahDatabase
import com.example.simsekolah.data.remote.retrofit.ApiConfig
import com.example.simsekolah.data.repository.SchoolRepository

object Injection {
    fun provideRepository(context: Context): SchoolRepository {
        val apiService = ApiConfig.getApiService(context)
        val database = SekolahDatabase.getDatabase(context)
        return SchoolRepository(apiService, database.sekolahDao())
    }
}
