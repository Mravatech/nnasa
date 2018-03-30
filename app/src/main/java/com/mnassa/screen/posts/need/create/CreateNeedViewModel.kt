package com.mnassa.screen.posts.need.create

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface CreateNeedViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?
    fun createPost(need: String, tags: List<TagModel>, images: List<AttachedImage>, placeId: String?, price: Double?, shareOptions: SharingOptionsController.ShareToOptions)
}