package com.mnassa.domain.interactor

import com.mnassa.core.events.CompositeEventListener
import com.mnassa.domain.model.PersonalInfoModel
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import com.mnassa.domain.model.*

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {
    val onAccountChangedListener: CompositeEventListener<ShortAccountModel>

    val currentProfile: BroadcastChannel<ShortAccountModel>
    suspend fun getAllAccounts(): ReceiveChannel<List<ShortAccountModel>>

    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel

    suspend fun setCurrentUserAccount(account: ShortAccountModel)

    suspend fun processAccount(account: PersonalInfoModel)
    suspend fun processAccount(account: CompanyInfoModel)
    suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel)
    suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel)

    suspend fun getToken(): String?
    suspend fun getAccountIdOrNull(): String?
    suspend fun getAccountIdOrException(): String

    suspend fun getProfileByAccountId(accountId: String): ProfileAccountModel?
    suspend fun getProfileById(accountId: String): ReceiveChannel<ProfileAccountModel?>
}