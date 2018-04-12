package com.mnassa.domain.interactor.impl

import android.net.Uri
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.PostsRepository
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository,
                          private val storageInteractor: StorageInteractor,
                          private val tagInteractor: TagInteractor,
                          private val userProfileInteractorImpl: UserProfileInteractor) : PostsInteractor {

    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllWithChangesHandling()
    override suspend fun loadById(id: String): ReceiveChannel<PostModel?> = postsRepository.loadById(id)
    override suspend fun loadAllUserPostByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByAccountUd(accountId)

    private val viewedItemIdsBuffer = ConcurrentSkipListSet<String>()
    private val lastItemsSentTime = AtomicLong()
    private var sendViewedItemsJob: Job? = null
    private var previousAccountId = AtomicReference<String>()

    override suspend fun onItemViewed(item: PostModel) {
        val id = item.id
        val accountId = userProfileInteractorImpl.getAccountId()

        if (item.author.id == accountId) {
            return
        }

        if (accountId != previousAccountId.get()) {
            viewedItemIdsBuffer.clear()
            previousAccountId.set(accountId)
        }

        //bufferize items to send
        viewedItemIdsBuffer.add(id)

        if (System.currentTimeMillis() - lastItemsSentTime.get() < SEND_VIEWED_ITEMS_BUFFER_DELAY) {
            sendViewedItemsJob?.cancel()
        }
        sendViewedItemsJob = launch {
            delay(SEND_VIEWED_ITEMS_BUFFER_DELAY)

            val itemsToSend = viewedItemIdsBuffer.toList()
            viewedItemIdsBuffer.removeAll(itemsToSend)
            try {
                if (itemsToSend.isNotEmpty()) {
                    postsRepository.sendViewed(itemsToSend)
                }
                lastItemsSentTime.set(System.currentTimeMillis())
            } catch (e: Exception) {
                //ignore all exceptions here
                Timber.d(e)
                viewedItemIdsBuffer.addAll(itemsToSend)
            }
        }
    }

    override suspend fun createNeed(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<TagModel>,
            price: Long?,
            placeId: String?
    ): PostModel {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
        }.map { it.await() }
        return postsRepository.createNeed(text, allImages, privacy, createTags(tags), price, placeId)
    }

    override suspend fun updateNeed(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            tags: List<TagModel>,
            price: Long?,
            placeId: String?) {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
        }.map { it.await() }
        postsRepository.updateNeed(postId, text, allImages, createTags(tags), price, placeId)
    }

    override suspend fun removePost(postId: String) {
        postsRepository.removePost(postId)
    }

    override suspend fun repostPost(postId: String, text: String?, privacyConnections: List<String>): PostModel {
        return postsRepository.repostPost(postId, text, privacyConnections)
    }

    private suspend fun createTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags)
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

    override suspend fun createUserRecommendation(accountId: String, text: String, privacy: PostPrivacyOptions) {
        postsRepository.createUserRecommendation(accountId, text, privacy)
    }

    override suspend fun updateUserRecommendation(postId: String, accountId: String, text: String) {
        postsRepository.updateUserRecommendation(postId, accountId, text)
    }

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}