package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mnassa.data.network.api.UsersApi
import com.mnassa.domain.models.UserProfileModel
import com.mnassa.domain.models.impl.UserProfileModelImpl
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserRepositoryImpl(
        private val context: Context,
        private val converter: ConvertersContext,
        private val usersApi: UsersApi,
        private val db: DatabaseReference) : UserRepository {

    override suspend fun getCurrentUser(): UserProfileModel? {
        val user = FirebaseAuth.getInstance().currentUser

        return if (user != null) converter.convert(user) else null
    }
}