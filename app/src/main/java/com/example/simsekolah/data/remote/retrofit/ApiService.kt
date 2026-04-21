package com.example.simsekolah.data.remote.retrofit

import com.example.simsekolah.data.remote.response.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("guru")
    suspend fun getGuru(): GuruResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse
    
    @FormUrlEncoded
    @POST("login")
    suspend fun loginSiswa(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("mapel")
    suspend fun getMapel(): MapelResponse

    @GET("kelas")
    suspend fun getKelas(): KelasResponse

    @GET("jadwal")
    suspend fun getJadwal(): JadwalResponse

    @GET("tugas")
    suspend fun getTugas(): TugasResponse

    @GET("siswa")
    suspend fun getSiswa(): SiswaResponse

    @GET("nilai")
    suspend fun getNilai(): Any

    @GET("absensi")
    suspend fun getAbsensi(): AbsensiResponse

    @GET("admin")
    suspend fun getAdmin(): Any

    @GET("pengumuman")
    suspend fun getPengumuman(): Any

    @GET("superadmin")
    suspend fun getSuperAdmin(): Any
}
