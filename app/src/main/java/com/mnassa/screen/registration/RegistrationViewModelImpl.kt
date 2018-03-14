package com.mnassa.screen.registration

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/26/2018.
 */
class RegistrationViewModelImpl(
        private val userProfileInteractor: UserProfileInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor
) : MnassaViewModelImpl(), RegistrationViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<RegistrationViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    override fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: List<String>, interests: List<String>) {
        handleException {
            withProgressSuspend {
                val shortAccountModel = userProfileInteractor.createPersonalAccount(
                        firstName = firstName,
                        secondName = secondName,
                        userName = userName,
                        city = city,
                        offers = offers,
                        interests = interests
                )
                openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen(shortAccountModel))
            }
        }
    }

    override fun registerOrganization(userName: String, city: String, companyName: String, offers: List<String>, interests: List<String>) {
        handleException {
            withProgressSuspend {
                userProfileInteractor.createOrganizationAccount(
                        companyName = companyName,
                        userName = userName,
                        city = city,
                        offers = offers,
                        interests = interests
                )
            }
            openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen())
        }
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel>? {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
}