package com.mnassa.domain.service

import android.os.Parcelable
import java.io.Serializable
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginService {
    suspend fun requestVerificationCode(phoneNumber: String, previousResponse: VerificationCodeResponse? = null): ReceiveChannel<VerificationCodeResponse>
    suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse)
    suspend fun signOut()

    interface VerificationCodeResponse: Parcelable {
        val phoneNumber: String
        val isVerificationNeeded: Boolean
    }
}