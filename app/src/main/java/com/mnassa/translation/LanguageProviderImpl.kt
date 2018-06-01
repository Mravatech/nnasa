package com.mnassa.translation

import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
class LanguageProviderImpl : LanguageProvider {

    private val arabian = Locale("ar")
    private val english = Locale.ENGLISH
    override var locale: Locale
        get() = Locale.getDefault()
        set(value) {
            Locale.setDefault(value)
        }
    override val language: String
        get() = locale.language

    override fun changeLocale(): String {
        locale = when {
            isEnglish -> arabian
            else -> english
        }
        return language
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