package com.mnassa.domain.plurals

import com.mnassa.domain.model.Plural

/**
 * @author Artem Chepurnoy
 */
interface PluralQuantityRules {
    fun pluralOf(quantity: Int): Plural
}
