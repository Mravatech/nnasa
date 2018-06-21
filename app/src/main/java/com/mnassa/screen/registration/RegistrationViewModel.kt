package com.mnassa.screen.registration

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/26/2018.
 */
interface RegistrationViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val openScreenChannel: BroadcastChannel<RegistrationViewModel.OpenScreenCommand>
    val hasPersonalAccountChannel: BroadcastChannel<Boolean>

    fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: List<TagModel>, interests: List<TagModel>)
    fun registerOrganization(userName: String, city: String, companyName: String, offers: List<TagModel>, interests: List<TagModel>)

    suspend fun isInterestsMandatory(): Boolean
    suspend fun isOffersMandatory(): Boolean

    sealed class OpenScreenCommand {
        class PersonalInfoScreen(val shortAccountModel: ShortAccountModel) : OpenScreenCommand()
        class OrganizationInfoScreen(val shortAccountModel: ShortAccountModel) : OpenScreenCommand()
    }
}