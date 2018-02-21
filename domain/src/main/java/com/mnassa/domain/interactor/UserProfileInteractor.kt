package com.mnassa.domain.interactor

import com.mnassa.domain.models.UserProfile

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileInteractor {
    suspend fun getProfile(): UserProfile
}