package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.models.TranslatedWord
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryInteractorImpl(private val repository: DictionaryRepository) : DictionaryInteractor {

    override suspend fun handleDictionaryUpdates() {
        try {
            repository.getMobileUiVersion().consumeEach { serverVersion ->
                val mobileVersion = repository.getLocalDictionaryVersion()
                if (serverVersion != mobileVersion) {
                    repository.saveLocalDictionary(serverVersion, repository.loadDictionary())
                }
            }
        } catch (e: Exception) {
            //TODO: ask Vlad about permissions
            //must never happen
            Timber.e(e)
            delay(5_000L)
            return handleDictionaryUpdates()
        }
    }

    override fun getWord(key: String): TranslatedWord = repository.getLocalWord(key)
}