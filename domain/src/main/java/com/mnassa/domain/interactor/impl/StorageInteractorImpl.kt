package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.StoragePhotoData
import com.mnassa.domain.repository.StorageRepository
import com.mnassa.domain.repository.UserRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageInteractorImpl(private val storageRepository: StorageRepository,
                            private val userRepository: UserRepository) : StorageInteractor {

    override suspend fun sendAvatar(uploadPhoto: StoragePhotoData): String {
        val token = requireNotNull(userRepository.getFirebaseUserId())
        return storageRepository.uploadPhotoToStorage(uploadPhoto, token)
    }

}