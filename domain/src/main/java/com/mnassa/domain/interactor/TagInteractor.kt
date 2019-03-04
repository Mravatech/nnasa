package com.mnassa.domain.interactor

import com.mnassa.domain.model.TagModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/7/2018
 */

interface TagInteractor{
    suspend fun get(id: String): TagModel?
    suspend fun get(tagIds: List<String>): List<TagModel>
    suspend fun getAll(): List<TagModel>
    suspend fun createCustomTagIds(tags: List<String>): List<String>
    suspend fun shouldShowAddTagsDialog(): Boolean

    suspend fun getAddTagPrice(): ReceiveChannel<Long?>
    suspend fun calculateRemoveTagPrice(removedTagsCount: Int): Long?

    suspend fun isInterestsMandatory(): Boolean
    suspend fun isOffersMandatory(): Boolean

}
