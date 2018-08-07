package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import kotlin.reflect.KProperty

/**
 * Created by Peter on 2/23/2018.
 */
class TranslatedWordModelImpl(
        private val languageProvider: LanguageProvider,
        override var id: String,
        override val info: String,
        override val engTranslate: String?,
        override val arabicTranslate: String?
) : TranslatedWordModel {

    constructor(languageProvider: LanguageProvider, id: String, info: String) : this(languageProvider, id, info, null, null)
    constructor(languageProvider: LanguageProvider, info: String) : this(languageProvider, info, info)

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): String = toString()

    override fun toString(): String = languageProvider.chooseTranslate(this)

    override fun equals(other: Any?): Boolean = other is TranslatedWordModel && id == other.id && info == other.info
}