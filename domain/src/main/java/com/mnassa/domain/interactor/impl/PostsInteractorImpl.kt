package com.mnassa.domain.interactor.impl

import android.net.Uri
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.PostsRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * Created by Peter on 3/16/2018.
 */
class PostsInteractorImpl(private val postsRepository: PostsRepository,
                          private val storageInteractor: StorageInteractor,
                          private val tagInteractor: TagInteractor,
                          private val userProfileInteractorImpl: UserProfileInteractor) : PostsInteractor {

    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllWithChangesHandling()
    override suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>> = postsRepository.loadAllInfoPosts()
    override suspend fun loadById(id: String): ReceiveChannel<PostModel?> = postsRepository.loadById(id)
    override suspend fun loadAllUserPostByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByAccountId(accountId)

    private val viewItemChannel = ArrayChannel<ListItemEvent<PostModel>>(10)

    init {
        launch {
            viewItemChannel.bufferize(SubscriptionsContainerDelegate(), SEND_VIEWED_ITEMS_BUFFER_DELAY).consumeEach {
                if (it.item.isNotEmpty()) {
                    try {
                        postsRepository.sendViewed(it.item.map { it.id })
                    } catch (e: Exception) {
                        Timber.d(e)
                    }
                }
            }
        }
    }

    override suspend fun onItemViewed(item: PostModel) {
        if (item.author.id == userProfileInteractorImpl.getAccountIdOrException()) {
            return
        }
        viewItemChannel.send(ListItemEvent.Added(item))
    }

    override suspend fun onItemOpened(item: PostModel) {
        postsRepository.sendOpened(listOf(item.id))
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

    override suspend fun createGeneralPost(
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            privacy: PostPrivacyOptions,
            tags: List<TagModel>,
            placeId: String?
    ): PostModel {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
        }.map { it.await() }
        return postsRepository.createGeneralPost(text, allImages, privacy, createTags(tags), placeId)
    }

    override suspend fun updateGeneralPost(
            postId: String,
            text: String,
            imagesToUpload: List<Uri>,
            uploadedImages: List<String>,
            tags: List<TagModel>,
            placeId: String?
    ) {
        val allImages = uploadedImages + imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
        }.map { it.await() }
        postsRepository.updateGeneralPost(postId, text, allImages, createTags(tags), placeId)
    }

    override suspend fun removePost(postId: String) {
        postsRepository.removePost(postId)
    }

    override suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel {
        return postsRepository.repostPost(postId, text, privacy)
    }

    override suspend fun hideInfoPost(postId: String) {
        postsRepository.hideInfoPost(postId)
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