package com.mnassa.domain.interactor

import com.mnassa.domain.service.LoginService
import kotlinx.coroutines.experimental.channels.Channel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    suspend fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(phoneNumber: String): Channel<LoginService.VerificationCodeResponse>
    suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse)
    suspend fun signOut()

    class InvalidVerificationCode : IllegalArgumentException()
}