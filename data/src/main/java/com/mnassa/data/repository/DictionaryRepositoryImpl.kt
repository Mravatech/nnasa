package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.network.bean.firebase.TranslatedWordBean
import com.mnassa.data.preferences.DictionaryPreferences
import com.mnassa.domain.models.TranslatedWord
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryRepositoryImpl(
        private val databaseReference: DatabaseReference,
        private val converter: ConvertersContext,
        context: Context) : DictionaryRepository {

    private val dictionaryPreferences = DictionaryPreferences(context)

    override suspend fun getMobileUiVersion(): ReceiveChannel<Int> {
        return getObservable<Int>(databaseReference, path = "clientData", id = "mobileUiVersion").map { requireNotNull(it) }
    }

    override suspend fun loadDictionary(): List<TranslatedWord> {
        return async {
            val dictionary = get<TranslatedWordBean>(
                    databaseReference,
                    path = "dictionary/mobileUi"
            ).filter { !(it.eng.isNullOrBlank() && it.ar.isNullOrBlank()) }
            converter.convertCollection(dictionary, TranslatedWord::class.java)
        }.await()
    }

    override fun getLocalDictionaryVersion(): Int = dictionaryPreferences.getLocalDictionaryVersion()

    override fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWord>) = dictionaryPreferences.saveLocalDictionary(version, dictionary)

    override fun getLocalWord(id: String): TranslatedWord = dictionaryPreferences.getLocalWord(id)
}