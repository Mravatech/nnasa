package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.TagModelTemp
import com.mnassa.domain.model.impl.TagModelTempImpl
import com.mnassa.domain.repository.TagRepository

/**
 * Created by Peter on 2/22/2018.
 */

class TagRepositoryImpl(
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler
) : TagRepository {

    override suspend fun search(search: String): List<TagModelTemp> {
        val tags = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return converter.convertCollection(tags, TagModelTemp::class.java)
                .filter {
                    it.name.toLowerCase().startsWith(search.toLowerCase())
                }
    }

}