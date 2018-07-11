package com.mnassa.screen.profile.edit

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/29/2018
 */
abstract class BaseEditableProfileViewModelImpl(private val tagInteractor: TagInteractor) : BaseEditableProfileViewModel, MnassaViewModelImpl() {

    override val addTagRewardChannel: BroadcastChannel<Long?> = ConflatedBroadcastChannel()
    private val isInterestsMandatory = async { tagInteractor.isInterestsMandatory() }
    private val isOffersMandatory = async { tagInteractor.isOffersMandatory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            tagInteractor.getAddTagPrice().consumeTo(addTagRewardChannel)
        }
    }

    protected suspend fun getFilteredTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags.map { it.toString() })
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }


    override suspend fun calculateDeleteTagsPrice(tagsCount: Int): Long? = tagInteractor.calculateRemoveTagPrice(tagsCount)

    override suspend fun isInterestsMandatory(): Boolean = isInterestsMandatory.await()

    override suspend fun isOffersMandatory(): Boolean = isOffersMandatory.await()
}