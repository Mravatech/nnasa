package com.mnassa.domain.other

import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
interface LanguageProvider {
    var locale: Locale
    val language: String

    val isArabian: Boolean
        get() {
            return (language == "ara" || language == "ar")
        }
    val isEnglish: Boolean
        get() = !isArabian

}