package com.mnassa.translation

import android.content.Context
import android.content.SharedPreferences
import com.mnassa.App.Companion.context
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
class LanguageProviderImpl : LanguageProvider {

    @Transient
    private var prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
    }

    private val arabian = Locale("ar")
    private val english = Locale.ENGLISH
    override var locale: Locale
        get() {
            val lang = prefs.getString(LANGUAGE_SETTINGS, null) ?: return Locale.getDefault()
            return Locale(lang)
        }
        set(value) {
            Locale.setDefault(value)
            prefs.edit().putString(LANGUAGE_SETTINGS, value.language).apply()
        }
    override val language: String
        get() = locale.language

    override fun changeLocale() {
        locale = when {
            isEnglish -> arabian
            else -> english
        }
    }

    override fun chooseTranslate(word: TranslatedWordModel): String {
        return with(word) {
            when {
                isArabian && !arabicTranslate.isNullOrBlank() -> arabicTranslate!!
                !engTranslate.isNullOrBlank() -> engTranslate!!
                else -> info
            }
        }
    }

    companion object {
        const val LANGUAGE_PREFERENCE = "LANGUAGE_PREFERENCE"
        const val LANGUAGE_SETTINGS = "LANGUAGE_SETTINGS"
    }
}