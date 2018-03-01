package com.mnassa.screen.registration

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/26/2018.
 */
interface RegistrationViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<RegistrationViewModel.OpenScreenCommand>

    fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: String, interests: String)
    fun registerOrganization(userName: String, city: String, companyName: String, offers: String, interests: String)

    sealed class OpenScreenCommand {
        class PersonalInfoScreen : OpenScreenCommand()
        class OrganizationInfoScreen : OpenScreenCommand()
    }
}