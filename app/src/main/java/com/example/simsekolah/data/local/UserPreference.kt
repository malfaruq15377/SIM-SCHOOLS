package com.example.simsekolah.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.simsekolah.data.model.UserModel

class UserPreference(context: Context) {
    companion object {
        private const val PREFS_NAME = "user_pref"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val AGE = "age"
        private const val NO_PHONE = "phone"
        private const val ADDRESS = "address"
        private const val MAJOR = "major"
        private const val FATHER_NAME = "father_name"
        private const val MOTHER_NAME = "mother_name"
        private const val WEIGHT = "weight"
        private const val HEIGHT = "height"
        private const val DATE_OF_BIRTH = "date_of_birth"
        private const val ROLE = "role"
    }

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setUser(value: UserModel) {
        preferences.edit {
            putString(NAME, value.name)
            putString(EMAIL, value.email)
            putInt(AGE, value.age)
            putString(NO_PHONE, value.noPhone)
            putString(ADDRESS, value.address)
            putString(MAJOR, value.major)
            putString(FATHER_NAME, value.fatherName)
            putString(MOTHER_NAME, value.motherName)
            putFloat(WEIGHT, value.weight.toFloat())
            putFloat(HEIGHT, value.height.toFloat())
            putString(DATE_OF_BIRTH, value.dateOfBirth)
            putString(ROLE, value.role)
        }
    }

    fun getUser(): UserModel {
        return UserModel(
            name = preferences.getString(NAME, ""),
            email = preferences.getString(EMAIL, ""),
            age = preferences.getInt(AGE, 0),
            noPhone = preferences.getString(NO_PHONE, ""),
            address = preferences.getString(ADDRESS, ""),
            major = preferences.getString(MAJOR, ""),
            fatherName = preferences.getString(FATHER_NAME, ""),
            motherName = preferences.getString(MOTHER_NAME, ""),
            weight = preferences.getFloat(WEIGHT, 0f).toDouble(),
            height = preferences.getFloat(HEIGHT, 0f).toDouble(),
            dateOfBirth = preferences.getString(DATE_OF_BIRTH, ""),
            role = preferences.getString(ROLE, "")
        )
    }

    fun logout() {
        preferences.edit { clear() }
    }
}