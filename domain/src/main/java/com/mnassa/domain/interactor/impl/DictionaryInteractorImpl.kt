package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryInteractorImpl(repositoryLazy: () -> DictionaryRepository) : DictionaryInteractor {

    private val repository: DictionaryRepository by lazy(repositoryLazy)

    override suspend fun handleDictionaryUpdates() {
        try {
            repository.getMobileUiVersion().consumeEach { serverVersion ->
                async {
                    val mobileVersion = repository.getLocalDictionaryVersion()
                    if (serverVersion != mobileVersion) {
                        repository.saveLocalDictionary(serverVersion, repository.loadDictionary())
                    }
                }.await()
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