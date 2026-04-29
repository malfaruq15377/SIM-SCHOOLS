package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class SiswaResponse(
	@field:SerializedName("success") val success: Boolean,
	@field:SerializedName("message") val message: String,
	@field:SerializedName("data") val data: SiswaLoginData
)

data class SiswaLoginData(
	@field:SerializedName("user") val user: SiswaItem,
	@field:SerializedName("token") val token: String
)

data class DataItem(

	@field:SerializedName("address")
	val address: String,

	@field:SerializedName("gender")
	val gender: String,

	@field:SerializedName("kelasId")
	val kelasId: Int,

	@field:SerializedName("isActive")
	val isActive: Boolean,

	@field:SerializedName("uuid")
	val uuid: String,

	@field:SerializedName("birthDate")
	val birthDate: String,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("password")
	val password: String,

	@field:SerializedName("parentName")
	val parentName: String,

	@field:SerializedName("deletedAt")
	val deletedAt: Any,

	@field:SerializedName("phone")
	val phone: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("nis")
	val nis: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("updatedAt")
	val updatedAt: String
)
