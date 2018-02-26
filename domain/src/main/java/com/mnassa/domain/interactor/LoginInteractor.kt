package com.mnassa.domain.interactor

import com.mnassa.domain.service.LoginService
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    suspend fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(phoneNumber: String, previousResponse: LoginService.VerificationCodeResponse? = null): ReceiveChannel<LoginService.VerificationCodeResponse>
    suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse)
    suspend fun signOut()

    class InvalidVerificationCode : IllegalArgumentException("Invalid code")
}