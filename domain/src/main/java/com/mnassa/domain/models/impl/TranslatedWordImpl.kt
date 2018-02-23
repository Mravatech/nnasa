package com.mnassa.domain.models.impl

import com.mnassa.domain.models.TranslatedWord
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by Peter on 2/23/2018.
 */
class TranslatedWordImpl(override var id: String, override val engTranslate: String?, override val arabicTranslate: String?) : TranslatedWord {

    override fun getValue(thisRef: String, property: KProperty<*>): String {
        //TODO: handle language changes

        val isoLanguage = Locale.getDefault().isO3Language
        return if ((isoLanguage == "ara" || isoLanguage == "ar") && !arabicTranslate.isNullOrEmpty()) {
            arabicTranslate!!
        } else engTranslate ?: ""
    }
}