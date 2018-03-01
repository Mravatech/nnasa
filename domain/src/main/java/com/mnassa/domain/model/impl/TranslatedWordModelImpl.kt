package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TranslatedWordModel
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by Peter on 2/23/2018.
 */
class TranslatedWordModelImpl(
        override var id: String,
        override val info: String,
        override val engTranslate: String?,
        override val arabicTranslate: String?
) : TranslatedWordModel {

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): String {
        val isoLanguage = Locale.getDefault().isO3Language

        return when {
            (isoLanguage == "ara" || isoLanguage == "ar") && !arabicTranslate.isNullOrBlank() -> arabicTranslate!!
            !engTranslate.isNullOrBlank() -> engTranslate!!
            else -> info
        }
    }
}