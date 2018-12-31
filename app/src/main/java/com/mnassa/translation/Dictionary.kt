package com.mnassa.translation

import android.support.annotation.StringRes
import com.mnassa.App
import com.mnassa.domain.interactor.DictionaryInteractor
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

/**
 * Created by Peter on 24.02.2018.
 */
fun fromDictionary(@StringRes stringId: Int): String {
    return fromDictionary(App.context.getString(stringId))
}

fun fromDictionary(key: String): String {
    val result: String by withDictionaryInteractor { getWord(key) }
    return result.processAttrs()
}

fun fromDictionary(key: String, defaultValue: String): String {
    val result = fromDictionary(key)
    if (result.isBlank()) {
        return defaultValue
    }
    return result
}

// Plural

fun fromDictionaryPlural(@StringRes stringId: Int, quantity: Int): String {
    return fromDictionaryPlural(App.context.getString(stringId), quantity)
}

fun fromDictionaryPlural(key: String, quantity: Int): String {
    val result: String by withDictionaryInteractor { getPlural(key, quantity) }
    return result.processAttrs()
}

// Other

private inline fun <T> withDictionaryInteractor(
    crossinline block: DictionaryInteractor.() -> T
): T {
    val kodein by closestKodein(App.context)
    val dictionaryInteractor: DictionaryInteractor by kodein.instance()
    return dictionaryInteractor.block()
}

private fun String.processAttrs() = replace("%i", "%d").replace("%@", "%s")
