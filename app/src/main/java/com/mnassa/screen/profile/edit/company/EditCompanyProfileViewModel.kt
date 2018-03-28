package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
interface EditCompanyProfileViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val imageUploadedChannel: BroadcastChannel<String>
    val tagChannel: BroadcastChannel<TagCommand>
    fun getTagsByIds(ids: List<String>?, isOffers: Boolean)
    fun uploadPhotoToStorage(uri: Uri)
    fun updateCompanyAccount(
            profileAccountModel: ProfileAccountModel,
            userName: String,
            showContactEmail: Boolean,
            contactEmail: String?,
            founded: Long?,
            organizationType: String?,
            website: String?,
            foundedDate: String?,
            locationId: String?,
            interests: List<TagModel>,
            offers: List<TagModel>
    )

    sealed class TagCommand {
        data class TagInterests(val interests: List<TagModel>) : TagCommand()
        data class TagOffers(val offers: List<TagModel>) : TagCommand()
    }

}