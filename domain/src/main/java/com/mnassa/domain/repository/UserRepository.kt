package com.mnassa.domain.repository

import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun setCurrentAccount(account: ShortAccountModel?)
    suspend fun getCurrentAccountOrNull(): ShortAccountModel?
    suspend fun getCurrentAccountOrException(): ShortAccountModel

    suspend fun getAccounts(): List<ShortAccountModel>
    suspend fun getAccountById(id: String): ShortAccountModel?

    suspend fun getAccountByIdChannel(accountId: String): ReceiveChannel<ShortAccountModel?>
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

    suspend fun getProfileByAccountId(accountId: String): ProfileAccountModel?
    suspend fun getProfileById(accountId: String): ReceiveChannel<ProfileAccountModel?>
    suspend fun addPushToken()
    fun getAccountIdOrNull(): String?
    fun getAccountIdOrException(): String
    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?

    suspend fun getPermissions(): ReceiveChannel<PermissionsModel>

    suspend fun getUserStatusChannel(firebaseUserId: String): ReceiveChannel<UserStatusModel>
}