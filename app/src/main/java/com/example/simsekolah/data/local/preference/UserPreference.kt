package com.example.simsekolah.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.simsekolah.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference(private val context: Context) {

    private val ID_KEY = intPreferencesKey("id")
    private val NAME_KEY = stringPreferencesKey("name")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val PHONE_KEY = stringPreferencesKey("phone")
    private val ADDRESS_KEY = stringPreferencesKey("address")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val IS_LOGIN_KEY = booleanPreferencesKey("is_login")
    private val EXTRA_KEY = stringPreferencesKey("extra")
    private val GENDER_KEY = stringPreferencesKey("gender")
    private val BIRTH_DATE_KEY = stringPreferencesKey("birth_date")
    private val IS_WALI_KELAS_KEY = booleanPreferencesKey("is_wali_kelas")
    private val KELAS_ID_KEY = intPreferencesKey("kelas_id")

    suspend fun saveSession(user: UserModel) {
        context.dataStore.edit { preferences ->
            preferences[ID_KEY] = user.id
            preferences[NAME_KEY] = user.name
            preferences[EMAIL_KEY] = user.email
            preferences[PHONE_KEY] = user.phone
            preferences[ADDRESS_KEY] = user.address
            preferences[ROLE_KEY] = user.role
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true
            preferences[EXTRA_KEY] = user.extraInfo ?: ""
            preferences[GENDER_KEY] = user.gender ?: ""
            preferences[BIRTH_DATE_KEY] = user.birthDate ?: ""
            preferences[IS_WALI_KELAS_KEY] = user.isWaliKelas
            preferences[KELAS_ID_KEY] = user.kelasId ?: 0
        }
    }

    fun getSession(): Flow<UserModel> {
        return context.dataStore.data.map { preferences ->
            UserModel(
                preferences[ID_KEY] ?: 0,
                preferences[NAME_KEY] ?: "",
                preferences[EMAIL_KEY] ?: "",
                preferences[PHONE_KEY] ?: "",
                preferences[ADDRESS_KEY] ?: "",
                preferences[ROLE_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[IS_LOGIN_KEY] ?: false,
                preferences[EXTRA_KEY],
                preferences[GENDER_KEY],
                preferences[BIRTH_DATE_KEY],
                preferences[IS_WALI_KELAS_KEY] ?: false,
                preferences[KELAS_ID_KEY]
            )
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        fun getInstance(context: Context): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
