package com.mnassa.screen.events.create

import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/23/2018.
 */
interface CreateEventViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener, ChipsAdapter.ChipSearch {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    fun publish(model: RawEventModel)
    suspend fun canPromoteEvents(): Boolean
    suspend fun getPromoteEventPrice(): Long
}