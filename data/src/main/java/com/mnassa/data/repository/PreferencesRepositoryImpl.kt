package com.mnassa.data.repository

import android.content.Context
import androidx.content.edit
import com.mnassa.domain.repository.PreferencesRepository

/**
 * Created by Peter on 7/24/2018.
 */
class PreferencesRepositoryImpl(private val context: Context) : PreferencesRepository {

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun saveString(key: String, value: String?) {
        prefs.edit {
            putString(key, value)
        }
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun saveLong(key: String, value: Long) {
        prefs.edit {
            putLong(key, value)
        }
    }

    override fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)

    companion object {
        private const val PREFS_NAME = "MNASSA_PREFS"
    }
}