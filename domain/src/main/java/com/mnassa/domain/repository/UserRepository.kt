package com.mnassa.domain.repository

import com.mnassa.domain.model.AccountModel
import com.mnassa.domain.model.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun setCurrentUserAccount(account: AccountModel)
    suspend fun getCurrentUser(): UserProfileModel?
    suspend fun getAccounts(): List<AccountModel>
    suspend fun createAccount(firstName: String, secondName: String, userName: String, city: String)
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String)

    suspend fun getFirebaseToken(): String?
    suspend fun getFirebaseUserId(): String?
}