package com.example.simsekolah.data.repository

import com.example.simsekolah.data.local.room.SekolahDao
import com.example.simsekolah.data.remote.request.LoginRequest
import com.example.simsekolah.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.flow

class SchoolRepository(
    private val apiService: ApiService,
    private val sekolahDao: SekolahDao
) {

    fun login(role: String, email: String, pass: String) = flow {
        val request = LoginRequest(email, pass) // Bungkus email & password ke JSON object
        emit(apiService.login(role, request))
    }

    fun getJadwal() = flow { 
        emit(apiService.getJadwal()) 
    }

    fun getSiswa() = flow { 
        emit(apiService.getSiswa()) 
    }

    fun getGuru() = flow {
        emit(apiService.getGuru())
    }

    fun getTugas() = flow {
        emit(apiService.getTugas())
    }

    fun getAbsensi() = flow {
        emit(apiService.getAbsensi())
    }

    fun getMapel() = flow {
        emit(apiService.getMapel())
    }

    fun getKelas() = flow {
        emit(apiService.getKelas())
    }

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null
        fun getInstance(apiService: ApiService, sekolahDao: SekolahDao): SchoolRepository =
            instance ?: synchronized(this) {
                instance ?: SchoolRepository(apiService, sekolahDao)
            }.also { instance = it }
    }
}
