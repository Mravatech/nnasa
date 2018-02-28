package com.mnassa.domain.interactor

import com.mnassa.domain.model.TranslatedWordModel

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryInteractor {
    suspend fun handleDictionaryUpdates()
    fun getWord(key: String): TranslatedWordModel

    val noInternetMessage: String
    val somethingWentWrongMessage: String
}