package com.mnassa.domain.interactor

import com.mnassa.domain.models.TranslatedWord

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryInteractor {
    suspend fun handleDictionaryUpdates()
    fun getWord(key: String): TranslatedWord
}