package com.mnassa.domain.other

import java.util.*

/**
 * Created by Peter on 2/26/2018.
 */
interface LanguageProvider {
    var locale: Locale
    val language: String
}