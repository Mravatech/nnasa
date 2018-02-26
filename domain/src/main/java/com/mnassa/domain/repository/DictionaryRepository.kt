package com.mnassa.domain.repository

import com.mnassa.domain.models.TranslatedWord
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/23/2018.
 */
interface DictionaryRepository {
    suspend fun getMobileUiVersion(): ReceiveChannel<Int>
    suspend fun loadDictionary(): List<TranslatedWord>

    fun getLocalDictionaryVersion(): Int
    fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWord>)
    fun getLocalWord(id: String): TranslatedWord
}