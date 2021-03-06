package com.mnassa.screen.login.entercode

import android.os.Bundle
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 23.02.2018.
 */
class EnterCodeViewModelImpl(private val loginInteractor: LoginInteractor, private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), EnterCodeViewModel {

    override val openScreenChannel: BroadcastChannel<EnterCodeViewModel.OpenScreenCommand> = BroadcastChannel(10)
    override lateinit var verificationResponse: PhoneVerificationModel

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
        requestVerificationCodeJob = launchWorker {
            val phoneNumber = verificationResponse.phoneNumber
            loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                verificationResponse = it
                if (it.isVerified) {
                    signIn()
                }
            }
        }
    }

    override fun verifyCode(code: String) {
        signIn(code)
    }

    private var signInJob: Job? = null
    private fun signIn(code: String? = null) {
        signInJob?.cancel()
        signInJob = launchWorker {
            withProgressSuspend {
                Timber.d("MNSA_LOGIN EnterCodeViewModelImpl->signIn with code $code")

                val accounts = loginInteractor.signIn(verificationResponse, code)
                val nextScreen = when {
                    accounts.isEmpty() -> EnterCodeViewModel.OpenScreenCommand.RegistrationScreen()
                    accounts.size == SINGLE_ACCOUNT_COUNT -> {
                        userProfileInteractor.setCurrentUserAccount(accounts.first())
                        EnterCodeViewModel.OpenScreenCommand.MainScreen()
                    }
                    else -> EnterCodeViewModel.OpenScreenCommand.SelectAccount(accounts)
                }
                Timber.d("MNSA_LOGIN EnterCodeViewModelImpl->signIn open $nextScreen")
                openScreenChannel.send(nextScreen)
            }
        }
    }

    private companion object {
        private const val SINGLE_ACCOUNT_COUNT = 1
        private const val EXTRA_VERIFICATION_RESPONSE = "EXTRA_VERIFICATION_RESPONSE"
    }
}