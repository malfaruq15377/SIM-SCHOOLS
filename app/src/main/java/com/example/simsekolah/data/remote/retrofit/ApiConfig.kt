package com.example.simsekolah.data.remote.retrofit

import android.content.Context
import com.example.simsekolah.data.local.preference.UserPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private const val BASE_URL = "https://school.petik.or.id/"

    fun getApiService(context: Context): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        
        val authInterceptor = Interceptor { chain ->
            val userPreference = UserPreference.getInstance(context)
            val token = runBlocking { userPreference.getSession().first().token }
            
            val req = chain.request()
            val requestHeaders = req.newBuilder()
            if (token.isNotEmpty()) {
                requestHeaders.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestHeaders.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
