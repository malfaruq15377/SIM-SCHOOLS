package com.example.simsekolah.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var address: String? = null,
    var major: String? = null,
    var fatherName: String? = null,
    var motherName: String? = null,
    var height: Double = 0.0,
    var weight: Double = 0.0,
    var dateOfBirth: String? = null,
    var noPhone: String? = null,
    var age: Int = 0,
    var image: String? = null,
    var role: String? = null,
    var token: String? = null
): Parcelable