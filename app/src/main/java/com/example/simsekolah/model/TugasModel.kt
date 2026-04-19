package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TugasModel(
    val id: String = System.currentTimeMillis().toString(),
    val deadline: String,
    val time: String,
    val title: String,
    val description: String,
    val fileName: String? = null,
    val filePath: String? = null,
    var isDone: Boolean = false
) : Parcelable