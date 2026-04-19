package com.example.simsekolah.data.remote.retrofit

import com.example.simsekolah.data.remote.response.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("siswa/login")
    suspend fun loginSiswa(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("mapel")
    suspend fun getMapel(): BaseResponse<List<MapelItem>>

    @GET("kelas")
    suspend fun getKelas(): BaseResponse<List<KelasItem>>

    @GET("jadwal")
    suspend fun getJadwal(): BaseResponse<List<JadwalItem>>

    @GET("guru")
    suspend fun getGuru(): GuruResponse

    @GET("siswa")
    suspend fun getSiswa(): BaseResponse<List<MuridItem>>

    @GET("nilai")
    suspend fun getNilai(): BaseResponse<List<NilaiItem>>

    @GET("absensi")
    suspend fun getAbsensi(): BaseResponse<List<AbsensiItem>>

    @GET("admin")
    suspend fun getAdmin(): BaseResponse<List<AdminItem>>

    @GET("pengumuman")
    suspend fun getPengumuman(): BaseResponse<List<PengumumanItem>>

    @GET("superadmin")
    suspend fun getSuperAdmin(): BaseResponse<List<SuperAdminItem>>

    @FormUrlEncoded
    @POST("{role}/update-password/{id}")
    suspend fun updatePassword(
        @Path("role") role: String,
        @Path("id") id: String,
        @Field("new_password") newPassword: String
    ): BaseResponse<String>
}
