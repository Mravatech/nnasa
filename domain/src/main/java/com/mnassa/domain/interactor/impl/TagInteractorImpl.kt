package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.TagRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

class TagInteractorImpl(
        private val userRepository: TagRepository
        ) : TagInteractor {

    override suspend fun search(search: String): List<TagModel> {
        return userRepository.search(search)
    }
}