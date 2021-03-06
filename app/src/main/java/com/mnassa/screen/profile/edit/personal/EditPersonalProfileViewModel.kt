package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
interface EditPersonalProfileViewModel : BaseEditableProfileViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val openScreenChannel: BroadcastChannel<PersonalScreenCommander>
    fun saveLocallyAvatarUri(uri: Uri)
    fun updatePersonalAccount(
            profileAccountModel: ProfileAccountModel,
            firstName: String,
            secondName: String,
            userName: String,
            showContactEmail: Boolean,
            contactEmail: String?,
            showContactPhone: Boolean,
            contactPhone: String?,
            birthday: Long?,
            birthdayDate: String?,
            locationId: String?,
            isMale: Boolean,
            abilities: List<AccountAbility>,
            interests: List<TagModel>,
            offers: List<TagModel>
    )
    sealed class PersonalScreenCommander{
        class ClosePersonalEditScreen : PersonalScreenCommander()
    }
}