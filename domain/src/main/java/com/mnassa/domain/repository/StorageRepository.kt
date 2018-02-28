package com.mnassa.domain.repository

import com.mnassa.domain.models.storage.DownloadPhoto
import com.mnassa.domain.models.storage.UploadPhoto
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */
interface StorageRepository {

    suspend fun uploadPhotoToStorage(uploadPhoto: UploadPhoto, token: String): ReceiveChannel<String>
    suspend fun downloadPhotoFromStorage(downloadPhoto: DownloadPhoto, token: String): ReceiveChannel<String>
    fun cancelUploading()

}