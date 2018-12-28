package com.mnassa.domain.extensions

import com.mnassa.domain.model.Plural
import com.seppius.i18n.plurals.PluralRules
import timber.log.Timber
import java.util.*

private val QUANTITY_OTHER = 0x0000
private val QUANTITY_ZERO = 0x0001
private val QUANTITY_ONE = 0x0002
private val QUANTITY_TWO = 0x0004
private val QUANTITY_FEW = 0x0008
private val QUANTITY_MANY = 0x0010

fun PluralRules?.pluralOf(quantity: Int): Plural {
    if (this == null) {
        return Plural.OTHER
    }

    val result = PluralRules::class.java.getDeclaredMethod("quantityForNumber", Int::class.java)
        .run {
            isAccessible = true
            invoke(this@pluralOf, quantity)
        }

    return when (result) {
        QUANTITY_OTHER -> Plural.OTHER
        QUANTITY_ZERO -> Plural.ZERO
        QUANTITY_ONE -> Plural.ONE
        QUANTITY_TWO -> Plural.TWO
        QUANTITY_FEW -> Plural.FEW
        QUANTITY_MANY -> Plural.MANY
        else -> Plural.OTHER
    }
}

fun pluralRulesOf(locale: Locale): PluralRules? {
    return try {
        PluralRules::class.java.getDeclaredMethod("ruleForLocale", Locale::class.java)
            .run {
                isAccessible = true
                return@run invoke(null, locale) as PluralRules?
            }
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}
