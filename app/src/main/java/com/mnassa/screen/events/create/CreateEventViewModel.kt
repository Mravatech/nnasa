package com.mnassa.screen.events.create

import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.RawEventModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 4/23/2018.
 */
interface CreateEventViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun publish(model: RawEventModel)
    suspend fun canPromoteEvents(): Boolean
    suspend fun getPromoteEventPrice(): Long
    suspend fun getUserLocation(): LocationPlaceModel?
}