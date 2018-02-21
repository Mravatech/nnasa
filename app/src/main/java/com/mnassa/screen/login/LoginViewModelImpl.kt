package com.mnassa.screen.login

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class LoginViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), LoginViewModel {
    override val openScreenChannel: RendezvousChannel<LoginViewModel.ScreenType> = RendezvousChannel()

    override fun requestVerificationCode(phoneNumber: String) {
        launchCoroutineUI {
            try {
                val result = loginInteractor.requestVerificationCode(phoneNumber)
                if (!result.isVerificationNeeded) {
                    openScreenChannel.send(LoginViewModel.ScreenType.MAIN)
                    return@launchCoroutineUI
                }

                //wait for confirm code

            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }

    override fun login(verificationCode: String) {

    }
}