package com.mnassa.screen.login

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.service.LoginService
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class LoginViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), LoginViewModel {
    private lateinit var verificationResponse: LoginService.VerificationCodeResponse //TODO: save to Bundle

    override val openScreenChannel: RendezvousChannel<LoginViewModel.ScreenType> = RendezvousChannel()
    override val showMessageChannel: RendezvousChannel<String> = RendezvousChannel()

    private var requestVerificationCodeJob: Job? = null
    override fun requestVerificationCode(phoneNumber: String) {
        requestVerificationCodeJob?.cancel()
        requestVerificationCodeJob = launchCoroutineUI {
            try {
                loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                    verificationResponse = it

                    when {
                        //wait for confirm code
                        verificationResponse.isVerificationNeeded -> openScreenChannel.send(LoginViewModel.ScreenType.ENTER_VERIFICATION_CODE)
                        //if user have been verified automatically
                        else -> openScreenChannel.send(LoginViewModel.ScreenType.MAIN)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                showMessageChannel.send(e.localizedMessage)
            }
        }
    }

    override fun login(verificationCode: String) {

        if (!this::verificationResponse.isInitialized) {
            launchCoroutineUI {
                showMessageChannel.send("Verification response does not found!")
            }
            return
        }

        launchCoroutineUI {
            try {
                loginInteractor.signIn(verificationCode, verificationResponse)
                openScreenChannel.send(LoginViewModel.ScreenType.MAIN)
            } catch (e: Exception) {
                Timber.e(e)
                showMessageChannel.send(e.localizedMessage)
            }
        }
    }
}