package com.example.simsekolah.data.remote.retrofit

import com.example.simsekolah.data.remote.response.*
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("login/siswa")
    suspend fun loginSiswa(
        @Body request: JsonObject
    ): SiswaResponse

    @POST("login/guru")
    suspend fun loginGuru(
        @Body request: JsonObject
    ): GuruResponse

    @GET("jadwal")
    suspend fun getJadwal(
        @Query("kelasId") kelasId: Int
    ): BaseResponse<List<JadwalItem>>

    @GET("pengumuman")
    suspend fun getPengumuman(): BaseResponse<List<PengumumanItem>>

    @GET("absensi")
    suspend fun getAbsensi(
        @Query("siswaId") siswaId: Int
    ): BaseResponse<List<AbsensiItem>>

    @POST("absensi")
    suspend fun postAbsensi(
        @Body request: JsonObject
    ): BaseResponse<AbsensiItem>

    @POST("absensi/bulk")
    suspend fun postAbsensiBulk(
        @Body request: JsonObject
    ): BaseResponse<String>

    @GET("nilai")
    suspend fun getNilai(
        @Query("siswaId") siswaId: Int
    ): BaseResponse<List<NilaiItem>>

    @GET("mapel")
    suspend fun getMapel(): BaseResponse<List<MapelItem>>

    @GET("kelas")
    suspend fun getKelas(): KelasResponse
    
    @GET("guru")
    suspend fun getGuru(): BaseResponse<List<GuruItem>>
    
    @GET("siswa")
    suspend fun getSiswa(): BaseResponse<List<SiswaItem>>

    @GET("assignments")
    suspend fun getAssignments(
        @Query("kelasId") kelasId: Int? = null,
        @Query("guruId") guruId: Int? = null,
        @Query("siswaId") siswaId: Int? = null
    ): BaseResponse<List<AssignmentItem>>

    @GET("assignments/submissions")
    suspend fun getSubmissions(
        @Query("assignmentId") assignmentId: Int
    ): BaseResponse<List<SubmissionItem>>

    @POST("assignments")
    suspend fun createAssignment(
        @Body request: JsonObject
    ): BaseResponse<AssignmentItem>

    @POST("assignments/submit")
    suspend fun submitAssignment(
        @Body request: JsonObject
    ): BaseResponse<SubmissionItem>
}
