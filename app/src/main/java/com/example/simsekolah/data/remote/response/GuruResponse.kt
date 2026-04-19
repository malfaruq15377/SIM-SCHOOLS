package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class GuruResponse(
	@SerializedName("data")
	val data: List<GuruItem>,

	@SerializedName("success")
	val success: Boolean,

	@SerializedName("message")
	val message: String
)