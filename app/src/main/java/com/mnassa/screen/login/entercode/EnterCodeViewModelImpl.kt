package com.mnassa.screen.login.entercode

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.service.LoginService
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 23.02.2018.
 */
class EnterCodeViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), EnterCodeViewModel {

    override val openScreenChannel: RendezvousChannel<EnterCodeViewModel.OpenScreenCommand> = RendezvousChannel()
    override val showMessageChannel: RendezvousChannel<String> = RendezvousChannel()
    override lateinit var verificationResponse: LoginService.VerificationCodeResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            verificationResponse = getParcelable(EXTRA_VERIFICATION_RESPONSE)
        }
    }

    override fun saveInstanceState(outBundle: Bundle) {
        super.saveInstanceState(outBundle)
        outBundle.putParcelable(EXTRA_VERIFICATION_RESPONSE, verificationResponse)
    }

    private var requestVerificationCodeJob: Job? = null
    override fun resendCode() {
        requestVerificationCodeJob?.cancel()
        requestVerificationCodeJob = launchCoroutineUI {
            try {
                val phoneNumber = verificationResponse.phoneNumber
                loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                    verificationResponse = it
                    when {
                        verificationResponse.isVerificationNeeded -> {
                            //do nothing
                            //wait for confirm code
                        }
                    //if user have been verified automatically
                        else -> openScreenChannel.send(EnterCodeViewModel.OpenScreenCommand.MainScreen())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                showMessageChannel.send(e.message ?: "")
            }
        }
    }

    private var loginJob: Job? = null
    override fun verifyCode(code: String) {
        loginJob?.cancel()

        loginJob = launchCoroutineUI {
            try {
                loginInteractor.signIn(code, verificationResponse)
                openScreenChannel.send(EnterCodeViewModel.OpenScreenCommand.MainScreen())
            } catch (e: Exception) {
                Timber.e(e)
                showMessageChannel.send(e.message ?: "")
            }
        }
    }

    private companion object {
        private const val EXTRA_VERIFICATION_RESPONSE = "EXTRA_VERIFICATION_RESPONSE"
    }
}