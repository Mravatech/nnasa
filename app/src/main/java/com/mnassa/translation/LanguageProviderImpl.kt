package com.mnassa.translation

import com.mnassa.domain.extensions.pluralRulesOf
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import com.seppius.i18n.plurals.PluralRules
import timber.log.Timber
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
class LanguageProviderImpl : LanguageProvider {

    private val arabian = Locale("ar")
    private val english = Locale.ENGLISH
    override var locale: Locale
        get() = Locale.getDefault().arabianOrEnglish()
        set(value) {
            Locale.setDefault(value.arabianOrEnglish())
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

    private fun Locale.arabianOrEnglish(): Locale {
        return if (language == english.language || language == arabian.language) this
        else english
    }

    override fun getPluralRules(): PluralRules? = pluralRulesOf(locale)

    companion object {
        const val LANGUAGE_PREFERENCE = "LANGUAGE_PREFERENCE"
        const val LANGUAGE_SETTINGS = "LANGUAGE_SETTINGS"
    }
}