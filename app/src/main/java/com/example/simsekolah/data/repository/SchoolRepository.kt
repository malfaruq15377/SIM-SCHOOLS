package com.example.simsekolah.data.repository

import com.example.simsekolah.data.local.room.SekolahDao
import com.example.simsekolah.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.flow

class SchoolRepository(
    private val apiService: ApiService,
    private val sekolahDao: SekolahDao
) {

    fun login(username: String, pass: String) = flow {
        emit(apiService.login(username, pass))
    }

    fun loginSiswa(username: String, pass: String) = flow {
        emit(apiService.loginSiswa(username, pass))
    }

    fun getMapel() = flow { emit(apiService.getMapel()) }
    fun getKelas() = flow { emit(apiService.getKelas()) }
    fun getJadwal() = flow { emit(apiService.getJadwal()) }
    fun getTugas() = flow { emit(apiService.getTugas()) }
    fun getGuru() = flow { emit(apiService.getGuru()) }
    fun getSiswa() = flow { emit(apiService.getSiswa()) }
    fun getNilai() = flow { emit(apiService.getNilai()) }
    fun getAbsensi() = flow { emit(apiService.getAbsensi()) }
    fun getAdmin() = flow { emit(apiService.getAdmin()) }
    fun getPengumuman() = flow { emit(apiService.getPengumuman()) }
    fun getSuperAdmin() = flow { emit(apiService.getSuperAdmin()) }

    fun getLocalSekolah() = sekolahDao.getAllSekolah()

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null
        fun getInstance(apiService: ApiService, sekolahDao: SekolahDao): SchoolRepository =
            instance ?: synchronized(this) {
                instance ?: SchoolRepository(apiService, sekolahDao)
            }.also { instance = it }
    }
}
