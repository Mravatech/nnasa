package com.mnassa.data.repository

import com.google.firebase.database.DatabaseReference
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.api.FirebaseTagsApi
import com.mnassa.data.network.bean.firebase.PriceDbEntity
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.data.network.bean.retrofit.request.AddTagsDialogShowingTimeRequest
import com.mnassa.data.network.bean.retrofit.request.CustomTagsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.withContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TagRepositoryImpl(
        private val db: DatabaseReference,
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseTagsApi: FirebaseTagsApi,
        private val userRepository: UserRepository
) : TagRepository {

    private val tagsCache = ConcurrentHashMap<String, TagModel>()
    @Volatile
    private var isTagsCacheLoaded = false

    override suspend fun get(id: String): TagModel? {
        @Suppress("DeferredResultUnused")
        async { updateCacheIfNeeded() }

        return tagsCache.getOrPut(id) {
            databaseReference.child(DatabaseContract.TABLE_TAGS)
                    .child(id)
                    .await<TagDbEntity>(exceptionHandler)
                    ?.run { converter.convert(this, TagModel::class.java) }
        }
    }

    override suspend fun getAll(): List<TagModel> {
        return if (isTagsCacheLoaded) tagsCache.values.toList()
        else getAllAndUpdateCache()
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
        return firebaseTagsApi.createCustomTagIds(CustomTagsRequest(tags))
                .handleException(exceptionHandler).data.tags
                .also { updateCacheIfNeeded(forceUpdate = true) }
    }

    override suspend fun getAddTagsDialogInterval(): Long? {
        val days = db.child(DatabaseContract.TABLE_CLIENT_DATA_COL_TAGS_UPDATE_PERIOD)
                .await<Long>(exceptionHandler)
        return days?.let { TimeUnit.DAYS.toMillis(it) }
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

    override suspend fun getAddTagPrice(): ReceiveChannel<Long?> {
        return db.child(DatabaseContract.TABLE_DICTIONARY)
                .child(DatabaseContract.TABLE_DICTIONARY_COL_REWARD_FOR_PROFILE_TAG)
                .also { it.keepSynced(true) }
                .toValueChannel<PriceDbEntity>(exceptionHandler)
                .map { it?.takeIf { it.state }?.amount }
    }

    override suspend fun getRemoveTagPrice(): ReceiveChannel<Long?> {
        return db.child(DatabaseContract.TABLE_DICTIONARY)
                .child(DatabaseContract.TABLE_DICTIONARY_COL_PENALTY_FOR_PROFILE_TAG)
                .also { it.keepSynced(true) }
                .toValueChannel<PriceDbEntity>(exceptionHandler)
                .map { it?.takeIf { it.state }?.amount }
    }

    override suspend fun isInterestsMandatory(): Boolean {
        return db.child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_INTERESTS_MANDATORY)
                .also { it.keepSynced(true) }
                .await(exceptionHandler) ?: false
    }

    override suspend fun isOffersMandatory(): Boolean {
        return db.child(DatabaseContract.TABLE_CLIENT_DATA)
                .child(DatabaseContract.TABLE_CLIENT_DATA_COL_OFFERS_MANDATORY)
                .also { it.keepSynced(true) }
                .await(exceptionHandler) ?: false
    }

    private suspend fun getAllAndUpdateCache(): List<TagModel> {
        return withContext(DefaultDispatcher) {
            databaseReference.child(DatabaseContract.TABLE_TAGS)
                    .awaitList<TagDbEntity>(exceptionHandler)
                    .run { converter.convertCollection(this, TagModel::class.java) }
                    .also {
                        it.forEach {
                            val id = it.id
                            if (id != null) tagsCache[id] = it
                        }
                    }
        }
    }

    private suspend fun updateCacheIfNeeded(forceUpdate: Boolean = false) {
        if (isTagsCacheLoaded && !forceUpdate) return
        getAllAndUpdateCache()
        isTagsCacheLoaded = true
    }

}