package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseTagsApi
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.async

/**
 * Created by Peter on 2/22/2018.
 */

class TagRepositoryImpl(
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseTagsApi: FirebaseTagsApi
) : TagRepository {

    override suspend fun search(search: String): List<TagModel> {
        val tags = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return filter(search, converter.convertCollection(tags, TagModel::class.java)).await()
    }

    private fun filter(search: String, list: List<TagModel>) = async {
        list.filter { it.name.toLowerCase().startsWith(search.toLowerCase()) }
    }

    override suspend fun createCustomTagIds(tags: List<String>): List<String> {
        return firebaseTagsApi.createCustomTagIds(CustomTagsRequest(tags)).await().data.tags
    }

}