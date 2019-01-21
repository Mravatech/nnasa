package com.mnassa.domain.interactor

import com.mnassa.domain.model.TranslatedWordModel
import kotlinx.coroutines.experimental.Job

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryInteractor {
    fun handleDictionaryUpdates(): Job

    fun getWord(key: String): TranslatedWordModel
    fun getPlural(key: String, quantity: Int): TranslatedWordModel

    val noInternetMessage: String
    val somethingWentWrongMessage: String
}