package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.model.Plural
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.DictionaryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class DictionaryInteractorImpl(
    repositoryLazy: () -> DictionaryRepository,
    languageProviderLazy: () -> LanguageProvider
) : DictionaryInteractor {

    private val repository: DictionaryRepository by lazy(repositoryLazy)

    private val languageProvider: LanguageProvider by lazy(languageProviderLazy)

    override fun CoroutineScope.handleDictionaryUpdates() {
        launchWorker {
            repository.keepDictionarySynced(true)
            repository.produceDictionaryVersion().consumeEach { serverVersion ->
                withContext(Dispatchers.Default) {
                    val mobileVersion = repository.getLocalDictionaryVersion()
                    if (serverVersion != mobileVersion) {
                        val serverDict = repository.loadDictionary()
                        repository.saveLocalDictionary(serverVersion, serverDict)
                    }
                }
            }
        }
    }

    override fun getWord(key: String): TranslatedWordModel = repository.getLocalWord(key)

    override fun getPlural(key: String, quantity: Int): TranslatedWordModel {
        val plural = languageProvider.getPluralRules()?.pluralOf(quantity) ?: Plural.OTHER
        return getWord(key + plural.suffix).takeUnless { it.toString().isBlank() }
            ?: getWord(key + Plural.OTHER.suffix)
    }

    //TODO: add translations
    override val noInternetMessage: String = "NO INTERNET!"
    override val somethingWentWrongMessage: String = "SOMETHING WENT WRONG!"
}