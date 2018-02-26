package com.mnassa.other

import android.support.annotation.StringRes
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mnassa.App
import com.mnassa.domain.interactor.DictionaryInteractor

/**
 * Created by Peter on 24.02.2018.
 */
fun fromDictionary(@StringRes stringId: Int): String {
    val ctx = App.context
    val dictionaryRepository: DictionaryInteractor = ctx.appKodein.invoke().instance()
    val key = ctx.getString(stringId)
    val result: String by dictionaryRepository.getWord(key)
    return result
}
