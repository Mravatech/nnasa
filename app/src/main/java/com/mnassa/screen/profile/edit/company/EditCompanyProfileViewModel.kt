package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
interface EditCompanyProfileViewModel : BaseEditableProfileViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val openScreenChannel: BroadcastChannel<CompanyScreenCommander>
    fun saveLocallyAvatarUri(uri: Uri)
    fun updateCompanyAccount(
            profileAccountModel: ProfileAccountModel,
            userName: String,
            companyName: String,
            showContactEmail: Boolean,
            showContactPhone: Boolean,
            contactEmail: String?,
            contactPhone: String?,
            founded: Long?,
            organizationType: String?,
            website: String?,
            foundedDate: String?,
            locationId: String?,
            interests: List<TagModel>,
            offers: List<TagModel>
    )

    sealed class CompanyScreenCommander{
        class CloseCompanyEditScreen : CompanyScreenCommander()
    }

}