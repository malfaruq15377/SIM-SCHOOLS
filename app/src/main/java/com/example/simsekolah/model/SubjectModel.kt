package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubjectModel(
    val id: String = "",
    val name: String = "",
    val time: String = "",
    val guruName: String = "",
    val day: String = ""
) : Parcelable
