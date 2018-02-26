package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.bean.firebase.AccountBean
import com.mnassa.domain.model.AccountModel
import com.mnassa.domain.model.UserProfileModel
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserRepositoryImpl(
        private val converter: ConvertersContext,
        private val db: DatabaseReference) : UserRepository {

    override suspend fun getCurrentUser(): UserProfileModel? {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) converter.convert(user) else null
    }

    override suspend fun getAccounts(): List<AccountModel> {
        val user = FirebaseAuth.getInstance().currentUser ?: return emptyList()
        val beans = db.child("accountLinks").child(user.uid).awaitList<AccountBean>()
        return converter.convertCollection(beans, AccountModel::class.java)
    }

    override suspend fun getToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return user.getIdToken(false).await().token
    }

    override suspend fun getAccountId(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return user.uid
    }
}