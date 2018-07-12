package com.mnassa.domain.interactor.impl

import android.arch.lifecycle.LiveData
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

    override suspend fun loadAllWithPagination(): ReceiveChannel<PostModel> = postsRepository.loadAllWithPagination()
    override suspend fun loadAll(): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllWithChangesHandling()
    override suspend fun loadAllImmediately(): List<PostModel> = postsRepository.loadAllImmediately()
    override suspend fun loadAllInfoPosts(): ReceiveChannel<ListItemEvent<InfoPostModel>> = postsRepository.loadAllInfoPosts()
    override suspend fun loadById(id: String, authorId: String): ReceiveChannel<PostModel?> = postsRepository.loadById(id, authorId)
    override suspend fun loadAllUserPostByAccountId(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByAccountId(accountId)
    override suspend fun loadAllUserPostByAccountIdImmediately(accountId: String): List<PostModel> = postsRepository.loadAllUserPostByAccountIdImmediately(accountId)
    override suspend fun loadAllByGroupId(groupId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsRepository.loadAllByGroupId(groupId)
    override suspend fun loadAllByGroupIdImmediately(groupId: String): List<PostModel> = postsRepository.loadAllByGroupIdImmediately(groupId)

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

    override suspend fun onItemOpened(item: PostModel) = postsRepository.sendOpened(listOf(item.id))

    override suspend fun resetCounter() = postsRepository.resetCounter()

    override suspend fun createNeed(post: RawPostModel): PostModel {
        return postsRepository.createNeed(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)
        ))
    }

    override suspend fun updateNeed(post: RawPostModel) {
        postsRepository.updateNeed(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun changeStatus(id: String, status: ExpirationType) {
        postsRepository.changeStatus(id, status)
    }

    override suspend fun createGeneralPost(post: RawPostModel): PostModel {
        return postsRepository.createGeneralPost(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun updateGeneralPost(post: RawPostModel) {

        postsRepository.updateGeneralPost(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun createOffer(post: RawPostModel): OfferPostModel {
        return postsRepository.createOffer(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun updateOffer(post: RawPostModel) {
        postsRepository.updateOffer(post.copy(
                processedImages = processImages(post),
                processedTags = processTags(post)))
    }

    override suspend fun createUserRecommendation(post: RawRecommendPostModel) = postsRepository.createUserRecommendation(post)
    override suspend fun updateUserRecommendation(post: RawRecommendPostModel) = postsRepository.updateUserRecommendation(post)
    override suspend fun getShareOfferPostPrice(): Long? = postsRepository.getShareOfferPostPrice()
    override suspend fun getShareOfferPostPerUserPrice(): Long? = postsRepository.getShareOfferPostPerUserPrice()
    override suspend fun getPromotePostPrice(): Long = postsRepository.getPromotePostPrice() ?: 0L
    override suspend fun removePost(postId: String) = postsRepository.removePost(postId)
    override suspend fun repostPost(postId: String, text: String?, privacy: PostPrivacyOptions): PostModel =
            postsRepository.repostPost(postId, text, privacy)

    override suspend fun hideInfoPost(postId: String) = postsRepository.hideInfoPost(postId)
    override suspend fun loadOfferCategories(): List<OfferCategoryModel> = postsRepository.loadOfferCategories()
    override suspend fun promote(post: PostModel) = postsRepository.promote(post)

    private suspend fun processTags(post: RawPostModel): List<String> {
        val customTagsAndTagsWithIds: List<TagModel> = post.tags

        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags.map { it.toString() })
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

    private suspend fun processImages(post: RawPostModel): List<String> {
        return post.uploadedImages + post.imagesToUpload.map {
            async { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_POSTS)) }
        }.map { it.await() }
    }

    override suspend fun getDefaultExpirationDays(): Long = postsRepository.getDefaultExpirationDays()

    private companion object {
        private const val SEND_VIEWED_ITEMS_BUFFER_DELAY = 1_000L
    }
}