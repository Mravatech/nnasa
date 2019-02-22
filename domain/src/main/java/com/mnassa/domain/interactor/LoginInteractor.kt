package com.mnassa.domain.interactor

import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {

    val onLogoutListener: BroadcastChannel<LogoutReason>

    fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel? = null,
            promoCode: String? = null): ReceiveChannel<PhoneVerificationModel>

    suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel

    suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String? = null): List<ShortAccountModel>
    suspend fun signOut(reason: LogoutReason)

    fun CoroutineScope.handleUserStatus()
    fun CoroutineScope.handleAccountStatus()
    fun CoroutineScope.handleAccountRefresh()
}