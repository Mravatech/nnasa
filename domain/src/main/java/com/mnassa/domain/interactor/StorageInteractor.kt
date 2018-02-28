package com.mnassa.domain.interactor

import com.mnassa.domain.models.storage.DownloadPhoto
import com.mnassa.domain.models.storage.UploadPhoto
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

interface StorageInteractor {
    suspend fun getAvatar(downloadPhoto: DownloadPhoto): ReceiveChannel<String>
    suspend fun sendAvatar(uploadPhoto: UploadPhoto): ReceiveChannel<String>
    fun cancelUploading()
}