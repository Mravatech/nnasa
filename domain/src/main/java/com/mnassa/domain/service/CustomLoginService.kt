package com.mnassa.domain.service

import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 9/25/2018.
 */
interface CustomLoginService {
    suspend fun requestVerificationCode(phoneNumber: String): ReceiveChannel<PhoneVerificationModel>
    suspend fun signIn(verificationSMSCode: String, response: PhoneVerificationModel)
    suspend fun signOut()
}