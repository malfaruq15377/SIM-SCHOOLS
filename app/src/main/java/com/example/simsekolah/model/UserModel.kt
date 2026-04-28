package com.example.simsekolah.model

data class UserModel(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val role: String, // "siswa" or "guru"
    val token: String,
    val isLogin: Boolean = false,
    val extraInfo: String? = null // For NIS/NIP or Class
)
