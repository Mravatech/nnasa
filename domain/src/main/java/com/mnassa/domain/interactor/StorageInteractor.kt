package com.mnassa.domain.interactor

import com.mnassa.domain.model.StoragePhotoData

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

interface StorageInteractor {
    suspend fun sendImage(uploadPhoto: StoragePhotoData): String
}