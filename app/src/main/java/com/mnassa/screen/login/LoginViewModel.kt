package com.mnassa.screen.login

import com.mnassa.screen.base.MnassaViewModel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginViewModel : MnassaViewModel {
    fun requestVerificationCode(phoneNumber: String)
    fun login(verificationCode: String)
}