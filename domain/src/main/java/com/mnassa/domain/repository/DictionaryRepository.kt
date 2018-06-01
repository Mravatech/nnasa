package com.mnassa.domain.repository

import com.mnassa.domain.model.TranslatedWordModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryRepository {
    fun getMobileUiVersion(): ReceiveChannel<Int>
    suspend fun loadDictionary(): List<TranslatedWordModel>

    fun getLocalDictionaryVersion(): Int
    fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWordModel>)
    fun getLocalWord(id: String): TranslatedWordModel
}