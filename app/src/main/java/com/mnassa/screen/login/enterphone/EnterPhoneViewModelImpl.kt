package com.mnassa.screen.login.enterphone

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
 * Created by Peter on 2/21/2018.
 */
class EnterPhoneViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), EnterPhoneViewModel {
    private lateinit var verificationResponse: LoginService.VerificationCodeResponse

    override val openScreenChannel: RendezvousChannel<EnterPhoneViewModel.OpenScreenCommand> = RendezvousChannel()
    override val showMessageChannel: RendezvousChannel<String> = RendezvousChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_VERIFICATION_RESPONSE)) {
                verificationResponse = savedInstanceState.getParcelable(EXTRA_VERIFICATION_RESPONSE)
            }
        }
    }

    private var requestVerificationCodeJob: Job? = null
    override fun requestVerificationCode(phoneNumber: String) {
        requestVerificationCodeJob?.cancel()
        requestVerificationCodeJob = launchCoroutineUI {
            try {
                loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                    verificationResponse = it

                    when {
                        //wait for confirm code
                        verificationResponse.isVerificationNeeded -> openScreenChannel.send(
                                EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode(verificationResponse))
                        //if user have been verified automatically
                        else -> openScreenChannel.send(EnterPhoneViewModel.OpenScreenCommand.MainScreen())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                showMessageChannel.send(e.message ?: "")
            }
        }
    }

    override fun saveInstanceState(outBundle: Bundle) {
        super.saveInstanceState(outBundle)
        if (this::verificationResponse.isInitialized) {
            outBundle.putParcelable(EXTRA_VERIFICATION_RESPONSE, verificationResponse)
        }
    }


    private companion object {
        private const val EXTRA_VERIFICATION_RESPONSE = "EXTRA_VERIFICATION_RESPONSE"
    }
}