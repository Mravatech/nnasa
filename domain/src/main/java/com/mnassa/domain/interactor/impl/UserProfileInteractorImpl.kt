package com.mnassa.domain.interactor.impl

import com.mnassa.core.events.impl.SimpleCompositeEventListener
import com.mnassa.domain.exception.AccountDisabledException
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileInteractorImpl(
        userRepositoryLazy: () -> UserRepository
) : UserProfileInteractor {

    private val userRepository: UserRepository by lazy(userRepositoryLazy)

    override val onAccountChangedListener: SimpleCompositeEventListener<ShortAccountModel> = SimpleCompositeEventListener()

    override suspend fun getAllAccounts(): ReceiveChannel<List<ShortAccountModel>> = userRepository.getAllAccounts()

    override suspend fun createPersonalAccount(firstName: String,
                                               secondName: String,
                                               userName: String,
                                               city: String,
                                               offers: List<String>,
                                               interests: List<String>
    ): ShortAccountModel {
        val account = userRepository.createPersonAccount(
                firstName = firstName,
                secondName = secondName,
                userName = userName,
                city = city,
                offers = offers,
                interests = interests)
        userRepository.setCurrentAccount(account)
        return account
    }

    override suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel {
        val account = userRepository.createOrganizationAccount(
                companyName = companyName,
                userName = userName,
                city = city,
                offers = offers,
                interests = interests
        )
        userRepository.setCurrentAccount(account)
        return account
    }

    override suspend fun getProfileById(accountId: String): ProfileAccountModel? {
        return userRepository.getProfileByAccountId(accountId)
    }

    override suspend fun getProfileByIdChannel(accountId: String): ReceiveChannel<ProfileAccountModel?> =
            userRepository.getProfileById(accountId)

    override suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel) =
            userRepository.updateCompanyAccount(account)


    override suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel) =
            userRepository.updatePersonalAccount(account)


    override suspend fun processAccount(account: PersonalInfoModel) = userRepository.processAccount(account)

    override suspend fun processAccount(account: CompanyInfoModel) = userRepository.processAccount(account)

    override suspend fun setCurrentUserAccount(account: ShortAccountModel) {
        if (userRepository.getAccountStatusChannel(account.id).consume { receive() } is UserStatusModel.Enabled) {
            userRepository.setCurrentAccount(account)
            onAccountChangedListener.emit(account)
        } else throw AccountDisabledException("Account ${account.formattedName} (${account.id}) is disabled!", IllegalArgumentException())
    }

    override suspend fun addPushToken() = userRepository.addPushToken()

    override suspend fun getToken(): String? = userRepository.getFirebaseToken()
    override fun getAccountIdOrNull(): String? = userRepository.getAccountIdOrNull()
    override fun getAccountIdOrException(): String = userRepository.getAccountIdOrException()
    override suspend fun getAccountByIdChannel(accountId: String): ReceiveChannel<ShortAccountModel?> =
            userRepository.getAccountByIdChannel(accountId)

    override suspend fun getPermissions(): ReceiveChannel<PermissionsModel> = userRepository.getPermissions()
}