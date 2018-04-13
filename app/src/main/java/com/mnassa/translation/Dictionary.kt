package com.mnassa.translation

import android.support.annotation.StringRes
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mnassa.App
import com.mnassa.domain.interactor.DictionaryInteractor

/**
 * Created by Peter on 24.02.2018.
 */
fun fromDictionary(@StringRes stringId: Int): String {
    return fromDictionary(App.context.getString(stringId))
}

fun fromDictionary(key: String): String {
    val ctx = App.context
    val dictionaryInteractor: DictionaryInteractor = ctx.appKodein.invoke().instance()
    val result: String by dictionaryInteractor.getWord(key)
    return result.replace("%i", "%d")
}