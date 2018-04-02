package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileInteractorImpl(
        private val userRepository: UserRepository
) : UserProfileInteractor {

    override suspend fun getProfile(): ShortAccountModel {
        return requireNotNull(userRepository.getCurrentUser())
    }

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
        userRepository.setCurrentUserAccount(account)
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
        userRepository.setCurrentUserAccount(account)
        return account
    }

    override suspend fun getProfileByAccountId(accountId: String): ProfileAccountModel? {
        return userRepository.getProfileByAccountId(accountId)
    }

    override suspend fun getProfileById(accountId: String): ReceiveChannel<ProfileAccountModel?> =
            userRepository.getProfileById(accountId)

    override suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel) {
        userRepository.updateCompanyAccount(account)
    }

    override suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel) {
        userRepository.updatePersonalAccount(account)
    }

    override suspend fun processAccount(account: PersonalInfoModel) {
        userRepository.processAccount(account)
    }

    override suspend fun processAccount(account: CompanyInfoModel) {
        userRepository.processAccount(account)
    }

    override suspend fun setCurrentUserAccount(account: ShortAccountModel) {
        userRepository.setCurrentUserAccount(account)
    }

    override suspend fun getToken(): String? = userRepository.getFirebaseToken()
    override suspend fun getAccountId(): String? = userRepository.getAccountId()
}