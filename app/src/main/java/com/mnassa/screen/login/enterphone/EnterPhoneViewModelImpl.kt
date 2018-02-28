package com.mnassa.screen.login.enterphone

import android.os.Bundle
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class EnterPhoneViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), EnterPhoneViewModel {
    private lateinit var verificationResponse: PhoneVerificationModel

    override val openScreenChannel: ArrayBroadcastChannel<EnterPhoneViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)

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
        showProgress()

        requestVerificationCodeJob?.cancel()
        requestVerificationCodeJob = handleException {
            loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                verificationResponse = it

                hideProgress()

                when {
                    it.isVerified -> signIn(it)
                    else -> openScreenChannel.send(
                            EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode(it))
                }
            }
        }
    }

    private suspend fun signIn(phoneVerificationModel: PhoneVerificationModel) {
        withProgressSuspend {
            val accounts = loginInteractor.signIn(phoneVerificationModel)

            val nextScreen = when {
                accounts.isEmpty() -> EnterPhoneViewModel.OpenScreenCommand.Registration()
                accounts.size == 1 -> {
                    loginInteractor.selectAccount(accounts.first())
                    EnterPhoneViewModel.OpenScreenCommand.MainScreen()
                }
                else -> EnterPhoneViewModel.OpenScreenCommand.SelectAccount(accounts)
            }

            openScreenChannel.send(nextScreen)
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