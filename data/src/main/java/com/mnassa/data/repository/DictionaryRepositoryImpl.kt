package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.network.bean.firebase.TranslatedWordBean
import com.mnassa.data.repository.dictionary.DictionaryPreferences
import com.mnassa.data.repository.dictionary.DictionaryResources
import com.mnassa.domain.models.EmptyWord
import com.mnassa.domain.models.TranslatedWord
import com.mnassa.domain.other.AppInfoProvider
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
        context: Context,
        appInfoProvider: AppInfoProvider) : DictionaryRepository {

    private val dictionaryPreferences = DictionaryPreferences(context, appInfoProvider)
    private val dictionaryResources = DictionaryResources(context, appInfoProvider)

    override suspend fun getMobileUiVersion(): ReceiveChannel<Int> {
        return getObservable<Int>(databaseReference, path = "clientData", id = "mobileUiVersion").map { requireNotNull(it) }
    }

    override suspend fun loadDictionary(): List<TranslatedWord> {
        return async {
            val dictionary = get<TranslatedWordBean>(
                    databaseReference,
                    path = "dictionary/mobileUi"
            ).filter { !(it.info.isBlank() && it.en.isNullOrBlank() && it.ar.isNullOrBlank()) }
            converter.convertCollection(dictionary, TranslatedWord::class.java)
        }.await()
    }

    override fun getLocalDictionaryVersion(): Int = dictionaryPreferences.getDictionaryVersion()

    override fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWord>) {
        dictionaryPreferences.saveDictionary(version, dictionary)
        dictionaryResources.print(dictionary)
    }

    override fun getLocalWord(id: String): TranslatedWord =
            dictionaryPreferences.getWord(id) ?: dictionaryResources.getWord(id) ?: EmptyWord
}