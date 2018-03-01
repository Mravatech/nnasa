package com.mnassa.domain.service

import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface FirebaseLoginService {

    suspend fun checkPhone(phoneNumber: String, promoCode: String? = null)

    fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel? = null): ReceiveChannel<PhoneVerificationModel>

    suspend fun signIn(verificationSMSCode: String?, response: PhoneVerificationModel)
    suspend fun signOut()
}