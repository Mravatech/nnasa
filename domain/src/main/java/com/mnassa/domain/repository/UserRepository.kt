package com.mnassa.domain.repository

import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun setCurrentUserAccount(account: ShortAccountModel?)
    suspend fun getCurrentUser(): ShortAccountModel?
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
    suspend fun processAccount(account: CompanyInfoModel)
    suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel)
    suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel)

    suspend fun getPrifileByAccountId(accountId: String) : ProfileAccountModel?
    suspend fun getPrifileById(accountId: String) : ReceiveChannel<ProfileAccountModel?>

    fun getAccountId(): String?
    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?

    suspend fun getById(id: String): ShortAccountModel?
}