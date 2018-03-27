package com.mnassa.domain.repository

import com.mnassa.domain.model.PersonalInfoModel
import com.mnassa.domain.model.ShortAccountModel

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

    fun getAccountId(): String?
    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?
}