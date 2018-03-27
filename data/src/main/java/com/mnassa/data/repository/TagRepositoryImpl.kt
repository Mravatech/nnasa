package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseTagsApi
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.async

class TagRepositoryImpl(
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseTagsApi: FirebaseTagsApi
) : TagRepository {

    override suspend fun search(searchKeyword: String): List<TagModel> {
        val tags = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return filter(searchKeyword, converter.convertCollection(tags, TagModel::class.java)).await()
    }

    private fun filter(search: String, list: List<TagModel>) = async {
        list.filter { it.name.toLowerCase().startsWith(search.toLowerCase()) }
    }

    override suspend fun createCustomTagIds(tags: List<String>): List<String> {
        return firebaseTagsApi.createCustomTagIds(CustomTagsRequest(tags)).handleException(exceptionHandler).data.tags
    }

    override suspend fun getTagsByIds(ids: List<String>): List<TagModel> {
        if (ids.isEmpty()) return emptyList()
        val tags = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return filterById(ids, tags).await()
    }

    private fun filterById(ids: List<String>, tags: List<TagDbEntity>) = async {
        val result = mutableListOf<TagModel>()
        for (tag in tags) {
            if (ids.contains(tag.id)) {
                result.add(converter.convert(tag, TagModel::class.java))
            }
        }
        result
    }

}