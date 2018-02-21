package com.mnassa.domain.interactor

import com.mnassa.domain.service.LoginService

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    suspend fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(phoneNumber: String): LoginService.VerificationCodeResponse
    suspend fun processVerificationCode(verificationSMSCode: String, response: LoginService.VerificationCodeResponse)
}