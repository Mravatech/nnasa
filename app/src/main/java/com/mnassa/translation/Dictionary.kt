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
    val kodein by closestKodein(App.context)
    val dictionaryInteractor: DictionaryInteractor by kodein.instance()
    val result: String by dictionaryInteractor.getWord(key)
    return result.replace("%i", "%d")
}

fun fromDictionary(key: String, defaultValue: String): String {
    val result = fromDictionary(key)
    if (key.contains("userComment")){
        var i = 0
        i++
    }
    if (result.isBlank()){
        return defaultValue
    }
    return result
}