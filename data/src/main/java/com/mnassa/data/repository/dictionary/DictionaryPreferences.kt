package com.mnassa.data.repository.dictionary

import android.content.Context
import android.content.SharedPreferences
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.AppInfoProvider

/**
 * Created by Peter on 2/23/2018.
 */
internal class DictionaryPreferences(private val context: Context, private val appInfoProvider: AppInfoProvider) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences(DICTIONARY_PREFS, Context.MODE_PRIVATE)
    }

    fun getDictionaryVersion(): Int {
        return sharedPreferences.getInt(DICTIONARY_VERSION, -1)
    }

    fun saveDictionary(version: Int, dictionary: List<TranslatedWordModel>) {
        sharedPreferences.edit()
                .clear()
                .putInt(DICTIONARY_VERSION, version)
                .also { prefs -> dictionary.forEach { it.writeToPrefs(prefs) } }
                .apply()
    }

    fun getWord(id: String): TranslatedWordModel? {
        return readFromPrefs(sharedPreferences, id)
    }

    private fun TranslatedWordModel.writeToPrefs(sharedPrefs: SharedPreferences.Editor) {
        sharedPrefs.putString(id + "_ENG", engTranslate)
        sharedPrefs.putString(id + "_AR", arabicTranslate)
        sharedPrefs.putString(id + "_INFO", info)
    }

    private fun readFromPrefs(sharedPreferences: SharedPreferences, id: String): TranslatedWordModel? {
        val info = sharedPreferences.getString(id + "_INFO", null)
        if (info.isNullOrBlank()) return null

        return TranslatedWordModelImpl(
                id = id,
                engTranslate = sharedPreferences.getString(id + "_ENG", null),
                arabicTranslate = sharedPreferences.getString(id + "_AR", null),
                info = info)
    }

    private companion object {
        private const val DICTIONARY_PREFS = "DICTIONARY_PREFS"
        private const val DICTIONARY_VERSION = "DICTIONARY_VERSION"
    }
}