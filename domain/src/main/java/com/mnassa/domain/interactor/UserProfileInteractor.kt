package com.mnassa.domain.interactor

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModelTemp

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {

    suspend fun getProfile(): ShortAccountModel
    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<TagModelTemp>, interests: List<TagModelTemp>): ShortAccountModel
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<TagModelTemp>, interests: List<TagModelTemp>): ShortAccountModel

    suspend fun setCurrentUserAccount(account: ShortAccountModel)

    suspend fun processAccount(account: ShortAccountModel, path: String?)

    suspend fun getToken(): String?
    suspend fun getAccountId(): String?
}