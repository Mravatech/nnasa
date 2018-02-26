package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.bean.firebase.TranslatedWordBean
import com.mnassa.data.repository.dictionary.DictionaryPreferences
import com.mnassa.data.repository.dictionary.DictionaryResources
import com.mnassa.domain.model.EmptyWord
import com.mnassa.domain.model.TranslatedWordModel
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
        return databaseReference.child("clientData").child("mobileUiVersion").toValueChannel<Int>().map { requireNotNull(it) }
    }

    override suspend fun loadDictionary(): List<TranslatedWordModel> {
        return async {
            val dictionary = databaseReference.child("dictionary").child("mobileUi")
                    .awaitList<TranslatedWordBean>()
                    .filter { !(it.info.isBlank() && it.en.isNullOrBlank() && it.ar.isNullOrBlank()) }
            converter.convertCollection(dictionary, TranslatedWordModel::class.java)
        }.await()
    }

    override fun getLocalDictionaryVersion(): Int = dictionaryPreferences.getDictionaryVersion()

    override fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWordModel>) {
        dictionaryPreferences.saveDictionary(version, dictionary)
        dictionaryResources.print(dictionary)
    }

    override fun getLocalWord(id: String): TranslatedWordModel =
            dictionaryPreferences.getWord(id) ?: dictionaryResources.getWord(id) ?: EmptyWord
}