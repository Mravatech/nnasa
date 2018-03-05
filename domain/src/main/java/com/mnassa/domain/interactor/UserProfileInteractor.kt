package com.mnassa.domain.interactor

import com.mnassa.domain.model.ShortAccountModel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {

    suspend fun getProfile(): ShortAccountModel
    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel

    suspend fun setCurrentUserAccount(account: ShortAccountModel)

    suspend fun getToken(): String?
    suspend fun getAccountId(): String?
}