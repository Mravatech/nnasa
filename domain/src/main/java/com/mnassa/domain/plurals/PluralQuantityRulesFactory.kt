package com.mnassa.domain.plurals

import com.mnassa.domain.model.Plural
import com.seppius.i18n.plurals.PluralRules
import java.util.*

fun createPluralRules(locale: Locale): PluralQuantityRules? =
    PluralRules.ruleForLocale(locale)?.let(::PluralQuantityRulesAdapter)

/**
 * @author Artem Chepurnoy
 */
private class PluralQuantityRulesAdapter(private val rules: PluralRules) : PluralQuantityRules {

    override fun pluralOf(quantity: Int): Plural {
        val result = rules.quantityForNumber(quantity)
        return when (result) {
            PluralRules.QUANTITY_OTHER -> Plural.OTHER
            PluralRules.QUANTITY_ZERO -> Plural.ZERO
            PluralRules.QUANTITY_ONE -> Plural.ONE
            PluralRules.QUANTITY_TWO -> Plural.TWO
            PluralRules.QUANTITY_FEW -> Plural.FEW
            PluralRules.QUANTITY_MANY -> Plural.MANY
            else -> Plural.OTHER
        }
    }

}