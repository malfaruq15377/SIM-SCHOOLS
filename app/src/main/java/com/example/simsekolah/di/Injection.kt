package com.example.simsekolah.di

import android.content.Context
import com.example.simsekolah.data.local.room.SekolahDatabase
import com.example.simsekolah.SchoolRepository
import com.example.simsekolah.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): SchoolRepository {
        val apiService = ApiConfig.getApiService()
        val database = SekolahDatabase.getInstance(context)
        val dao = database.sekolahDao()
        return SchoolRepository.getInstance(apiService, dao)
    }
}
