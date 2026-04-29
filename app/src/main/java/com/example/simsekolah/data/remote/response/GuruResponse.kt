package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class GuruResponse(
	@field:SerializedName("success") val success: Boolean,
	@field:SerializedName("message") val message: String,
	@field:SerializedName("data") val data: GuruLoginData
)

data class GuruLoginData(
	@field:SerializedName("user") val user: GuruItem,
	@field:SerializedName("token") val token: String
)