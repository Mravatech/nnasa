package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.api.FirebaseDictionaryApi
import com.mnassa.data.network.bean.firebase.TranslatedWordDbEntity
import com.mnassa.data.network.bean.retrofit.request.RegisterUiKeyRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.repository.dictionary.DictionaryPreferences
import com.mnassa.data.repository.dictionary.DictionaryResources
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryRepositoryImpl(
        private val databaseReference: DatabaseReference,
        private val converter: ConvertersContext,
        private val dictionaryApi: FirebaseDictionaryApi,
        private val exceptionHandler: ExceptionHandler,
        private val languageProvider: LanguageProvider,
        context: Context,
        appInfoProvider: AppInfoProvider) : DictionaryRepository {

    private val dictionaryPreferences = DictionaryPreferences(context, languageProvider)
    private val dictionaryResources = DictionaryResources(context, appInfoProvider, languageProvider)

    override fun getMobileUiVersion(): ReceiveChannel<Int> {
        return databaseReference
                .child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_UI_VERSION)
                .toValueChannel<Int>(exceptionHandler)
                .map { requireNotNull(it) }
    }

    override suspend fun loadDictionary(): List<TranslatedWordModel> {
        return async {
            val dictionary = databaseReference
                    .child(DatabaseContract.TABLE_DICTIONARY)
                    .child(DatabaseContract.TABLE_DICTIONARY_COL_MOBILE_UI)
                    .awaitList<TranslatedWordDbEntity>(exceptionHandler)
                    .filter { !(it.info.isBlank() && it.en.isNullOrBlank() && it.ar.isNullOrBlank()) }
            converter.convertCollection(dictionary, TranslatedWordModel::class.java)
        }.await()
    }

    override fun getLocalDictionaryVersion(): Int = dictionaryPreferences.getDictionaryVersion()

    override fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWordModel>) {
        dictionaryPreferences.saveDictionary(version, dictionary)
        dictionaryResources.print(dictionary)
    }

    override fun getLocalWord(id: String): TranslatedWordModel {
        //1.
        val fromPrefs = dictionaryPreferences.getWord(id)
        if (fromPrefs != null) {
            return fromPrefs
        }

        //2.
        val fromResources = dictionaryResources.getWord(id)
        if (fromResources != null) {
            registerWord(id, fromResources.info)
            return fromResources
        }

        //3.
        registerWord(id)
        return TranslatedWordModelImpl(languageProvider, id, id)
    }

    private fun registerWord(id: String, info: String? = null) {
        Timber.i("REGISTER_WORD: id: $id; info: $info")
        dictionaryApi.registerUiKey(RegisterUiKeyRequest(id, info ?: id))
    }

}