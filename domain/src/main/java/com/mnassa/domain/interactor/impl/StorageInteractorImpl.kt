package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.DownloadPhoto
import com.mnassa.domain.model.UploadPhoto
import com.mnassa.domain.repository.StorageRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageInteractorImpl(private val storageRepository: StorageRepository,
                            private val userRepository: UserRepository) : StorageInteractor {

    override suspend fun getAvatar(downloadPhoto: DownloadPhoto): ReceiveChannel<String> {
        val token = userRepository.getCurrentUser()?.id!!
        return storageRepository.downloadPhotoFromStorage(downloadPhoto, token)
    }

    override suspend fun sendAvatar(uploadPhoto: UploadPhoto): ReceiveChannel<String> {
        val token = userRepository.getCurrentUser()?.id!!
        return storageRepository.uploadPhotoToStorage(uploadPhoto, token)
    }

    override fun cancelUploading() {
        storageRepository.cancelUploading()
    }

}