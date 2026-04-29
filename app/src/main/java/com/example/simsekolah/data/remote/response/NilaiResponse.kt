package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class NilaiResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String
)

