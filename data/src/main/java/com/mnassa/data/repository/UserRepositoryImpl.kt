package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.network.api.UsersApi
import com.mnassa.domain.models.UserProfile
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserRepositoryImpl(private val context: Context, private val converter: ConvertersContext, private val usersApi: UsersApi) : UserRepository {

    override suspend fun getCurrentUser(): UserProfile? {
        val user = FirebaseAuth.getInstance().currentUser

        return if (user != null) converter.convert(user) else null
    }
}