package com.mnassa.screen.login.entercode

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.JobCancellationException
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 23.02.2018.
 */
class EnterCodeViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), EnterCodeViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<EnterCodeViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    override val errorMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)
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
        requestVerificationCodeJob = launchCoroutineUI {
            try {
                val phoneNumber = verificationResponse.phoneNumber
                loginInteractor.requestVerificationCode(phoneNumber).consumeEach {
                    verificationResponse = it
                    if (it.isVerified) {
                        signIn()
                    }
                }
            } catch (e: JobCancellationException) {
                Timber.d(e)
            } catch (e: Exception) {
                Timber.e(e)
                errorMessageChannel.send(e.message ?: "")
            }
        }
    }

    override fun verifyCode(code: String) {
        signIn(code)
    }

    private var signInJob: Job? = null
    private fun signIn(code: String? = null) {
        signInJob?.cancel()

        signInJob = launchCoroutineUI {

            try {
                val accounts = loginInteractor.signIn(verificationResponse, code)
                val nextScreen = when {
                    accounts.isEmpty() -> EnterCodeViewModel.OpenScreenCommand.RegistrationScreen()
                    accounts.size == 1 -> {
                        loginInteractor.selectAccount(accounts.first())
                        EnterCodeViewModel.OpenScreenCommand.MainScreen()
                    }
                    else -> EnterCodeViewModel.OpenScreenCommand.SelectAccount(accounts)
                }
                openScreenChannel.send(nextScreen)
            } catch (e: JobCancellationException) {
                Timber.d(e)
            } catch (e: LoginInteractor.InvalidVerificationCode) {
                Timber.d(e)
            } catch (e: Exception) {
                Timber.e(e)
                errorMessageChannel.send(e.message ?: "")
            }
        }
    }

    private companion object {
        private const val EXTRA_VERIFICATION_RESPONSE = "EXTRA_VERIFICATION_RESPONSE"
    }
}