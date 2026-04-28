package com.example.simsekolah.data.remote.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("msg") // Beberapa endpoint mungkin pakai 'msg'
    val msg: String? = null,
    
    @SerializedName("data")
    val data: T? = null
)
