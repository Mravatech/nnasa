package com.mnassa.domain.model

import kotlin.properties.ReadOnlyProperty

/**
 * Created by Peter on 2/23/2018.
 */
interface TranslatedWordModel : Model, ReadOnlyProperty<Nothing?, String> {
    val info: String
    val engTranslate: String?
    val arabicTranslate: String?
}