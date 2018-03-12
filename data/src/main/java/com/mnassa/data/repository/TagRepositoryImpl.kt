package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository

/**
 * Created by Peter on 2/22/2018.
 */

class TagRepositoryImpl(
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler
) : TagRepository {

    override suspend fun search(search: String): List<TagModel> {
        val t = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return converter.convertCollection(t, TagModel::class.java).filter { it.nameEn.toLowerCase().startsWith(search.toLowerCase()) ||
                it.nameAr.toLowerCase().startsWith(search.toLowerCase())
        }
    }

}