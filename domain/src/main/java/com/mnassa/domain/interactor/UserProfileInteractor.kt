package com.mnassa.domain.interactor

import com.mnassa.domain.model.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {

    suspend fun getProfile(): UserProfileModel
    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String)
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String)

    suspend fun getToken(): String?
    suspend fun getAccountId(): String?
}