package com.mnassa.domain.repository

import com.mnassa.domain.model.UploadingPhotoData

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */
interface StorageRepository {

    suspend fun uploadPhotoToStorage(uploadPhoto: UploadingPhotoData, token: String): String

}