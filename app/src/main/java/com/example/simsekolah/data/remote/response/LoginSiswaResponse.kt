package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginSiswaResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class AbsensisItem(

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("siswaId")
	val siswaId: Int,

	@field:SerializedName("jadwalId")
	val jadwalId: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("uuid")
	val uuid: String,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)

data class Kelas(

	@field:SerializedName("waliKelasId")
	val waliKelasId: Int,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("uuid")
	val uuid: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)
data class LoginResult(
	@field:SerializedName("userId")
	val userId: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("token")
	val token: String,

	@field:SerializedName("role")
	val role: String // Pastikan isinya "murid" atau sesuai API
)
data class NilaisItem(

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("guruId")
	val guruId: Int,

	@field:SerializedName("nilai")
	val nilai: Int,

	@field:SerializedName("siswaId")
	val siswaId: Int,

	@field:SerializedName("mapelId")
	val mapelId: Int,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("uuid")
	val uuid: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)

data class DataItem(

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("password")
	val password: String,

	@field:SerializedName("nilais")
	val nilais: List<NilaisItem>,

	@field:SerializedName("kelas")
	val kelas: Kelas,

	@field:SerializedName("absensis")
	val absensis: List<AbsensisItem>,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("kelasId")
	val kelasId: Int,

	@field:SerializedName("isActive")
	val isActive: Boolean,

	@field:SerializedName("uuid")
	val uuid: String,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)
