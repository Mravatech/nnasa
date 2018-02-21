package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.mnassa.data.network.api.UsersApi
import com.mnassa.domain.models.UserProfile
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserRepositoryImpl(private val context: Context, private val converter: ConvertersContext, private val usersApi: UsersApi) : UserRepository {

    override suspend fun getCurrentUser(): UserProfile? {
        //TODO: use Firebase here

//        try {
//            val userNetwork = usersApi.getUser().await()
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
//
//        val profile = SomInternalUserProfile(1, "Name1")
//
//        return converter.convert(profile)

        return null
    }

    private companion object {

    }
}

data class SomInternalUserProfile(val id: Int, val name: String)