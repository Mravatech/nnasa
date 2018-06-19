package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseTagsApi
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.bean.retrofit.request.AddTagsDialogShowingTimeRequest
import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.async
import java.util.*
import java.util.concurrent.TimeUnit

class TagRepositoryImpl(
        private val db: DatabaseReference,
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseTagsApi: FirebaseTagsApi,
        private val userRepository: UserRepository
) : TagRepository {

    override suspend fun get(id: String): TagModel? {
        return databaseReference.child(DatabaseContract.TABLE_TAGS)
                .child(id)
                .await<TagDbEntity>(exceptionHandler)
                ?.run { converter.convert(this, TagModel::class.java) }
    }

    override suspend fun getAll(): List<TagModel> {
        return databaseReference.child(DatabaseContract.TABLE_TAGS)
                .awaitList<TagDbEntity>(exceptionHandler)
                .run { converter.convertCollection(this, TagModel::class.java) }
    }

    override suspend fun search(searchKeyword: String): List<TagModel> {
        val tags = databaseReference.child(DatabaseContract.TABLE_TAGS)
                .apply { keepSynced(true) }
                .awaitList<TagDbEntity>(exceptionHandler)
        return filter(searchKeyword, converter.convertCollection(tags, TagModel::class.java)).await()
    }

    private fun filter(search: String, list: List<TagModel>) = async {
        val search = search.toLowerCase()
        list.filter { it.name.toString().toLowerCase().contains(search) }
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

    override suspend fun getAddTagsDialogInterval(): Long? {
        val hours = db.child(DatabaseContract.TABLE_CLIENT_DATA_COL_TAGS_UPDATE_PERIOD)
                .await<Long>(exceptionHandler)
        return hours?.let { TimeUnit.HOURS.toMillis(it) }
    }

    override suspend fun getAddTagsDialogLastShowingTime(): Date? {
        val timestamp = db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(userRepository.getAccountIdOrException())
                .child(DatabaseContract.TABLE_ACCOUNTS_COL_TAG_REMINDER_TIME)
                .await<Long>(exceptionHandler)
        return timestamp?.let { Date(it) }
    }

    override suspend fun setAddTagsDialogShowingTime(time: Date) {
        firebaseTagsApi.setAddTagsDialogShowingTime(
                AddTagsDialogShowingTimeRequest(userRepository.getAccountIdOrException(), time.time))
                .handleException(exceptionHandler)
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