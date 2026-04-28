package com.example.simsekolah.data.remote.retrofit

import com.example.simsekolah.data.remote.response.LoginGuruResponse
import com.example.simsekolah.data.remote.response.LoginSiswaResponse
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login/siswa")
    suspend fun loginSiswa(
        @Body request: JsonObject
    ): LoginSiswaResponse

    @POST("login/guru")
    suspend fun loginGuru(
        @Body request: JsonObject
    ): LoginGuruResponse
}
