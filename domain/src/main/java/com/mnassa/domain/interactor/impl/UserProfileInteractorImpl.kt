package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.models.UserProfileModel
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileInteractorImpl(private val userRepository: UserRepository) : UserProfileInteractor {
    override suspend fun getProfile(): UserProfileModel {
        return requireNotNull(userRepository.getCurrentUser())
    }
}