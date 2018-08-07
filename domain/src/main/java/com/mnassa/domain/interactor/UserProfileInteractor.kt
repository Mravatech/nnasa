package com.mnassa.domain.interactor

import com.mnassa.core.events.CompositeEventListener
import com.mnassa.domain.model.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {
    val onAccountChangedListener: CompositeEventListener<ShortAccountModel>

    suspend fun getAllAccounts(): ReceiveChannel<List<ShortAccountModel>>

    suspend fun createPersonalAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel
    suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel

    suspend fun setCurrentUserAccount(account: ShortAccountModel)

    suspend fun processAccount(account: PersonalInfoModel)
    suspend fun processAccount(account: CompanyInfoModel)
    suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel)
    suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel)

    suspend fun getToken(): String?
    fun getAccountIdOrNull(): String?
    fun getAccountIdOrException(): String
    suspend fun getAccountByIdChannel(accountId: String): ReceiveChannel<ShortAccountModel?>

    suspend fun getProfileById(accountId: String): ProfileAccountModel?
    suspend fun getProfileByIdChannel(accountId: String): ReceiveChannel<ProfileAccountModel?>
    suspend fun addPushToken(token: String?)

    suspend fun getPermissions(): ReceiveChannel<PermissionsModel>
}