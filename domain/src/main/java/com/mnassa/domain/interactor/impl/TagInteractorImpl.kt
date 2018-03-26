package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

class TagInteractorImpl(
        private val tagRepository: TagRepository
) : TagInteractor {
    override suspend fun createCustomTagIds(tags: List<String>): List<String> {
        return tagRepository.createCustomTagIds(tags)
    }

    override suspend fun search(searchKeyword: String): List<TagModel> {
        return tagRepository.search(searchKeyword)
    }

    override suspend fun getTagsByIds(ids: List<String>): List<TagModel> {
        return tagRepository.getTagsByIds(ids)
    }

}