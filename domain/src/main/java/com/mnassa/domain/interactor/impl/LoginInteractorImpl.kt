package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.AccountModel
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository, private val loginService: FirebaseLoginService) : LoginInteractor {

    override suspend fun isLoggedIn(): Boolean {
        return userRepository.getCurrentUser() != null
    }

    override suspend fun requestVerificationCode(phoneNumber: String, previousResponse: PhoneVerificationModel?): ReceiveChannel<PhoneVerificationModel> {
        loginService.checkPhone(phoneNumber)
        return loginService.requestVerificationCode(phoneNumber, previousResponse)
    }

    override suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String?): List<AccountModel> {
        loginService.signIn(verificationSMSCode, response)
        return userRepository.getAccounts()
    }

    override suspend fun signOut() {
        loginService.signOut()
    }

    override suspend fun selectAccount(account: AccountModel) {
        userRepository.setCurrentUserAccount(account)
    }
}