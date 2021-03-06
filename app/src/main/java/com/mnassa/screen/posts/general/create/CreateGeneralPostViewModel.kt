package com.mnassa.screen.posts.general.create

import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.RawPostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 4/30/2018.
 */
interface CreateGeneralPostViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {

    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?

    suspend fun applyChanges(post: RawPostModel)
    suspend fun canPromotePost(): Boolean
    suspend fun getPromotePostPrice(): Long

    suspend fun getUserLocation(): LocationPlaceModel?
}