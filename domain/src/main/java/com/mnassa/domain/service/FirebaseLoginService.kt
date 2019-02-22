package com.mnassa.domain.service

import com.mnassa.domain.model.PhoneValidationResult
import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface FirebaseLoginService {

    suspend fun checkPhone(phoneNumber: String, promoCode: String? = null): PhoneValidationResult

    suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel? = null): ReceiveChannel<PhoneVerificationModel>

    suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel

    suspend fun signIn(verificationSMSCode: String?, response: PhoneVerificationModel)
    suspend fun signOut()
}