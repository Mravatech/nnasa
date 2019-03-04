package com.mnassa.domain.repository

import com.mnassa.domain.model.TranslatedWordModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryRepository {
    suspend fun produceDictionaryVersion(): ReceiveChannel<Int>

    fun keepDictionarySynced(keepSynced: Boolean)
    suspend fun loadDictionary(): List<TranslatedWordModel>

    fun getLocalDictionaryVersion(): Int
    fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWordModel>)
    fun getLocalWord(id: String): TranslatedWordModel
}