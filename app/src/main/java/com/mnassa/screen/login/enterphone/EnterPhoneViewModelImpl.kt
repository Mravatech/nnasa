package com.mnassa.screen.login.enterphone

import android.os.Bundle
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
open class EnterPhoneViewModelImpl(private val loginInteractor: LoginInteractor, private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), EnterPhoneViewModel {
    private lateinit var verificationResponse: PhoneVerificationModel
    private var signInJob: Job? = null

    override val openScreenChannel: BroadcastChannel<EnterPhoneViewModel.OpenScreenCommand> = BroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_VERIFICATION_RESPONSE)) {
                verificationResponse = savedInstanceState.getParcelable(EXTRA_VERIFICATION_RESPONSE)
            }
        }
    }

    override fun requestVerificationCode(phoneNumber: String, promoCode: String?) {
        signInJob?.cancel()
        signInJob = launchWorker {
            withProgressSuspend {
                loginInteractor.requestVerificationCode(phoneNumber = phoneNumber, promoCode = promoCode).consumeEach {
                    verificationResponse = it
                    when {
                        it.isVerified -> performSignIn(it)
                        else -> {
                            Timber.d("MNSA_LOGIN requestVerificationCode -> openScreenChannel.send EnterVerificationCode")
                            openScreenChannel.send(EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode(it))
                        }
                    }
                }
            }
        }
    }

    override fun signInByEmail(email: String, password: String) {
        signInJob?.cancel()
        signInJob = launchWorker {
            withProgressSuspend {
                val verificationModel = loginInteractor.processLoginByEmail(email, password)
                performSignIn(verificationModel)
            }
        }
    }

    private suspend fun performSignIn(phoneVerificationModel: PhoneVerificationModel) {
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