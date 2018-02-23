package com.mnassa.domain.models

import kotlin.properties.ReadOnlyProperty

/**
 * Created by Peter on 2/23/2018.
 */
interface TranslatedWord : Model, ReadOnlyProperty<String, String> {
    val engTranslate: String?
    val arabicTranslate: String?
}