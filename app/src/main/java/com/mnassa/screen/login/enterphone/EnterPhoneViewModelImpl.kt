package com.mnassa.screen.login.enterphone

import android.os.Bundle
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
open class EnterPhoneViewModelImpl(private val loginInteractor: LoginInteractor, private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), EnterPhoneViewModel {
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
    override fun requestVerificationCode(phoneNumber: String, promoCode: String?) {
        showProgress()

        requestVerificationCodeJob?.cancel()
        requestVerificationCodeJob = handleException {
            loginInteractor.requestVerificationCode(phoneNumber = phoneNumber, promoCode = promoCode).consumeEach {
                verificationResponse = it
                when {
                    it.isVerified -> signIn(it)
                    else -> {
                        Timber.d("MNSA_LOGIN requestVerificationCode -> openScreenChannel.send EnterVerificationCode")
                        hideProgress()
                        openScreenChannel.send(
                                EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode(it))
                    }
                }
            }
        }
        requestVerificationCodeJob?.invokeOnCompletion { hideProgress() }
    }

    override fun signInByEmail(email: String, password: String) {
        requestVerificationCodeJob?.cancel()

        requestVerificationCodeJob = handleException {
            signIn(loginInteractor.processLoginByEmail(email, password))
        }
        requestVerificationCodeJob?.invokeOnCompletion { hideProgress() }
    }

    private suspend fun signIn(phoneVerificationModel: PhoneVerificationModel) {
        withProgressSuspend {
            Timber.d("MNSA_LOGIN signIn $phoneVerificationModel")
            val accounts = loginInteractor.signIn(phoneVerificationModel)

            val nextScreen = when {
                accounts.isEmpty() -> EnterPhoneViewModel.OpenScreenCommand.Registration()
                accounts.size == 1 -> {
                    userProfileInteractor.setCurrentUserAccount(accounts.first())
                    EnterPhoneViewModel.OpenScreenCommand.MainScreen()
                }
                else -> EnterPhoneViewModel.OpenScreenCommand.SelectAccount(accounts)
            }

            Timber.d("MNSA_LOGIN signIn -> open $nextScreen")
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