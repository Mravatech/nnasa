package com.mnassa.screen.registration

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
class RegistrationViewModelImpl(private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), RegistrationViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<RegistrationViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    override val errorMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: String, interests: String) {
        launchCoroutineUI {

            try {
                userProfileInteractor.createPersonalAccount(
                        firstName = firstName,
                        secondName = secondName,
                        userName = userName,
                        city = city,
                        offers = offers,
                        interests = interests
                )
                openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen())
            } catch (e: Exception) {
                Timber.e(e)
                e.message?.let { errorMessageChannel.send(it) }
            }
        }
    }

    override fun registerOrganization(userName: String, city: String, companyName: String, offers: String, interests: String) {
        launchCoroutineUI {

            try {
                userProfileInteractor.createOrganizationAccount(
                        companyName = companyName,
                        userName = userName,
                        city = city,
                        offers = offers,
                        interests = interests
                )
                openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen())
            } catch (e: Exception) {
                Timber.e(e)
                e.message?.let { errorMessageChannel.send(it) }
            }
        }
    }
}