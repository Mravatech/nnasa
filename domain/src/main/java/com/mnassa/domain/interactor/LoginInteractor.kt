package com.mnassa.domain.interactor

import com.mnassa.core.events.CompositeEventListener
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    val onLogoutListener: CompositeEventListener<Unit>

    suspend fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel? = null,
            promoCode: String? = null): ReceiveChannel<PhoneVerificationModel>

    suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel

    suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String? = null): List<ShortAccountModel>
    suspend fun signOut()
}