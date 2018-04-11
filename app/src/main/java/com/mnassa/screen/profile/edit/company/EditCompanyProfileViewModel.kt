package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
interface EditCompanyProfileViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val openScreenChannel: ArrayBroadcastChannel<CompanyScreenCommander>
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