package com.mnassa.screen.group.create

import com.mnassa.domain.model.RawGroupModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/22/2018.
 */
interface CreateGroupViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChanel: BroadcastChannel<Unit>

    fun applyChanges(group: RawGroupModel)
    suspend fun getTag(tagId: String): TagModel?
}