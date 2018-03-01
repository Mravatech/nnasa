package com.mnassa.domain.interactor

import com.mnassa.domain.model.DownloadingPhotoData
import com.mnassa.domain.model.UploadingPhotoData

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

interface StorageInteractor {
    suspend fun getAvatar(downloadPhoto: DownloadingPhotoData): String
    suspend fun sendAvatar(uploadPhoto: UploadingPhotoData): String
}