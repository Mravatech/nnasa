package com.mnassa.domain.repository

import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun setCurrentAccount(account: ShortAccountModel?)
    suspend fun getCurrentAccount(): ShortAccountModel?
    suspend fun getCurrentAccountChannel(): ReceiveChannel<InvitedShortAccountModel>
    suspend fun getAccounts(): List<ShortAccountModel>
    suspend fun getAccountById(id: String): ShortAccountModel?

    val currentProfile: BroadcastChannel<ShortAccountModel>
    suspend fun getAllAccounts(): ReceiveChannel<List<ShortAccountModel>>

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
    suspend fun processAccount(account: CompanyInfoModel)
    suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel)
    suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel)

    suspend fun getProfileByAccountId(accountId: String) : ProfileAccountModel?
    suspend fun getProfileById(accountId: String) : ReceiveChannel<ProfileAccountModel?>
    suspend fun addPushToken()
    fun getAccountId(): String?
    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?
}