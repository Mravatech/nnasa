package com.mnassa.domain.interactor.impl

import android.net.Uri
import com.mnassa.domain.interactor.StorageInteractor
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

    override suspend fun sendAvatar(uri: Uri): ReceiveChannel<String> {
        val token = userRepository.getCurrentUser()?.id!!
        return storageRepository.uploadAvatarToStorage(uri, "avatars/", token)
    }

    override fun cancelUploading() {
        storageRepository.cancelUploading()
    }

    private suspend fun getPath(folder: String): String {
        val token = userRepository.getCurrentUser()?.id
        return "$folder$token"
    }

}