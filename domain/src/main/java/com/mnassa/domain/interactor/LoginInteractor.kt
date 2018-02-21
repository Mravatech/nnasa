package com.mnassa.domain.interactor

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginInteractor {
    suspend fun isLoggedIn(): Boolean
}