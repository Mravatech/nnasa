package com.mnassa.screen.registration

import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/26/2018.
 */
interface RegistrationViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val openScreenChannel: BroadcastChannel<RegistrationViewModel.OpenScreenCommand>
    suspend fun hasPersonalAccountChannel(): Boolean
    val addTagRewardChannel: BroadcastChannel<Long?>

    fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: List<TagModel>, interests: List<TagModel>)
    fun registerOrganization(userName: String, city: String, companyName: String, offers: List<TagModel>, interests: List<TagModel>)

    suspend fun isInterestsMandatory(): Boolean
    suspend fun isOffersMandatory(): Boolean

    sealed class OpenScreenCommand {
        class PersonalInfoScreen(val shortAccountModel: ProfileAccountModel) : OpenScreenCommand()
        class OrganizationInfoScreen(val shortAccountModel: ProfileAccountModel) : OpenScreenCommand()
    }
}