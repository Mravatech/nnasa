package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
interface EditPersonalProfileViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val imageUploadedChannel: BroadcastChannel<String>
    val tagChannel: BroadcastChannel<TagCommand>
    fun getTagsByIds(ids: List<String>?, isOffers: Boolean)
    fun uploadPhotoToStorage(uri: Uri)

    sealed class TagCommand {
        data class TagInterests(val interests: List<TagModel>) : TagCommand()
        data class TagOffers(val offers: List<TagModel>) : TagCommand()
    }

}