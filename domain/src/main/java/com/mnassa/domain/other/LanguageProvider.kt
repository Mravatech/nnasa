package com.mnassa.domain.other

import com.mnassa.domain.model.TranslatedWordModel
import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
interface LanguageProvider : Serializable {
    var locale: Locale
    val language: String

    val isArabian: Boolean get() = (language == "ara" || language == "ar")
    val isEnglish: Boolean get() = !isArabian

    fun chooseTranslate(word: TranslatedWordModel): String

}