package com.mnassa.domain.repository

import com.mnassa.domain.models.UserProfile

/**
 * Created by Peter on 2/21/2018.
 */
interface UserRepository {
    suspend fun getCurrentUser(): UserProfile?
}