package com.mnassa.screen.posts.general.create

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/30/2018.
 */
interface CreateGeneralPostViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {

    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?

    fun createPost(text: String, tags: List<TagModel>, images: List<AttachedImage>, placeId: String?, postPrivacyOptions: PostPrivacyOptions)
    suspend fun canPromotePost(): Boolean
    suspend fun getPromotePostPrice(): Long
}