package com.mnassa.translation

import android.util.Log
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.plurals.PluralQuantityRules
import com.mnassa.domain.plurals.createPluralRules
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
        Log.d("TRANSLATION", "is_arabian=$isArabian")
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

    override fun getPluralRules(): PluralQuantityRules? = createPluralRules(locale)

    companion object {
        const val LANGUAGE_PREFERENCE = "LANGUAGE_PREFERENCE"
        const val LANGUAGE_SETTINGS = "LANGUAGE_SETTINGS"
    }
}