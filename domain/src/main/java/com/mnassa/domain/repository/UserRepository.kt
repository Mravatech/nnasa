package com.mnassa.domain.repository

import com.mnassa.domain.model.PersonalInfoModel
import com.mnassa.domain.model.InvitedShortAccountModel
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun setCurrentUserAccount(account: ShortAccountModel?)
    suspend fun getCurrentUser(): ShortAccountModel?
    suspend fun getCurrentUserWithChannel(): ReceiveChannel<InvitedShortAccountModel>
    suspend fun getAccounts(): List<ShortAccountModel>

    suspend fun createPersonAccount(
            firstName: String,
            secondName: String,
            userName: String,
            city: String,
            offers: List<String>,
            interests: List<String>
    ): ShortAccountModel
    suspend fun createOrganizationAccount(
            companyName: String,
            userName: String,
            city: String,
            offers: List<String>,
            interests: List<String>
    ): ShortAccountModel

    suspend fun processAccount(account: PersonalInfoModel)

    fun getAccountId(): String?
    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?

    suspend fun getById(id: String): ShortAccountModel?
}