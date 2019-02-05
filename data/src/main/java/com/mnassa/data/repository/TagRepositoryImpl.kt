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
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class TagRepositoryImpl(
        private val db: DatabaseReference,
        private val converter: ConvertersContext,
        private val databaseReference: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseTagsApi: FirebaseTagsApi,
        private val userRepository: UserRepository
) : TagRepository {

    @Volatile
    private var tagsCache: Map<String, TagModel>? = null

    private val tagsMutex = Mutex()

    override suspend fun get(id: String): TagModel? {
        return tagsCache?.get(id) ?: run {
            databaseReference.child(DatabaseContract.TABLE_TAGS)
                .child(id)
                .await<TagDbEntity>(exceptionHandler)
                ?.run { converter.convert(this, TagModel::class.java) }
        }
    }

    override suspend fun getAll(): List<TagModel> {
        return getAll(forceUpdateCache = false)
    }

    private suspend fun getAll(forceUpdateCache: Boolean): List<TagModel> {
        return tagsMutex.withLock {
            if (forceUpdateCache) {
                tagsCache = null
            }
            // Return the list of preloaded models of
            // tags, or load then from network.
            tagsCache?.values?.toList()
                ?: run {
                    getAllFromNetwork()
                        .also { tags ->
                            tagsCache = HashMap<String, TagModel>().apply {
                                tags.forEach {
                                    val id = it.id
                                    if (id != null) {
                                        put(id, it)
                                    }
                                }
                            }
                        }
                }
        }
    }

    private suspend fun getAllFromNetwork(): List<TagModel> {
        return databaseReference.child(DatabaseContract.TABLE_TAGS)
            .awaitList<TagDbEntity>(exceptionHandler)
            .run { converter.convertCollection(this, TagModel::class.java) }
    }

    override suspend fun createCustomTagIds(tags: List<String>): List<String> {
        return firebaseTagsApi.createCustomTagIds(CustomTagsRequest(tags))
                .handleException(exceptionHandler).data.tags
                .also {
                    // Also update the cache
                    getAll(forceUpdateCache = true)
                }
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

}