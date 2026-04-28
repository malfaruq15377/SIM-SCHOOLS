package com.example.simsekolah.data.remote.retrofit

import com.example.simsekolah.data.remote.response.*
import com.example.simsekolah.data.remote.request.LoginRequest
import retrofit2.http.*

interface ApiService {
    @POST("/login/{role}")
    suspend fun login(
        @Path("role") role: String,
        @Body request: LoginRequest
    ): BaseResponse<LoginData>

    @GET("guru")
    suspend fun getGuru(): BaseResponse<List<GuruItem>>

    @GET("siswa")
    suspend fun getSiswa(): BaseResponse<List<SiswaItem>>

    @GET("jadwal")
    suspend fun getJadwal(): BaseResponse<List<JadwalItem>>

    @GET("tugas")
    suspend fun getTugas(): BaseResponse<List<TugasItem>>

    @GET("absensi")
    suspend fun getAbsensi(): BaseResponse<List<AbsensiItem>>
    
    @GET("mapel")
    suspend fun getMapel(): BaseResponse<List<MapelItem>>

    @GET("kelas")
    suspend fun getKelas(): BaseResponse<List<KelasItem>>
}
