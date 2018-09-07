package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryInteractorImpl(repositoryLazy: () -> DictionaryRepository) : DictionaryInteractor {

    private val repository: DictionaryRepository by lazy(repositoryLazy)

    override suspend fun handleDictionaryUpdates() {
        try {
            repository.getMobileUiVersion().consumeEach { serverVersion ->
                withContext(DefaultDispatcher) {
                    val mobileVersion = repository.getLocalDictionaryVersion()
                    if (serverVersion != mobileVersion) {
                        repository.saveLocalDictionary(serverVersion, repository.loadDictionary())
                    }
                }
            }
        } catch (e: Exception) {
            //must never happen
            Timber.e(e)
            delay(5_000L)
            return handleDictionaryUpdates()
        }
    }

    override fun getWord(key: String): TranslatedWordModel = repository.getLocalWord(key)

    //TODO: add translations
    override val noInternetMessage: String = "NO INTERNET!"
    override val somethingWentWrongMessage: String = "SOMETHING WENT WRONG!"
}