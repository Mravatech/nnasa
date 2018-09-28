package com.mnassa.domain.service

import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 9/25/2018.
 */
interface CustomLoginService {
    fun requestVerificationCode(phoneNumber: String): ReceiveChannel<PhoneVerificationModel>
    suspend fun signIn(verificationSMSCode: String, response: PhoneVerificationModel)
    suspend fun signOut()
}