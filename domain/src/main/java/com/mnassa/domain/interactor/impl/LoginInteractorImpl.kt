package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.LoginService

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository, private val loginService: LoginService) : LoginInteractor {

    override suspend fun isLoggedIn(): Boolean {
        return userRepository.getCurrentUser() != null
    }

    override suspend fun requestVerificationCode(phoneNumber: String): LoginService.VerificationCodeResponse {
        return loginService.requestVerificationCode(phoneNumber)
    }

    override suspend fun processVerificationCode(verificationSMSCode: String, response: LoginService.VerificationCodeResponse) {

    }
}