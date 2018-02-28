package com.mnassa.domain.interactor

import com.mnassa.domain.model.AccountModel
import com.mnassa.domain.model.PhoneVerificationModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    suspend fun isLoggedIn(): Boolean
    suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel? = null): ReceiveChannel<PhoneVerificationModel>

    @Throws(InvalidVerificationCode::class)
    suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String? = null): List<AccountModel>
    suspend fun signOut()

    suspend fun selectAccount(account: AccountModel)



    class InvalidVerificationCode : IllegalArgumentException("Invalid code")
}