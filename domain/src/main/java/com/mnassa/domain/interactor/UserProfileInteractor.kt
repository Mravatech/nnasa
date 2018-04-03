package com.mnassa.domain.interactor

import com.mnassa.domain.model.InvitedShortAccountModel
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {

    val currentProfile: BroadcastChannel<ShortAccountModel>
    suspend fun getCurrentUserWithChannel(): ReceiveChannel<InvitedShortAccountModel>
    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel

    suspend fun setCurrentUserAccount(account: ShortAccountModel)

    suspend fun processAccount(account: ShortAccountModel, path: String?)

    suspend fun getToken(): String?
    suspend fun getAccountId(): String?
}