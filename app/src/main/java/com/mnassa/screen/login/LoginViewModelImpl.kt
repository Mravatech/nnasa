package com.mnassa.screen.login

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class LoginViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), LoginViewModel {

    override fun requestVerificationCode(phoneNumber: String) {
        launchCoroutineUI {
            try {
                val result = loginInteractor.requestVerificationCode(phoneNumber)

            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }

    override fun login(verificationCode: String) {

    }
}