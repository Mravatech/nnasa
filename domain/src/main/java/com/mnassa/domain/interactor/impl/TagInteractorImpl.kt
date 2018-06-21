package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

class TagInteractorImpl(private val tagRepository: TagRepository,
                        private val profileInteractor: UserProfileInteractor) : TagInteractor {

    override suspend fun get(id: String): TagModel? = tagRepository.get(id)

    override suspend fun getAll(): List<TagModel> = tagRepository.getAll()

    override suspend fun createCustomTagIds(tags: List<String>): List<String> {
        return tagRepository.createCustomTagIds(tags)
    }

    override suspend fun search(searchKeyword: String): List<TagModel> {
        return tagRepository.search(searchKeyword)
    }

    override suspend fun getTagsByIds(ids: List<String>): List<TagModel> {
        return tagRepository.getTagsByIds(ids)
    }

    override suspend fun shouldShowAddTagsDialog(): Boolean {
        val intervalMillis = tagRepository.getAddTagsDialogInterval() ?: return false
        val lastShowingTime = tagRepository.getAddTagsDialogLastShowingTime()
        if (lastShowingTime == null || lastShowingTime.time < (System.currentTimeMillis() - intervalMillis)) {
            val userProfile = profileInteractor.getProfileById(profileInteractor.getAccountIdOrException())
                    ?: return false
            if (userProfile.interests.isEmpty() || userProfile.offers.isEmpty()) {
                tagRepository.setAddTagsDialogShowingTime(Date())
                return true
            }
        }
        return false
    }

    override suspend fun getAddTagPrice(): Long? = tagRepository.getAddTagPrice()

    override suspend fun calculateRemoveTagPrice(removedTagsCount: Int): Long? {
        return tagRepository.getRemoveTagPrice()?.let { removedTagsCount * it }
    }

    override suspend fun isInterestsMandatory(): Boolean = tagRepository.isInterestsMandatory()

    override suspend fun isOffersMandatory(): Boolean = tagRepository.isOffersMandatory()
}