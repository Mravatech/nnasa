package com.mnassa.data.repository

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.mnassa.core.converter.ConvertersContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryRepositoryImpl(
        private val databaseReference: DatabaseReference,
        converterLazy: () -> ConvertersContext,
        private val dictionaryApi: FirebaseDictionaryApi,
        private val exceptionHandler: ExceptionHandler,
        languageProviderLazy: () -> LanguageProvider,
        context: Context,
        appInfoProvider: AppInfoProvider) : DictionaryRepository {

    private val converter by lazy(converterLazy)
    private val languageProvider: LanguageProvider by lazy(languageProviderLazy)
    private val dictionaryPreferences by lazy { DictionaryPreferences(context, languageProvider) }
    private val dictionaryResources by lazy { DictionaryResources(context, appInfoProvider, languageProvider) }

    override suspend fun produceDictionaryVersion(): ReceiveChannel<Int> {
        return databaseReference
                .child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_UI_VERSION)
                .toValueChannel<Int>(exceptionHandler)
                .map { requireNotNull(it) }
    }

    override fun keepDictionarySynced(keepSynced: Boolean) {
        databaseReference
            .child(DatabaseContract.TABLE_DICTIONARY)
            .child(DatabaseContract.TABLE_DICTIONARY_COL_MOBILE_UI)
            .keepSynced(keepSynced)
    }

    override suspend fun loadDictionary(): List<TranslatedWordModel> {
        return withContext(Dispatchers.Default) {
            val dictionary = databaseReference
                    .child(DatabaseContract.TABLE_DICTIONARY)
                    .child(DatabaseContract.TABLE_DICTIONARY_COL_MOBILE_UI)
                    .awaitList<TranslatedWordDbEntity>(exceptionHandler)
                    .filter { !(it.info.isNullOrBlank() && it.en.isNullOrBlank() && it.ar.isNullOrBlank()) }
            converter.convertCollection(dictionary, TranslatedWordModel::class.java)
        }
    }

    override fun getLocalDictionaryVersion(): Int = dictionaryPreferences.getDictionaryVersion()

    override fun saveLocalDictionary(version: Int, dictionary: List<TranslatedWordModel>) {
        dictionaryPreferences.saveDictionary(version, dictionary)
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
        try {
            dictionaryApi.registerUiKey(RegisterUiKeyRequest(id, info ?: id))
        } catch (e: Exception) {
            //ignore all exceptions here
            Timber.e(e)
        }
    }

}