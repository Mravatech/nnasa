package com.mnassa.domain.repository

import com.mnassa.domain.model.StoragePhotoData

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */
interface StorageRepository {
    suspend fun uploadPhotoToStorage(uploadPhoto: StoragePhotoData, token: String): String
}