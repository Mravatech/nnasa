package com.mnassa.screen.posts.need.create

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface CreateNeedViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?
    suspend fun getDefaultExpirationDays(): Long
    fun createPost(need: String, tags: List<TagModel>, images: List<AttachedImage>, placeId: String?, price: Long?, timeOfExpiration: Long?, postPrivacyOptions: PostPrivacyOptions)
    suspend fun canPromotePost(): Boolean
    suspend fun getPromotePostPrice(): Long
}