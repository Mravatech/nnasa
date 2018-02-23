package com.mnassa.domain.service

import java.io.Serializable
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginService {
    suspend fun requestVerificationCode(phoneNumber: String): ReceiveChannel<VerificationCodeResponse>
    suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse)
    suspend fun signOut()

    interface VerificationCodeResponse: Serializable {
        val isVerificationNeeded: Boolean
    }
}