package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository) : LoginInteractor {

    override suspend fun isLoggedIn(): Boolean {
        return userRepository.getCurrentUser() != null
    }
}