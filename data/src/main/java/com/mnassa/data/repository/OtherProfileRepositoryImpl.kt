package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.repository.OtherProfileRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class OtherProfileRepositoryImpl (
        private val converter: ConvertersContext,
        private val exceptionHandler: ExceptionHandler,
        private val db: DatabaseReference
) : OtherProfileRepository {
    override suspend fun getPrifileByAccountId(accountId: String): ProfileAccountModel? {
        val bean = db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .apply { keepSynced(true) }
                .await<ProfileDbEntity>(exceptionHandler) ?: return null
        return converter.convert(bean)
    }
}