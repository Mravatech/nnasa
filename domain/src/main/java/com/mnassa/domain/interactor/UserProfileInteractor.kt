package com.mnassa.domain.interactor

import com.mnassa.domain.models.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {
    suspend fun getProfile(): UserProfileModel
}