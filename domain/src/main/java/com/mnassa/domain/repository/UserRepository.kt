package com.mnassa.domain.repository

import com.mnassa.domain.model.AccountModel
import com.mnassa.domain.model.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun getCurrentUser(): UserProfileModel?
    suspend fun getAccounts(): List<AccountModel>

    suspend fun getToken(): String?
    suspend fun getAccountId(): String?
}