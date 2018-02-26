package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.UserProfileModel
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileInteractorImpl(private val userRepository: UserRepository) : UserProfileInteractor {
    override suspend fun getProfile(): UserProfileModel {
        return requireNotNull(userRepository.getCurrentUser())
    }

    override suspend fun getToken(): String? = userRepository.getToken()
    override suspend fun getAccountId(): String? = userRepository.getAccountId()
}