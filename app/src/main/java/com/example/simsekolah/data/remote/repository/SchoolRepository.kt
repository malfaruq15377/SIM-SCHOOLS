package com.example.simsekolah.data.remote.repository

import com.example.simsekolah.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.flow

class SchoolRepository(private val apiService: ApiService) {

    fun getMapel() = flow { emit(apiService.getMapel()) }
    fun getKelas() = flow { emit(apiService.getKelas()) }
    fun getJadwal() = flow { emit(apiService.getJadwal()) }
    fun getGuru() = flow { emit(apiService.getGuru()) }
    fun getSiswa() = flow { emit(apiService.getSiswa()) }
    fun getNilai() = flow { emit(apiService.getNilai()) }
    fun getAbsensi() = flow { emit(apiService.getAbsensi()) }
    fun getAdmin() = flow { emit(apiService.getAdmin()) }
    fun getPengumuman() = flow { emit(apiService.getPengumuman()) }
    fun getSuperAdmin() = flow { emit(apiService.getSuperAdmin()) }

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null
        fun getInstance(apiService: ApiService): SchoolRepository =
            instance ?: synchronized(this) {
                instance ?: SchoolRepository(apiService)
            }.also { instance = it }
    }
}
