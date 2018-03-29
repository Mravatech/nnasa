package com.mnassa.domain.model

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by Peter on 2/23/2018.
 */
interface TranslatedWordModel : Model, ReadOnlyProperty<Nothing?, String> {
    val info: String
    val engTranslate: String?
    val arabicTranslate: String?
}

object EmptyWord : TranslatedWordModel {
    override var id: String = "EMPTY"
    override val info: String = ""
    override val engTranslate: String? = null
    override fun getValue(thisRef: Nothing?, property: KProperty<*>): String = ""
    override val arabicTranslate: String? = null
    override fun toString(): String = ""
}