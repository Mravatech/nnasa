package com.mnassa.screen.posts.need.create

import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter

/**
 * Created by Peter on 3/19/2018.
 */
interface CreateNeedViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    suspend fun getTag(tagId: String): TagModel?
}